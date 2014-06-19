package org.mpilone.vaadin.timeline;

import java.util.*;

import org.mpilone.vaadin.timeline.shared.*;

import com.vaadin.annotations.*;
import com.vaadin.data.Container;
import com.vaadin.server.KeyMapper;
import com.vaadin.ui.AbstractJavaScriptComponent;

/**
 * An implementation of the vis.js Timeline component (http://visjs.org/). The
 * timeline displays {@link TimelineItem}s provided by a
 * {@link TimelineItemProvider} on a scrollable and zoomable interface.
 *
 * @author mpilone
 */
@StyleSheet("vis/dist/vis.min.css")
@JavaScript({"timeline_connector.js", "vis/dist/vis.js"})
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
   * period starting "now" unless {@link #setWindow(java.util.Date, java.util.Date)
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
    setWindow(start, end);
  }

  /**
   * Returns the start date of the visible window. The date may be different
   * from the date set with the last call to {@link #setWindow(java.util.Date, java.util.Date)
   * } if the data or options causes a different window to be visible.
   *
   * @return the window start date
   */
  public Date getWindowStart() {
    return startDate;
  }

  /**
   * Returns the end date of the visible window. The date may be different from
   * the date set with the last call to {@link #setWindow(java.util.Date, java.util.Date)
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
   * Deselects all selected items. A
   * {@link SelectionChangeListener.SelectionChangeEvent} will be fired if items
   * are deselected.
   */
  public void deselectAll() {
    // TODO
  }

  /**
   * Selects the items with given IDs. A
   * {@link SelectionChangeListener.SelectionChangeEvent} will be fired if items
   * are selected. That is, if the items exist and are not selected).
   *
   * @param itemIds the IDs of the items to select
   */
  public void select(Collection<Object> itemIds) {
    // TODO
  }

  /**
   * Selects the items with given IDs. A
   * {@link SelectionChangeListener.SelectionChangeEvent} will be fired if items
   * are selected. That is, if the items exist and are not selected).
   *
   * @param itemId the ID of the items to select
   */
  public void select(Object... itemId) {
    // TODO
  }

  /**
   * Deselects the items with given IDs. A
   * {@link SelectionChangeListener.SelectionChangeEvent} will be fired if items
   * are deselected. That is, if the items exist and were selected).
   *
   * @param itemId the ID of the items to deselect
   */
  public void deselect(Object... itemId) {
    // TODO
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

    setupStateItems();
  }

  @Override
  public void itemSetChange(
      TimelineItemProvider.ItemSetChangeEvent changeEvent) {
    // sanity check
    if (provider == changeEvent.getSource()) {
      markAsDirty();
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
   */
  public void setWindow(Date start, Date end) {
    if (!Objects.equals(this.startDate, start) || !Objects.
        equals(this.endDate, end)) {
      this.startDate = start;
      this.endDate = end;

      clientRpc.setWindow(start.getTime(), end.getTime());
    }
  }

  /**
   * Adjust the custom time of the timeline. Adjust the custom time bar. Only
   * applicable when the option showCustomTime is true.
   *
   * @param time the current time to set
   */
  public void setCustomTime(Date time) {
    clientRpc.setCustomTime(time.getTime());
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
      TimelineItemProvider.Editable provider =
          (TimelineItemProvider.Editable) getItemProvider();
      provider.addItem(item);
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
      TimelineItemProvider.Editable provider =
          (TimelineItemProvider.Editable) getItemProvider();
      provider.removeItem(item);
      markAsDirty();
    }
    else {
      throw new UnsupportedOperationException(
          "Event provider does not support removing events");
    }
  }

  /**
   * Adds the given listener for
   * {@link SelectionChangeListener.SelectionChangeEvent} items.
   *
   * @param listener the listener to add
   */
  public void addSelectionChangeListener(SelectionChangeListener listener) {
    addListener(SelectionChangeListener.SelectionChangeEvent.class, listener,
        SelectionChangeListener.SELECTION_CHANGE_METHOD);
  }

  /**
   * Adds the given listener for
   * {@link SelectionChangeListener.SelectionChangeEvent} items.
   *
   * @param listener the listener to add
   */
  public void removeEventSelectListener(SelectionChangeListener listener) {
    removeListener(SelectionChangeListener.SelectionChangeEvent.class, listener,
        SelectionChangeListener.SELECTION_CHANGE_METHOD);
  }

  /**
   * Adds the given listener for
   * {@link WindowRangeChangeListener.WindowRangeChangeEvent} items.
   *
   * @param listener the listener to add
   */
  public void addWindowRangeChangeListener(WindowRangeChangeListener listener) {
    addListener(WindowRangeChangeListener.WindowRangeChangeEvent.class, listener,
        WindowRangeChangeListener.WINDOW_RANGE_CHANGE_METHOD);
  }

  /**
   * Adds the given listener for
   * {@link WindowRangeChangeListener.WindowRangeChangeEvent} items.
   *
   * @param listener the listener to add
   */
  public void removeWindowChangeListenerListener(
      WindowRangeChangeListener listener) {
    removeListener(WindowRangeChangeListener.WindowRangeChangeEvent.class,
        listener, WindowRangeChangeListener.WINDOW_RANGE_CHANGE_METHOD);
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
    public void rangeChanged(long start, long end) {
      Date startDate = new Date(start);
      Date endDate = new Date(end);

      if (!Objects.equals(Timeline.this.startDate, startDate) || !Objects.
          equals(
              Timeline.this.endDate, endDate)) {
        Timeline.this.startDate = startDate;
        Timeline.this.endDate = endDate;

        Timeline.this.markAsDirty();

        WindowRangeChangeListener.WindowRangeChangeEvent evt =
            new WindowRangeChangeListener.WindowRangeChangeEvent(Timeline.this,
                startDate, endDate);
        fireEvent(evt);
      }
    }

    @Override
    public void select(List<String> clientKeys) {

      Set<Object> newSelection = new HashSet<>();

      for (String clientKey : clientKeys) {
        newSelection.add(itemIdMapper.get(clientKey));
      }

      if (!newSelection.equals(selection)) {
        SelectionChangeListener.SelectionChangeEvent evt =
            new SelectionChangeListener.SelectionChangeEvent(Timeline.this,
                selection,
                newSelection);

        selection = new HashSet<>(newSelection);
        fireEvent(evt);
      }
      else {
        selection = new HashSet<>(newSelection);
      }
    }
  }
}
