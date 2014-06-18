package org.mpilone.vaadin.timeline;

import java.lang.reflect.Method;
import java.util.*;

import org.mpilone.vaadin.timeline.shared.*;

import com.vaadin.annotations.*;
import com.vaadin.server.KeyMapper;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.components.calendar.event.*;

/**
 * An implementation of the vis.js Timeline component (http://visjs.org/).
 *
 * @author mpilone
 */
@StyleSheet("vis/dist/vis.min.css")
@JavaScript({"timeline_connector.js", "vis/dist/vis.js"})
public class Timeline extends AbstractJavaScriptComponent implements
    TimelineItemProvider, TimelineItemProvider.Editable,
    TimelineItemProvider.ItemSetChangeListener {

  /**
   * The handler method to fire on the {@link SelectionChangeListener}.
   */
  static final Method SELECTION_CHANGE_METHOD;

  /**
   * The handler method to fire on the {@link WindowRangeChangeListener}.
   */
  static final Method WINDOW_RANGE_CHANGE_METHOD;

  static {
    try {
      SELECTION_CHANGE_METHOD = SelectionChangeListener.class.getMethod(
          "selectionChange",
          SelectionChangeListener.SelectionChangeEvent.class);
      WINDOW_RANGE_CHANGE_METHOD = WindowRangeChangeListener.class.getMethod(
          "windowRangeChange",
          WindowRangeChangeListener.WindowRangeChangeEvent.class);
    }
    catch (NoSuchMethodException | SecurityException ex) {
      throw new RuntimeException("Unable to find required handler method.", ex);
    }
  }

  private List<TimelineItem> items;
  private HashSet<Object> selection;
  private KeyMapper<Object> itemIdMapper;
  private List<TimelineGroup> groups;
  private TimelineOptions options;

  /**
   * The event provider.
   */
  private TimelineItemProvider timelineItemProvider;

  private final TimelineServerRpc serverRpc = new ServerRpcImpl();

  private final TimelineClientRpc clientRpc;

  /**
   * Internal calendar data source.
   */
  protected java.util.Calendar currentCalendar = java.util.Calendar
      .getInstance();

  private Date startDate;
  private Date endDate;

  public Timeline() {
    this(null, new BasicItemProvider());
  }

  public Timeline(TimelineItemProvider eventProvider) {
    this(null, eventProvider);
  }

  public Timeline(String caption) {
    this(caption, new BasicItemProvider());
  }

  public Timeline(String caption, TimelineItemProvider itemProvider) {
    setWidth("100%");
    setCaption(caption);
    setItemProvider(itemProvider);

    registerRpc(serverRpc);
    clientRpc = getRpcProxy(TimelineClientRpc.class);

    itemIdMapper = new KeyMapper<>();
    selection = new HashSet<>();
    options = new BasicTimelineOptions(this);

    Date start = currentCalendar.getTime();
    currentCalendar.add(java.util.Calendar.HOUR, 8);
    Date end = currentCalendar.getTime();
    setWindow(start, end);
  }

  public Date getStartDate() {
    return startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  /**
   * Set the {@link CalendarEventProvider} to be used with this calendar. The
   * EventProvider is used to query for items to show, and must be non-null. By
   * default a null null {@link BasicEventProvider} is used.
   *
   * @param timelineItemProvider the calendarEventProvider to set. Cannot be
   * null.
   */
  public void setItemProvider(TimelineItemProvider timelineItemProvider) {
    if (timelineItemProvider == null) {
      throw new IllegalArgumentException(
          "Timeline item provider cannot be null");
    }

    // remove old listener
    if (getItemProvider() instanceof TimelineItemProvider.ItemSetChangeNotifier) {
      ((TimelineItemProvider.ItemSetChangeNotifier) getItemProvider())
          .removeItemSetChangeListener(this);
    }

    this.timelineItemProvider = timelineItemProvider;

    // add new listener
    if (timelineItemProvider instanceof TimelineItemProvider.ItemSetChangeNotifier) {
      ((TimelineItemProvider.ItemSetChangeNotifier) timelineItemProvider)
          .addItemSetChangeListener(this);
    }
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
        group.content = g.getCaption();
        group.id = g.getId();
        stateGroups.add(group);
      }
    }
    getState().groups = stateGroups;
  }

  public List<TimelineGroup> getGroups() {
    return groups;
  }

  /**
   * Returns the event provider current in use.
   *
   * @return the {@link TimelineItemProvider} currently in use
   */
  public TimelineItemProvider getItemProvider() {
    return timelineItemProvider;
  }

  public void deselectAll() {
    // TODO
  }

  public void select(Collection<Object> itemIds) {
    // TODO
  }

  public void select(Object... itemId) {
    // TODO
  }

  public void deselect(Object... itemId) {
    // TODO
  }

  public boolean isSelected(Object itemId) {
    return getSelection().contains(itemId);
  }

  public Collection<Object> getSelection() {
    return Collections.unmodifiableCollection(selection);
  }

  /**
   * Returns the current configuration options for the timeline.
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
    if (timelineItemProvider == changeEvent.getProvider()) {
      markAsDirty();
    }
  }

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

//  /**
//   * Sets a container as a data source for the items in the calendar.
//   * Equivalent for doing
//   * {@code Timeline.setItemProvider(new ContainerEventProvider(container))}
//   *
//   * Use this method if you are adding a container which uses the default
//   * property ids like {@link BeanItemContainer} for instance. If you are using
//   * custom properties instead use
//   * {@link Timeline#setContainerDataSource(com.vaadin.data.Container.Indexed, Object, Object, Object, Object, Object)}
//   *
//   * Please note that the container must be sorted by date!
//   *
//   * @param container The container to use as a datasource
//   */
//  public void setContainerDataSource(Container.Indexed container) {
//    ContainerEventProvider provider = new ContainerEventProvider(container);
//
//    setItemProvider(provider);
//  }
//  /**
//   * Sets a container as a data source for the items in the calendar.
//   * Equivalent for doing
//   * <code>Calendar.setItemProvider(new ContainerEventProvider(container))</code>
//   *
//   * Please note that the container must be sorted by date!
//   *
//   * @param container The container to use as a data source
//   * @param captionProperty The property that has the caption, null if no
//   * caption property is present
//   * @param descriptionProperty The property that has the description, null if
//   * no description property is present
//   * @param startDateProperty The property that has the starting date
//   * @param endDateProperty The property that has the ending date
//   * @param styleNameProperty The property that has the stylename, null if no
//   * stylname property is present
//   */
//  public void setContainerDataSource(Container.Indexed container,
//      Object captionProperty, Object descriptionProperty,
//      Object startDateProperty, Object endDateProperty,
//      Object styleNameProperty) {
//    ContainerEventProvider provider = new ContainerEventProvider(container);
//    provider.setCaptionProperty(captionProperty);
//    provider.setDescriptionProperty(descriptionProperty);
//    provider.setStartDateProperty(startDateProperty);
//    provider.setEndDateProperty(endDateProperty);
//    provider.setStyleNameProperty(styleNameProperty);
//
//    setItemProvider(provider);
//  }
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
        SELECTION_CHANGE_METHOD);
  }

  /**
   * Adds the given listener for
   * {@link SelectionChangeListener.SelectionChangeEvent} items.
   *
   * @param listener the listener to add
   */
  public void removeEventSelectListener(SelectionChangeListener listener) {
    removeListener(SelectionChangeListener.SelectionChangeEvent.class, listener,
        SELECTION_CHANGE_METHOD);
  }

  /**
   * Adds the given listener for
   * {@link WindowRangeChangeListener.WindowRangeChangeEvent} items.
   *
   * @param listener the listener to add
   */
  public void addWindowRangeChangeListener(WindowRangeChangeListener listener) {
    addListener(WindowRangeChangeListener.WindowRangeChangeEvent.class, listener,
        WINDOW_RANGE_CHANGE_METHOD);
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
        listener, WINDOW_RANGE_CHANGE_METHOD);
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
