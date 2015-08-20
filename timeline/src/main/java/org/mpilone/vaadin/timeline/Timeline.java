package org.mpilone.vaadin.timeline;

import java.util.*;

import org.mpilone.vaadin.timeline.ClickListener.ClickEvent;
import org.mpilone.vaadin.timeline.ContextMenuListener.ContextMenuEvent;
import org.mpilone.vaadin.timeline.DoubleClickListener.DoubleClickEvent;
import org.mpilone.vaadin.timeline.shared.*;

import com.vaadin.annotations.*;
import com.vaadin.annotations.JavaScript;
import com.vaadin.data.Container;
import com.vaadin.server.KeyMapper;
import com.vaadin.ui.*;

/**
 * An implementation of the vis.js Timeline component (http://visjs.org/). The
 * timeline displays {@link TimelineItem}s provided by a
 * {@link TimelineItemProvider} on a scrollable and zoomable interface.
 *
 * @author mpilone
 */
@StyleSheet({"vis/dist/vis.min.css", "vis/dist/vis.map",
  "vis/dist/img/timeline/delete.png"})
@JavaScript({"timeline_connector.js", "vis/dist/vis.min.js"})
public class Timeline extends AbstractJavaScriptComponent implements
    TimelineItemProvider, TimelineItemProvider.Editable,
    TimelineItemProvider.ItemSetChangeListener {

  private List<TimelineItem> items;
  private HashSet<Object> selection;
  private KeyMapper<Object> itemIdMapper;
  private List<TimelineGroup> groups;
  private TimelineOptions options;
  private TimelineItemProvider provider;
  private final TimelineServerRpc serverRpc = new ServerRpcImpl();
  private final TimelineClientRpc clientRpc;
  protected java.util.Calendar currentCalendar = java.util.Calendar
      .getInstance();
  private Date startDate;
  private Date endDate;
  private boolean itemsDirty;

  /**
   * Constructs the timeline with no caption and an empty item provider.
   */
  public Timeline() {
    this(null, new BasicItemProvider());
  }

  /**
   * Constructs the timeline with no caption.
   *
   * @param provider the provider of timeline items
   */
  public Timeline(TimelineItemProvider provider) {
    this(null, provider);
  }

  /**
   * Constructs the timeline with an empty item provider.
   *
   * @param caption the caption of the component
   */
  public Timeline(String caption) {
    this(caption, new BasicItemProvider());
  }

  /**
   * Constructs the timeline. The timeline will default to showing an 8 hour
   * period starting "now" unless {@link #setWindow(java.util.Date, java.util.Date, org.mpilone.vaadin.timeline.TimelineMethodOptions.SetWindow)
   * } is called.
   *
   * @param caption the caption of the component
   * @param provider the provider of timeline items
   */
  public Timeline(String caption, TimelineItemProvider provider) {
    setWidth("100%");
    setCaption(caption);
    setItemProvider(provider);

    registerRpc(serverRpc);
    clientRpc = getRpcProxy(TimelineClientRpc.class);

    itemIdMapper = new KeyMapper<>();
    selection = new HashSet<>();
    options = new StateMappingOptions(this);

    Date start = currentCalendar.getTime();
    currentCalendar.add(java.util.Calendar.HOUR, 8);
    Date end = currentCalendar.getTime();
    setWindow(start, end, null);
  }

  /**
   * Adds the listener to receive single left-click events on the timeline.
   *
   * @param listener the listener to add
   */
  public void addClickListener(ClickListener listener) {
    addListener(ClickEvent.class, listener, ClickListener.METHOD);
  }

  /**
   * Removes the listener to receive single left-click events on the timeline.
   *
   * @param listener the listener to remove
   */
  public void removeClickListener(ClickListener listener) {
    removeListener(ClickEvent.class, listener, ClickListener.METHOD);
  }

  /**
   * Adds the listener to receive double left-click events on the timeline.
   *
   * @param listener the listener to add
   */
  public void addDoubleClickListener(DoubleClickListener listener) {
    addListener(DoubleClickEvent.class, listener, DoubleClickListener.METHOD);
  }

  /**
   * Removes the listener to receive double left-click events on the timeline.
   *
   * @param listener the listener to remove
   */
  public void removeClickListener(DoubleClickListener listener) {
    removeListener(DoubleClickEvent.class, listener, DoubleClickListener.METHOD);
  }

  /**
   * Adds the listener to receive right-click events on the timeline.
   *
   * @param listener the listener to add
   */
  public void addContextMenuListener(ContextMenuListener listener) {
    addListener(ContextMenuEvent.class, listener, ContextMenuListener.METHOD);
  }

  /**
   * Removes the listener to receive right-click events on the timeline.
   *
   * @param listener the listener to remove
   */
  public void removeContextMenuListener(ContextMenuListener listener) {
    removeListener(ContextMenuEvent.class, listener, ContextMenuListener.METHOD);
  }

  /**
   * Returns the start date of the visible window. The date may be different
   * from the date set with the last call to {@link #setWindow(java.util.Date, java.util.Date, org.mpilone.vaadin.timeline.TimelineMethodOptions.SetWindow)
   * } if the data or options causes a different window to be visible.
   *
   * @return the window start date
   */
  public Date getWindowStart() {
    return startDate;
  }

  /**
   * Returns the end date of the visible window. The date may be different from
   * the date set with the last call to {@link #setWindow(java.util.Date, java.util.Date, org.mpilone.vaadin.timeline.TimelineMethodOptions.SetWindow)
   * } if the data or options causes a different window to be visible.
   *
   * @return the window end date
   */
  public Date getWindowEnd() {
    return endDate;
  }

  /**
   * Set the {@link TimelineItemProvider} to be used with this timeline. The
   * provider is used to query for items to show. By default a
   * {@link BasicItemProvider} is used.
   *
   * @param provider the provider to set. Cannot be null
   */
  public void setItemProvider(TimelineItemProvider provider) {
    if (provider == null) {
      provider = new BasicItemProvider();
    }

    if (provider != this.provider) {
      // remove old listener
      if (getItemProvider() instanceof TimelineItemProvider.ItemSetChangeNotifier) {
        ((TimelineItemProvider.ItemSetChangeNotifier) getItemProvider())
            .removeItemSetChangeListener(this);
      }

      this.provider = provider;

      // add new listener
      if (provider instanceof TimelineItemProvider.ItemSetChangeNotifier) {
        ((TimelineItemProvider.ItemSetChangeNotifier) provider)
            .addItemSetChangeListener(this);
      }
    }

    markItemsAsDirty();
  }

  /**
   * Marks the items as dirty which causes them to be sent to the client.
   */
  private void markItemsAsDirty() {
    itemsDirty = true;
    markAsDirty();
  }

  /**
   * Sets the groups used to group together items with the same group ID
   * reference into rows.
   *
   * @param groups the groups to set or null to clear the groups
   */
  public void setGroups(List<TimelineGroup> groups) {
    this.groups = groups;

    List<TimelineState.Group> stateGroups = new ArrayList<>();
    if (groups != null) {
      for (TimelineGroup g : groups) {
        TimelineState.Group group = new TimelineState.Group();
        group.className = g.getStyleName() == null ? "" : g.getStyleName();
        group.content = g.getContent();
        group.id = g.getId();
        stateGroups.add(group);
      }
    }
    getState().groups = stateGroups;
  }

  /**
   * Returns the groups used to group together items with the same group ID.
   *
   * @return the groups or null if none have been specified
   */
  public List<TimelineGroup> getGroups() {
    return groups;
  }

  /**
   * Returns the event provider current in use.
   *
   * @return the {@link TimelineItemProvider} currently in use
   */
  public TimelineItemProvider getItemProvider() {
    return provider;
  }

  /**
   * Deselects all selected items. This is a simple convenience method for
   * calling {@link #setSelection(java.util.Collection, org.mpilone.vaadin.timeline.TimelineMethodOptions.SetSelection)
   * } with an empty list. A {@link SelectListener.SelectEvent} will be fired if
   * items are deselected.
   *
   * @param options the method options
   */
  public void deselectAll(TimelineMethodOptions.SetSelection options) {
    setSelection(Collections.emptySet(), options);
  }

  /**
   * Selects the items with given IDs. A {@link SelectListener.SelectEvent} will
   * be fired if items are selected. That is, if the items exist and are not
   * selected.
   *
   * @param itemIds the IDs of the items to setSelection
   * @param options the method options
   */
  public void setSelection(Collection<Object> itemIds,
      TimelineMethodOptions.SetSelection options) {
    if (itemIds == null) {
      itemIds = Collections.emptySet();
    }

    // Convert the item IDs into the timeline item keys.
    String[] keys = new String[itemIds.size()];
    int i = 0;
    for (Object itemId : itemIds) {
      keys[i++] = itemIdMapper.key(itemId);
    }

    TimelineClientRpc.MethodOptions.SetSelection rpcOptions =
        new TimelineClientRpc.MethodOptions.SetSelection();
    if (options != null) {
      rpcOptions.animation = TimelineMethodOptions.map(options.getAnimation());
      rpcOptions.focus = options.isFocus();
    }

    clientRpc.setSelection(keys, rpcOptions);
  }

  /**
   * Deselects the items with given IDs. This is a simple convenience method for
   * calling {@link #setSelection(java.util.Collection, org.mpilone.vaadin.timeline.TimelineMethodOptions.SetSelection)
   * } with the items removed from the current selection. A
   * {@link SelectListener.SelectEvent} will be fired if items are deselected.
   * That is, if the items exist and were selected).
   *
   * @param itemIds the ID of the items to deselect
   * @param options the method options
   */
  public void deselect(Collection<Object> itemIds,
      TimelineMethodOptions.SetSelection options) {
    Set<Object> newSelection = new HashSet<>(getSelection());
    newSelection.removeAll(itemIds);

    setSelection(newSelection, options);
  }

  /**
   * Returns true if the given item ID is selected.
   *
   * @param itemId the ID of the item to check
   *
   * @return true if selected, false if not selected or not found
   */
  public boolean isSelected(Object itemId) {
    return getSelection().contains(itemId);
  }

  /**
   * Returns an unmodifiable collection of item IDs that are selected or an
   * empty collection if nothing is selected.
   *
   * @return the collection of selected item IDs
   */
  public Collection<Object> getSelection() {
    return Collections.unmodifiableCollection(selection);
  }

  /**
   * Returns the current configuration options for the timeline. The options
   * returned can be modified and are effective immediately.
   *
   * @return the configuration options for the timeline
   */
  public TimelineOptions getOptions() {
    return options;
  }

  @Override
  protected TimelineState getState() {
    return (TimelineState) super.getState();
  }

  @Override
  protected TimelineState getState(boolean markAsDirty) {
    return (TimelineState) super.getState(markAsDirty);
  }

  @Override
  public void beforeClientResponse(boolean initial) {
    super.beforeClientResponse(initial);

    if (itemsDirty) {
      setupStateItems();
      itemsDirty = false;
    }
  }

  @Override
  public void itemSetChange(
      TimelineItemProvider.ItemSetChangeEvent changeEvent) {
    // sanity check
    if (provider == changeEvent.getSource()) {
      markItemsAsDirty();
    }
  }

  /**
   * Sets up the items to be returned to the client in the timeline state.
   */
  private void setupStateItems() {
    items = getItemProvider().getItems(startDate, endDate);

    itemIdMapper.removeAll();

    List<TimelineState.Item> stateItems = new ArrayList<>();
    if (items != null) {
      for (TimelineItem item : items) {
        TimelineState.Item i = new TimelineState.Item();
        i.id = itemIdMapper.key(item.getId());
        i.content = item.getContent() == null ? "" : item.getContent();
        i.start = item.getStart().getTime();
        i.end = item.getEnd().getTime();
        i.className = item.getStyleName() == null ? "" : item
            .getStyleName();
        i.group = item.getGroupId();
        i.type = item.getType() == null ? null : item.getType().name().
            toLowerCase();
        i.title = item.getTitle();
        i.editable = item.getEditable();

        stateItems.add(i);
      }
    }
    getState().items = stateItems;
  }

  /**
   * Sets the visible range (zoom) to the specified range. Accepts two
   * parameters of type Date that represent the first and last times of the
   * wanted selected visible range.
   *
   * @param start the start date
   * @param end the end date
   * @param options the options for the method
   */
  public void setWindow(Date start, Date end,
      TimelineMethodOptions.SetWindow options) {
    if (!Objects.equals(this.startDate, start) || !Objects.
        equals(this.endDate, end)) {
      this.startDate = start;
      this.endDate = end;

      TimelineClientRpc.MethodOptions.SetWindow rpcOptions =
          new TimelineClientRpc.MethodOptions.SetWindow();
      if (options != null) {
        rpcOptions.animation = TimelineMethodOptions.map(options.getAnimation());
      }

      clientRpc.setWindow(start.getTime(), end.getTime(), rpcOptions);
    }
  }

  /**
   * Adjust the visible window such that it fits all items.
   *
   * @param options the options for the method
   */
  public void fit(TimelineMethodOptions.Fit options) {
    TimelineClientRpc.MethodOptions.Fit rpcOptions =
        new TimelineClientRpc.MethodOptions.Fit();
    if (options != null) {
      rpcOptions.animation = TimelineMethodOptions.map(options.getAnimation());
    }

    clientRpc.fit(rpcOptions);
  }

  /**
   * Move the window such that given time is centered on screen.
   *
   * @param time the time to move to
   * @param options the options for the method
   */
  public void moveTo(Date time, TimelineMethodOptions.MoveTo options) {
    TimelineClientRpc.MethodOptions.MoveTo rpcOptions =
        new TimelineClientRpc.MethodOptions.MoveTo();

    if (options != null) {
      rpcOptions.animation = TimelineMethodOptions.map(options.getAnimation());
    }

    clientRpc.moveTo(time.getTime(), rpcOptions);
  }

  /**
   * Adjust the visible window such that the selected item (or multiple items)
   * are centered on screen.
   *
   * @param itemIds the ids of the items to focus on
   * @param options the options for the method
   */
  public void focus(Collection<Object> itemIds,
      TimelineMethodOptions.Fit options) {

    if (itemIds == null) {
      itemIds = Collections.emptySet();
    }

    // Convert the item IDs into the timeline item keys.
    String[] keys = new String[itemIds.size()];
    int i = 0;
    for (Object itemId : itemIds) {
      keys[i++] = itemIdMapper.key(itemId);
    }

    TimelineClientRpc.MethodOptions.Focus rpcOptions =
        new TimelineClientRpc.MethodOptions.Focus();
    if (options != null) {
      rpcOptions.animation = TimelineMethodOptions.map(options.getAnimation());
    }

    clientRpc.focus(keys, rpcOptions);
  }

  /**
   * Add new vertical bar representing a custom time that can be dragged by the
   * user.
   *
   * @param time the current time to set
   * @param id the id of the custom time bar to modify
   */
  public void addCustomTime(Date time, String id) {
    clientRpc.addCustomTime(time.getTime(), id);
  }

  /**
   * Remove vertical bars previously added to the timeline via addCustomTime
   * method.
   *
   * @param id the id of the custom time bar to remove
   */
  public void removeCustomTime(String id) {
    clientRpc.removeCustomTime(id);
  }

  /**
   * Adjust the custom time of the timeline. Adjust the custom time bar. Only
   * applicable when the option showCustomTime is true.
   *
   * @param time the current time to set
   * @param id the id of the custom time bar to modify
   */
  public void setCustomTime(Date time, String id) {
    clientRpc.setCustomTime(time.getTime(), id);
  }

  /**
   * Adjust the current time of the time bar on the timeline. This can be used
   * for example to ensure that a client's time is synchronized with a shared
   * server time.
   *
   * @param time the current time to set
   */
  public void setCurrentTime(Date time) {
    clientRpc.setCurrentTime(time.getTime());
  }

  /**
   * Sets a container as a data source for the items in the timeline. This is a
   * convenience method for doing
   * {@code Timeline.setItemProvider(new ContainerItemProvider(container))}. Use
   * this method if you are adding a container which uses the default property
   * IDs and cannot support the sorting optimization of the
   * {@link ContainerItemProvider}. If you are using custom properties or
   * sorting, use
   * {@link #setItemProvider(org.mpilone.vaadin.timeline.TimelineItemProvider)}
   * with a configured {@link ContainerItemProvider}.
   *
   * @param container the container to use as a datasource
   */
  public void setContainerDataSource(Container container) {
    setItemProvider(new ContainerItemProvider(container));
  }

  @Override
  public List<TimelineItem> getItems(Date startDate, Date endDate) {
    return getItemProvider().getItems(startDate, endDate);
  }

  @Override
  public void addItem(TimelineItem item) {
    if (getItemProvider() instanceof TimelineItemProvider.Editable) {
      TimelineItemProvider.Editable p =
          (TimelineItemProvider.Editable) getItemProvider();
      p.addItem(item);
      markAsDirty();
    }
    else {
      throw new UnsupportedOperationException(
          "Item provider does not support adding events");
    }
  }

  @Override
  public void removeItem(TimelineItem item) {
    if (getItemProvider() instanceof TimelineItemProvider.Editable) {
      TimelineItemProvider.Editable p =
          (TimelineItemProvider.Editable) getItemProvider();
      p.removeItem(item);
      markAsDirty();
    }
    else {
      throw new UnsupportedOperationException(
          "Event provider does not support removing events");
    }
  }

  /**
   * Adds the given listener for {@link SelectListener.SelectEvent} items.
   *
   * @param listener the listener to add
   */
  public void addSelectListener(SelectListener listener) {
    addListener(SelectListener.SelectEvent.class, listener,
        SelectListener.SELECTION_CHANGE_METHOD);
  }

  /**
   * Adds the given listener for {@link SelectListener.SelectEvent} items.
   *
   * @param listener the listener to add
   */
  public void removeSelectListener(SelectListener listener) {
    removeListener(SelectListener.SelectEvent.class, listener,
        SelectListener.SELECTION_CHANGE_METHOD);
  }

  /**
   * Adds the given listener for
   * {@link RangeChangedListener.RangeChangedEvent}s.
   *
   * @param listener the listener to add
   */
  public void addRangeChangedListener(RangeChangedListener listener) {
    addListener(RangeChangedListener.RangeChangedEvent.class, listener,
        RangeChangedListener.METHOD);
  }

  /**
   * Adds the given listener for
   * {@link RangeChangedListener.RangeChangedEvent}s.
   *
   * @param listener the listener to add
   */
  public void removeRangeChangedListener(
      RangeChangedListener listener) {
    removeListener(RangeChangedListener.RangeChangedEvent.class,
        listener, RangeChangedListener.METHOD);
  }

  /**
   * Implementation of the server RPC for the timeline component.
   */
  private class ServerRpcImpl implements TimelineServerRpc {

    @Override
    public void ackSetCurrentTime() {
      // TODO: Implement lag calculation and adjustment
    }

    @Override
    public void rangeChanged(long start, long end, boolean byUser) {
      Date startDate = new Date(start);
      Date endDate = new Date(end);

      if (byUser) {
        Timeline.this.startDate = startDate;
        Timeline.this.endDate = endDate;

        // Mark the timeline as dirty so we fetch new items from the provider
        // and send them back to the client.
        Timeline.this.markItemsAsDirty();
      }

      RangeChangedListener.RangeChangedEvent evt =
          new RangeChangedListener.RangeChangedEvent(Timeline.this,
              startDate, endDate, byUser);
      fireEvent(evt);
    }

    @Override
    public void select(List<String> clientKeys) {

      Set<Object> itemIds = new HashSet<>();

      for (String clientKey : clientKeys) {
        itemIds.add(itemIdMapper.get(clientKey));
      }

      // Apply the new selection internally.
      Set<Object> oldSelection = selection;
      selection = new HashSet<>(itemIds);

      // Only fire the event if the selection actually changed. This is more
      // consistent with Vaadin components.
      if (!itemIds.equals(oldSelection)) {
        SelectListener.SelectEvent evt =
            new SelectListener.SelectEvent(Timeline.this, itemIds);

        fireEvent(evt);
      }
    }

    @Override
    public void click(EventProperties eventProps) {
      Object itemId = eventProps.item == null ? null : itemIdMapper.get(
          eventProps.item);

      fireEvent(new ClickEvent(Timeline.this, itemId, eventProps));
    }

    @Override
    public void doubleClick(EventProperties eventProps) {
      Object itemId = eventProps.item == null ? null : itemIdMapper.get(
          eventProps.item);

      fireEvent(new DoubleClickEvent(Timeline.this, itemId, eventProps));
    }

    @Override
    public void contextmenu(EventProperties eventProps) {
      Object itemId = eventProps.item == null ? null : itemIdMapper.get(
          eventProps.item);

      fireEvent(new ContextMenuEvent(Timeline.this, itemId, eventProps));
    }
  }

}
