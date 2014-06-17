
package org.mpilone.vaadin.timeline;

import java.lang.reflect.Method;
import java.util.Date;

import com.vaadin.ui.Component;
import com.vaadin.ui.components.calendar.event.CalendarEvent;

/**
 * The handler and events for the {@link Timeline} component.
 *
 * @author mpilone
 */
public abstract class TimelineComponentEvents {

  /**
   * The handler method to fire on the {@link EventSelectListener}.
   */
  static final Method EVENT_SELECT_METHOD;

  /**
   * The handler method to fire on the {@link VisibleRangeChangeListener}.
   */
  static final Method VISIBLE_RANGE_CHANGE_METHOD;

  static {
    try {
      EVENT_SELECT_METHOD = EventSelectListener.class.getMethod("eventSelect",
          EventSelect.class);
      VISIBLE_RANGE_CHANGE_METHOD = VisibleRangeChangeListener.class.getMethod(
          "visibleRangeChange",
          VisibleRangeChange.class);
    }
    catch (NoSuchMethodException | SecurityException ex) {
      throw new RuntimeException("Unable to find required handler method.", ex);
    }
  }

  /**
   * Constructor to prevent construction.
   */
  private TimelineComponentEvents() {
    // no op
  }

  /**
   * The common base class of all timeline events.
   */
  public abstract static class TimelineComponentEvent extends Component.Event {

    /**
     *  * Constructs the event.
     *
     * @param source the source timeline component
     */
    public TimelineComponentEvent(Timeline source) {
      super(source);
    }

    @Override
    public Timeline getComponent() {
      return (Timeline) super.getComponent();
    }
  }

  /**
   * The event fired when an event is selected on the timeline.
   */
  public static class EventSelect extends TimelineComponentEvent {
    private final CalendarEvent calendarEvent;

    /**
     * Constructs the event.
     *
     * @param source the source timeline component
     * @param calendarEvent the calendar event that was selected or null if
     * selection was cleared
     */
    public EventSelect(Timeline source, CalendarEvent calendarEvent) {
      super(source);

      this.calendarEvent = calendarEvent;
    }

    /**
     * Returns the calendar event that was selected or null if the selection was
     * cleared.
     *
     * @return the calendar event that was selected or null
     */
    public CalendarEvent getCalendarEvent() {
      return calendarEvent;
    }
  }

  /**
   * Event handler for calendar event selection.
   */
  public interface EventSelectListener {

    /**
     * Called when an event is selected (or unselected) in the timeline.
     *
     * @param event the event details
     */
    void eventSelect(EventSelect event);
  }

  /**
   * The event fired when an event is selected on the timeline.
   */
  public static class VisibleRangeChange extends TimelineComponentEvent {
    private final Date startDate;
    private final Date endDate;

    /**
     * Constructs the event.
     *
     * @param source the source timeline component
     * @param startDate the start date of the visible time range
     * @param endDate the end date of the visible time range
     */
    public VisibleRangeChange(Timeline source, Date startDate, Date endDate) {
      super(source);

      this.startDate = startDate;
      this.endDate = endDate;
    }

    /**
     * Returns the start date of the visible time range.
     *
     * @return the start date
     */
    public Date getStartDate() {
      return startDate;
    }

    /**
     * Returns the end date of the visible time range.
     *
     * @return the end date
     */
    public Date getEndDate() {
      return endDate;
    }
  }

  /**
   * Event handler for calendar event selection.
   */
  public interface VisibleRangeChangeListener {

    /**
     * Called when the visible range changes on the timeline.
     *
     * @param event the event details
     */
    void visibleRangeChange(VisibleRangeChange event);
  }

}
