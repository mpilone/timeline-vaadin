package org.mpilone.vaadin.timeline;

import java.util.*;

import org.mpilone.vaadin.timeline.shared.TimelineServerRpc;
import org.mpilone.vaadin.timeline.shared.TimelineState;

import com.vaadin.annotations.*;
import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.Calendar;
import com.vaadin.ui.components.calendar.ContainerEventProvider;
import com.vaadin.ui.components.calendar.event.*;

/**
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

  Calendar cal;

  /**
   * Internal buffer of events.
   */
  protected List<CalendarEvent> events;

  /**
   * The event provider.
   */
  private CalendarEventProvider calendarEventProvider;

  private TimelineServerRpc rpc = new ServerRpcImpl();

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
    registerRpc(rpc);
    setCaption(caption);
//    handlers = new HashMap<String, EventListener>();
//    setDefaultHandlers();
//    currentCalendar.setTime(new Date());
    setEventProvider(eventProvider);
  }

  public void setStartDate(Date date) {
    if (!getStartDate().equals(date)) {
      markAsDirty();
      startDate = date;
    }
  }

  public Date getStartDate() {
    if (startDate == null) {
      startDate = new Date();
    }

    return startDate;
  }

  public void setEndDate(Date date) {
    if (!getEndDate().equals(date)) {
      markAsDirty();
      endDate = date;
    }
  }

  public Date getEndDate() {
    if (endDate == null) {
      currentCalendar.setTime(startDate);
      currentCalendar.add(java.util.Calendar.DATE, 7);
      endDate = currentCalendar.getTime();
    }

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
   * @return the {@link CalendarEventProvider} currently used
   */
  public CalendarEventProvider getEventProvider() {
    return calendarEventProvider;
  }

  public void setShowCurrentTime(boolean visible) {
    getState().showCurrentTime = visible;
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

  }

}
