package org.mpilone.vaadin.timeline;

import java.util.*;

import org.mpilone.vaadin.timeline.ClickListener.ClickEvent;
import org.mpilone.vaadin.timeline.ContextMenuListener.ContextMenuEvent;
import org.mpilone.vaadin.timeline.DoubleClickListener.DoubleClickEvent;
import org.mpilone.vaadin.timeline.shared.*;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractJavaScriptComponent;

/**
 * <p>
 * An implementation of the vis.js Timeline component (http://visjs.org/). The
 * timeline displays {@link TimelineItem}s provided by a
 * {@link TimelineItemProvider} on a scrollable and zoomable interface.
 * </p>
 * <p>
 * Note that unlike other components, the internal state of the timeline, such
 * as selected items and the window range, is only updated via events from the
 * client. This is required because the client implementation has the complex
 * logic to calculate the window range taking into account the configuration
 * options like zoom and min/max as well as the items.
 * </p>
 *
 * @author mpilone
 */
@StyleSheet({"vis/dist/vis.min.css", "vis/dist/img/timeline/delete.png"})
@JavaScript({"timeline_connector.js", "vis/dist/vis-timeline-graph2d.min.js"})
public class Timeline extends AbstractJavaScriptComponent implements
    TimelineItemProvider, TimelineItemProvider.Editable,
    TimelineItemProvider.ItemSetChangeListener {

  private List<TimelineItem> items;
  private Set<Object> selection;
  private DataProviderKeyMapper keyMapper;
  private List<TimelineGroup> groups;
  private TimelineOptions options;
  private TimelineItemProvider provider;
  private final TimelineServerRpc serverRpc = new ServerRpcImpl();
  private final TimelineClientRpc clientRpc;
  protected java.util.Calendar currentCalendar = java.util.Calendar
      .getInstance();
  private DateRange window;

  private boolean itemsDirty;
  private boolean windowDirty;
  private boolean groupsDirty;
  private DateRange pendingWindow;
  private TimelineMethodOptions.SetWindow pendingSetWindowOptions;

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

    keyMapper = new DataProviderKeyMapper();
    selection = new HashSet<>();
    options = new StateMappingOptions(this);
    groups = new ArrayList<>();
    window = new DateRange(new Date(0), new Date(0));
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
   * Returns the date range of the visible window. The date may be different
   * from the date set with the last call to {@link #setWindow(java.util.Date, java.util.Date, org.mpilone.vaadin.timeline.TimelineMethodOptions.SetWindow)
   * } because the range is based on what is actually visible to the user based
   * on calculations performed on the client side. It is possible that the
   * window range is null if the timeline has never been displayed on the client
   * side.
   *
   * @return the window date range or null
   */
  public DateRange getWindow() {
    return window;
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

      markItemsAsDirty();
    }
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
    this.groups = groups == null ? new ArrayList<TimelineGroup>() : groups;

    groupsDirty = true;
    markAsDirty();
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
    List<String> keyList = keyMapper.getKeys(itemIds);
    String[] keys = new String[keyList.size()];
    keyList.toArray(keys);

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

    boolean windowValid = !window.getStart().equals(window.getEnd()) || window.
        getStart().after(window.getEnd());
    boolean restoring = false;

    if (initial && windowValid) {
      // Normally this happens when the timeline was previously displayed 
      // and hidden. We basically want to restore the state on this initial
      // display.
      restoring = true;

      if (!windowDirty) {
        // Reapply the existing window so it can be pushed back to the client.
        setWindow(window.getStart(), window.getEnd(), null);
      }
    }
    else if (initial && !windowDirty) {
      // The window is not dirty and the current window is not valid.
      // This means we have to initialize the window for the first time
      // to some intelligent default.
      Date start = currentCalendar.getTime();
      currentCalendar.add(java.util.Calendar.HOUR, 8);
      Date end = currentCalendar.getTime();
      setWindow(start, end, null);
    }

    if (restoring) {
      // Send everything to the client. We know we have a valid window so
      // we can send items and if the window hasn't changed we won't get a
      // range change event so it is now or never. If the window did change,
      // we will get a range change but that will be handled properly
      // and we will resend the items in the next request. So there is a chance
      // of sending the items twice but we have to assume the window
      // didn't change.
      sendGroupsToClient();
      sendItemsToClient();
      sendWindowToClient();
    }
    else {
      if (groupsDirty) {
        sendGroupsToClient();
      }

      if (windowDirty) {
        // Send the dirty window which will trigger a range change event
        // for the items later.
        sendWindowToClient();
      }
      else if (itemsDirty && windowValid) {
        // The window isn't dirty so we're not expecting a range change
        // event. If we have a valid window, go ahead and send the items.
        // Because of the delay from sending the window to the client and
        // getting the range change event, we can get into the state of the
        // window not being dirty (because we sent it) but the window not
        // being valid yet (because we didn't get the range change event).
        // We don't want to send the items yet in this state because we'd
        // query the item provider with an invalid range.
        sendItemsToClient();
      }
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
   * Sets up the groups to be sent to the client and makes the RPC call to send
   * them.
   */
  private void sendGroupsToClient() {
    TimelineClientRpc.Group[] rpcGroups = new TimelineClientRpc.Group[groups
        .size()];
    int i = 0;
    for (TimelineGroup g : groups) {
      TimelineClientRpc.Group group = new TimelineClientRpc.Group();
      group.className = g.getStyleName() == null ? "" : g.getStyleName();
      group.content = g.getContent();
      group.id = g.getId();
      group.order = g.getOrder();
      group.style = g.getStyle();
      group.subgroupOrder = g.getSubgroupOrder();
      group.title = g.getTitle();
      rpcGroups[i++] = group;
    }
    clientRpc.setGroups(rpcGroups);

    groupsDirty = false;
  }

  /**
   * Sets up the items to be sent to the client and makes the RPC call to send
   * them.
   */
  private void sendItemsToClient() {
    items = getItemProvider().getItems(window.getStart(), window.getEnd());
    items = items == null ? new ArrayList<TimelineItem>() : items;

    List<Object> itemIds = new ArrayList<>(items.size());
    TimelineClientRpc.Item[] rpcItems = new TimelineClientRpc.Item[items.size()];
    int j = 0;
    for (TimelineItem item : items) {

      itemIds.add(item.getId());

      TimelineClientRpc.Item rpcItem = new TimelineClientRpc.Item();
      rpcItem.className = item.getStyleName();
      rpcItem.content = item.getContent() == null ? "" : item.getContent();
      rpcItem.end = item.getEnd().getTime();
      rpcItem.group = item.getGroupId();
      rpcItem.id = keyMapper.getKey(item.getId());
      rpcItem.start = item.getStart().getTime();
      rpcItem.style = item.getStyle();
      rpcItem.subgroup = item.getSubgroupId();
      rpcItem.title = item.getTitle();
      rpcItem.type = item.getType() == null ? null : item.getType().name().
          toLowerCase();
      rpcItem.editable = item.getEditable();

      rpcItems[j++] = rpcItem;
    }

    // Cleanup any id mappings that are no longer active or pinned.
    keyMapper.setActiveRows(itemIds);

    // Set the items.
    clientRpc.setItems(rpcItems);

    // Clear the dirty flag now that we sent the items.
    itemsDirty = false;

    // Reapply the selection in the event that a selected item move out of
    // the window and back in again.
    setSelection(selection, new TimelineMethodOptions.SetSelection(false,
        null));
  }

  /**
   * <p>
   * Sets the visible range (zoom) to the specified range. Accepts two
   * parameters of type Date that represent the first and last times of the
   * wanted selected visible range.
   * </p>
   * <p>
   * Note that the window range is sent to the client and the internal state of
   * the timeline window is not modified until the client generates a range
   * change event. This is required because the client may modify the window
   * based on zoom, min/max, scaling, etc.
   * </p>
   *
   * @param start the start date
   * @param end the end date
   * @param options the options for the method
   */
  public void setWindow(Date start, Date end,
      TimelineMethodOptions.SetWindow options) {

    Objects.requireNonNull(start, "start cannot be null");
    Objects.requireNonNull(end, "end cannot be null");
    if (start.after(end) || start.equals(end)) {
      throw new IllegalArgumentException("start date must be before end date.");
    }

    pendingWindow = new DateRange(start, end);
    pendingSetWindowOptions = options;

    //if (windowDirty || !Objects.equals(window, pendingWindow)) {
      windowDirty = true;
      markAsDirty();
    //}
  }

  /**
   * Sends the pending window to the client and clears the dirty state of the
   * window.
   */
  private void sendWindowToClient() {
    // Send the request to the client. We don't update the internal
    // window range until we get the range change event from the
    // client because the actual range may be different because of
    // the items, zoom level, min/max, etc.
    TimelineClientRpc.MethodOptions.SetWindow rpcOptions =
        new TimelineClientRpc.MethodOptions.SetWindow();
    if (pendingSetWindowOptions != null) {
      rpcOptions.animation = TimelineMethodOptions.map(pendingSetWindowOptions.
          getAnimation());
    }

    clientRpc.setWindow(pendingWindow.getStart().getTime(), pendingWindow.
        getEnd().getTime(), rpcOptions);

    windowDirty = false;
    pendingSetWindowOptions = null;
    pendingWindow = null;
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
    List<String> keyList = keyMapper.getKeys(itemIds);
    String[] keys = new String[keyList.size()];
    keyList.toArray(keys);

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
  //
  // @Override
  // public boolean isReadOnly() {
  // return super.isReadOnly();
  // }
  //
  // @Override
  // public void setReadOnly(boolean readOnly) {
  // super.setReadOnly(readOnly);
  // }

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
      DateRange newWindow = new DateRange(new Date(start), new Date(end));

      // Only mark the items dirty and fire the event if the range
      // actually changed.
      if (!Objects.equals(newWindow, window)) {
        Timeline.this.window = newWindow;

        // Mark the timeline as dirty so we fetch new items from the provider
        // and send them back to the client.
        Timeline.this.markItemsAsDirty();

        RangeChangedListener.RangeChangedEvent evt =
            new RangeChangedListener.RangeChangedEvent(Timeline.this,
                window.getStart(), window.getEnd(), byUser);
        fireEvent(evt);
      }
    }

    @Override
    public void select(List<String> clientKeys) {

      // Convert the keys back into item IDs.
      Set<Object> newSelection = new HashSet<>(keyMapper.getItemIds(
          clientKeys));

      // Unpin items no longer selected and pin items newly selected.
      for (Object itemId : newSelection) {
        if (keyMapper.isPinned(itemId) && !selection.contains(itemId)) {
          keyMapper.unpin(itemId);
        }
        else if (!keyMapper.isPinned(itemId)) {
          keyMapper.pin(itemId);
        }
      }

      // Apply the new selection internally.
      Set<Object> oldSelection = selection;
      selection = newSelection;

      // Only fire the event if the selection actually changed. This is more
      // consistent with Vaadin components.
      if (!selection.equals(oldSelection)) {
        SelectListener.SelectEvent evt = new SelectListener.SelectEvent(
            Timeline.this, selection);

        fireEvent(evt);
      }
    }

    @Override
    public void click(EventProperties eventProps) {
      Object itemId = eventProps.item == null ? null : keyMapper.getItemId(
          eventProps.item);

      fireEvent(new ClickEvent(Timeline.this, itemId, eventProps));
    }

    @Override
    public void doubleClick(EventProperties eventProps) {
      Object itemId = eventProps.item == null ? null : keyMapper.getItemId(
          eventProps.item);

      fireEvent(new DoubleClickEvent(Timeline.this, itemId, eventProps));
    }

    @Override
    public void contextmenu(EventProperties eventProps) {
      Object itemId = eventProps.item == null ? null : keyMapper.getItemId(
          eventProps.item);

      fireEvent(new ContextMenuEvent(Timeline.this, itemId, eventProps));
    }
  }

}
