package org.mpilone.vaadin.timeline;

import java.util.*;

import org.mpilone.vaadin.timeline.shared.TimelineClientRpc;
import org.mpilone.vaadin.timeline.shared.TimelineServerRpc;
import org.mpilone.vaadin.timeline.shared.TimelineState;

import com.vaadin.annotations.*;
import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.components.calendar.ContainerEventProvider;
import com.vaadin.ui.components.calendar.event.*;

/**
 * An implementation of the Chaps Links Timeline component
 * (http://almende.github.io/chap-links-library/timeline.html).
 *
 * @author mpilone
 */
@StyleSheet("timeline/timeline.css")
@JavaScript({"timeline_connector.js", "timeline/timeline-min.js"})
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
    setVisibleChartRange(start, end);

//    handlers = new HashMap<String, EventListener>();
//    setDefaultHandlers();
//    currentCalendar.setTime(new Date());
  }

  public void setStartDate(Date date) {
    setVisibleChartRange(date, endDate);
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setEndDate(Date date) {
    setVisibleChartRange(startDate, date);
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
  }

  /**
   * Sets the visible range (zoom) to the specified range. Accepts two
   * parameters of type Date that represent the first and last times of the
   * wanted selected visible range.
   *
   * @param start the start date
   * @param end the end date
   */
  public void setVisibleChartRange(Date start, Date end) {
    if (!Objects.equals(this.startDate, start) || !Objects.
        equals(this.endDate, end)) {
      this.startDate = start;
      this.endDate = end;

      clientRpc.setVisibleChartRange(start.getTime(), end.getTime());
    }
  }

  /**
   * Move the visible range such that the current time is located in the center
   * of the timeline.
   */
  public void setVisibleChartRangeNow() {
    clientRpc.setVisibleChartRangeNow();
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
   * If true, the timeline is zoomable. When the timeline is zoomed, the
   * rangechange event is fired.
   *
   * @param zoomable true to enable client side zooming, false to disable
   */
  public void setZoomable(boolean zoomable) {
    getState().zoomable = zoomable;
  }

  /**
   * Returns true if the timeline is zoomable.
   *
   * @return true if zoomable
   */
  public boolean isZoomable() {
    return getState().zoomable;
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
   * If true, the timeline is movable. When the timeline moved, the rangechange
   * events are fired.
   *
   * @param moveable true to enable client side moving, false to disable
   */
  public void setMoveable(boolean moveable) {
    getState().moveable = moveable;
  }

  /**
   * Returns true if the timeline is movable.
   *
   * @return true if the timeline is moveable on the client
   */
  public boolean isMoveable() {
    return getState().moveable;
  }

  /**
   * Adjust the current time of the timeline. This can for example be changed to
   * match the time of a server or a time offset of another time zone.
   *
   * @param time the current time to set
   */
  public void setCurrentTime(Date time) {
    clientRpc.setCurrentTime(time.getTime());
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

  private class ServerRpcImpl implements TimelineServerRpc {

    @Override
    public void ackSetCurrentTime() {
      // TODO: Implement method
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

        System.out.println("rangeChanged: " + startDate + " to " + endDate);
        // TODO: fire an event
      }
    }

    @Override
    public void select(int index) {
      System.out.println("Selected: " + index);

      // TODO: fire an event
    }

  }

}
