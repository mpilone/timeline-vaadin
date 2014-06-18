package org.mpilone.vaadin.timeline;

import java.util.*;

import org.mpilone.vaadin.timeline.TimelineComponentEvents.EventSelect;
import org.mpilone.vaadin.timeline.TimelineComponentEvents.EventSelectListener;
import org.mpilone.vaadin.timeline.TimelineComponentEvents.VisibleRangeChange;
import org.mpilone.vaadin.timeline.TimelineComponentEvents.VisibleRangeChangeListener;
import org.mpilone.vaadin.timeline.shared.*;

import com.vaadin.annotations.*;
import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.components.calendar.ContainerEventProvider;
import com.vaadin.ui.components.calendar.event.*;

/**
 * An implementation of the vis.js Timeline component (http://visjs.org/).
 *
 * @author mpilone
 */
@StyleSheet("vis/dist/vis.min.css")
@JavaScript({"timeline_connector.js", "vis/dist/vis.js"})
public class Timeline extends AbstractJavaScriptComponent implements
    CalendarEventProvider.EventSetChangeListener,
    CalendarEvent.EventChangeListener,
    CalendarEventProvider,
    CalendarEditableEventProvider {

  /**
   * Internal buffer of events.
   */
  protected List<CalendarEvent> events;

  /**
   * Internal buffer of groups.
   */
  protected List<TimelineGroup> groups;

  /**
   * The event provider.
   */
  private CalendarEventProvider calendarEventProvider;

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
    this(null, new BasicEventProvider());
  }

  public Timeline(CalendarEventProvider eventProvider) {
    this(null, eventProvider);
  }

  public Timeline(String caption) {
    this(caption, new BasicEventProvider());
  }

  public Timeline(String caption, CalendarEventProvider eventProvider) {
    setWidth("100%");
    setCaption(caption);
    setEventProvider(eventProvider);

    registerRpc(serverRpc);
    clientRpc = getRpcProxy(TimelineClientRpc.class);

    Date start = currentCalendar.getTime();
    currentCalendar.add(java.util.Calendar.HOUR, 8);
    Date end = currentCalendar.getTime();
    setWindow(start, end);

//    handlers = new HashMap<String, EventListener>();
//    setDefaultHandlers();
//    currentCalendar.setTime(new Date());
  }

  public void setStartDate(Date date) {
    setWindow(date, endDate);
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setEndDate(Date date) {
    setWindow(startDate, date);
  }

  public Date getEndDate() {
    return endDate;
  }


  /**
   * Set the {@link CalendarEventProvider} to be used with this calendar. The
   * EventProvider is used to query for events to show, and must be non-null. By
   * default a null null {@link BasicEventProvider} is used.
   *
   * @param calendarEventProvider the calendarEventProvider to set. Cannot be
   * null.
   */
  public void setEventProvider(CalendarEventProvider calendarEventProvider) {
    if (calendarEventProvider == null) {
      throw new IllegalArgumentException(
          "Calendar event provider cannot be null");
    }

    // remove old listener
    if (getEventProvider() instanceof CalendarEventProvider.EventSetChangeNotifier) {
      ((CalendarEventProvider.EventSetChangeNotifier) getEventProvider())
          .removeEventSetChangeListener(this);
    }
    if (getEventProvider() instanceof CalendarEvent.EventChangeNotifier) {
      ((CalendarEvent.EventChangeNotifier) getEventProvider())
          .removeEventChangeListener(this);
    }

    this.calendarEventProvider = calendarEventProvider;

    // add new listener
    if (calendarEventProvider instanceof CalendarEventProvider.EventSetChangeNotifier) {
      ((CalendarEventProvider.EventSetChangeNotifier) calendarEventProvider)
          .addEventSetChangeListener(this);
    }
    if (calendarEventProvider instanceof CalendarEvent.EventChangeNotifier) {
      ((CalendarEvent.EventChangeNotifier) calendarEventProvider)
          .addEventChangeListener(this);
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
    markAsDirty();
  }

  /**
   * Returns the event provider current in use.
   *
   * @return the {@link CalendarEventProvider} currently in use
   */
  public CalendarEventProvider getEventProvider() {
    return calendarEventProvider;
  }

  /**
   * If true, the timeline shows a red, vertical line displaying the current
   * time. This time can be synchronized with a server via the method
   * setCurrentTime.
   *
   * @param visible true to enable the vertical time bar, false to disable
   */
  public void setShowCurrentTime(boolean visible) {
    getState().showCurrentTime = visible;
  }

  /**
   * Returns true if the current time is being shown on the client side.
   *
   * @return true if enabled
   */
  public boolean isShowCurrentTime() {
    return getState().showCurrentTime;
  }

  /**
   * Show a vertical bar displaying a custom time. This line can be dragged by
   * the user. The custom time can be utilized to show a state in the past or in
   * the future.
   *
   * @param visible true to enable the vertical time bar, false to disable
   */
  public void setShowCustomTime(boolean visible) {
    getState().showCustomTime = visible;
  }

  /**
   * Returns true if the current time is being shown on the client side.
   *
   * @return true if enabled
   */
  public boolean isShowCustomTime() {
    return getState().showCustomTime;
  }

  @Override
  protected TimelineState getState() {
    return (TimelineState) super.getState();
  }

  @Override
  public void beforeClientResponse(boolean initial) {
    super.beforeClientResponse(initial);

    setupCalendarEvents();
  }

  @Override
  public void eventSetChange(
      CalendarEventProvider.EventSetChangeEvent changeEvent) {
    // sanity check
    if (calendarEventProvider == changeEvent.getProvider()) {
      markAsDirty();
    }
  }

  private void setupCalendarEvents() {
    com.vaadin.ui.Calendar cal;

    events = getEventProvider().getEvents(startDate, endDate);

    List<TimelineState.Event> calendarStateEvents = new ArrayList<>();
    if (events != null) {
      for (int i = 0; i < events.size(); i++) {
        CalendarEvent e = events.get(i);
        TimelineState.Event event = new TimelineState.Event();
        event.index = i;
        event.content = e.getCaption() == null ? "" : e.getCaption();
        event.start = e.getStart().getTime();
        event.end = e.getEnd().getTime();
//                event.timeFrom = df_time.format(e.getStart());
//                event.timeTo = df_time.format(e.getEnd());
//        event.description = e.getDescription() == null ? "" : e
//            .getDescription();
        event.className = e.getStyleName() == null ? "" : e
            .getStyleName();
//        event.allDay = e.isAllDay();

        if (e instanceof TimelineEvent) {
          event.group = ((TimelineEvent) e).getGroup();
        }

        calendarStateEvents.add(event);
      }
    }
    getState().events = calendarStateEvents;

    List<TimelineState.Group> calendarStateGroups = new ArrayList<>();
    if (groups != null) {
      for (TimelineGroup g : groups) {
        TimelineState.Group group = new TimelineState.Group();
        group.className = g.getStyleName() == null ? "" : g.getStyleName();
        group.content = g.getCaption();
        group.id = g.getId();
        calendarStateGroups.add(group);
      }
    }
    getState().groups = calendarStateGroups;
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
   * Set a minimum zoom interval for the visible range in milliseconds. It will
   * not be possible to zoom in further than this minimum.
   *
   * @param zoomMin the minimum zoom in milliseconds
   */
  public void setZoomMin(int zoomMin) {
    getState().zoomMin = zoomMin;
  }

  /**
   * Returns the minimum zoom interval.
   *
   * @return the mimimum zoom in milliseconds
   */
  public int getZoomMin() {
    return getState().zoomMin;
  }

  /**
   * Set a maximum zoom interval for the visible range in milliseconds. It will
   * not be possible to zoom out further than this maximum.
   *
   * @param zoomMax the maximum zoom range
   */
  public void setZoomMax(int zoomMax) {
    getState().zoomMax = zoomMax;
  }

  /**
   * The maximim zoom interval.
   *
   * @return the maximum zoom interval
   */
  public int getZoomMax() {
    return getState().zoomMax;
  }

  /**
   * If true, the events on the timeline are selectable. When an event is
   * selected, the select event is fired.
   *
   * @param selectable true to enable event selection, false to disable
   */
  public void setSelectable(boolean selectable) {
    getState().selectable = selectable;
  }

  /**
   * Returns true if events on the timeline are selectable.
   *
   * @return true if the events on the timeline are selectable on the client
   */
  public boolean isSelectable() {
    return getState().selectable;
  }

  /**
   * If true, the events on the timeline can be moved in time.
   *
   * @param updateTime true to enable client side moving of events in time,
   * false to disable
   */
  public void setUpdateTime(boolean updateTime) {
    getState().updateTime = updateTime;
  }

  /**
   * Returns true if the events on the timeline can be moved in time.
   *
   * @return true if events are moveable on the client
   */
  public boolean isUpdateTime() {
    return getState().updateTime;
  }

  /**
   * If true, the events on the timeline can be moved between groups.
   *
   * @param updateGroup true to enable moving events between groups, false to
   * disable
   */
  public void setUpdateGroup(boolean updateGroup) {
    getState().updateGroup = updateGroup;
  }

  /**
   * Returns true if the events on the timeline can be moved between groups.
   *
   * @return true if events can be moved between groups on the client
   */
  public boolean isUpdateGroup() {
    return getState().updateGroup;
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
   * Sets a container as a data source for the events in the calendar.
   * Equivalent for doing
   * {@code Timeline.setEventProvider(new ContainerEventProvider(container))}
   *
   * Use this method if you are adding a container which uses the default
   * property ids like {@link BeanItemContainer} for instance. If you are using
   * custom properties instead use
   * {@link Timeline#setContainerDataSource(com.vaadin.data.Container.Indexed, Object, Object, Object, Object, Object)}
   *
   * Please note that the container must be sorted by date!
   *
   * @param container The container to use as a datasource
   */
  public void setContainerDataSource(Container.Indexed container) {
    ContainerEventProvider provider = new ContainerEventProvider(container);
   
    setEventProvider(provider);
  }

  /**
   * Sets a container as a data source for the events in the calendar.
   * Equivalent for doing
   * <code>Calendar.setEventProvider(new ContainerEventProvider(container))</code>
   *
   * Please note that the container must be sorted by date!
   *
   * @param container The container to use as a data source
   * @param captionProperty The property that has the caption, null if no
   * caption property is present
   * @param descriptionProperty The property that has the description, null if
   * no description property is present
   * @param startDateProperty The property that has the starting date
   * @param endDateProperty The property that has the ending date
   * @param styleNameProperty The property that has the stylename, null if no
   * stylname property is present
   */
  public void setContainerDataSource(Container.Indexed container,
      Object captionProperty, Object descriptionProperty,
      Object startDateProperty, Object endDateProperty,
      Object styleNameProperty) {
    ContainerEventProvider provider = new ContainerEventProvider(container);
    provider.setCaptionProperty(captionProperty);
    provider.setDescriptionProperty(descriptionProperty);
    provider.setStartDateProperty(startDateProperty);
    provider.setEndDateProperty(endDateProperty);
    provider.setStyleNameProperty(styleNameProperty);
    
    setEventProvider(provider);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.vaadin.addon.calendar.event.CalendarEventProvider#getEvents(java.
   * util.Date, java.util.Date)
   */
  @Override
  public List<CalendarEvent> getEvents(Date startDate, Date endDate) {
    return getEventProvider().getEvents(startDate, endDate);
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.vaadin.addon.calendar.event.CalendarEditableEventProvider#addEvent
   * (com.vaadin.addon.calendar.event.CalendarEvent)
   */
  @Override
  public void addEvent(CalendarEvent event) {
    if (getEventProvider() instanceof CalendarEditableEventProvider) {
      CalendarEditableEventProvider provider =
          (CalendarEditableEventProvider) getEventProvider();
      provider.addEvent(event);
      markAsDirty();
    }
    else {
      throw new UnsupportedOperationException(
          "Event provider does not support adding events");
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * com.vaadin.addon.calendar.event.CalendarEditableEventProvider#removeEvent
   * (com.vaadin.addon.calendar.event.CalendarEvent)
   */
  @Override
  public void removeEvent(CalendarEvent event) {
    if (getEventProvider() instanceof CalendarEditableEventProvider) {
      CalendarEditableEventProvider provider =
          (CalendarEditableEventProvider) getEventProvider();
      provider.removeEvent(event);
      markAsDirty();
    }
    else {
      throw new UnsupportedOperationException(
          "Event provider does not support removing events");
    }
  }

  @Override
  public void eventChange(CalendarEvent.EventChangeEvent eventChangeEvent) {
    markAsDirty();
  }

  /**
   * Adds the given listener for {@link EventSelect} events.
   *
   * @param listener the listener to add
   */
  public void addEventSelectListener(EventSelectListener listener) {
    addListener(EventSelect.class, listener,
        TimelineComponentEvents.EVENT_SELECT_METHOD);
  }

  /**
   * Adds the given listener for {@link EventSelect} events.
   *
   * @param listener the listener to add
   */
  public void removeEventSelectListener(EventSelectListener listener) {
    removeListener(EventSelect.class, listener,
        TimelineComponentEvents.EVENT_SELECT_METHOD);
  }

  /**
   * Adds the given listener for {@link VisibleRangeChange} events.
   *
   * @param listener the listener to add
   */
  public void addVisibleRangeChangeListener(VisibleRangeChangeListener listener) {
    addListener(VisibleRangeChange.class, listener,
        TimelineComponentEvents.VISIBLE_RANGE_CHANGE_METHOD);
  }

  /**
   * Adds the given listener for {@link VisibleRangeChange} events.
   *
   * @param listener the listener to add
   */
  public void removeVisibleChangeListenerListener(
      VisibleRangeChangeListener listener) {
    removeListener(VisibleRangeChange.class, listener,
        TimelineComponentEvents.VISIBLE_RANGE_CHANGE_METHOD);
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

        VisibleRangeChange evt =
            new VisibleRangeChange(Timeline.this, startDate, endDate);
        fireEvent(evt);
      }
    }

    @Override
    public void select(int index) {
      System.out.println("Selected: " + index);

      EventSelect evt;
      if (index == -1 || events.size() <= index) {
        evt = new EventSelect(Timeline.this, null);
      }
      else {
        evt = new EventSelect(Timeline.this, events.get(index));
      }

      fireEvent(evt);
    }
  }
}
