
package org.mpilone.vaadin.timeline;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.EventObject;

import com.vaadin.util.ReflectTools;

/**
 * A listener to be notified about visible window range change events.
 *
 * @author mpilone
 */
public interface RangeChangedListener {

  /**
   * The event handling method on the {@link RangeChangedListener}.
   */
  static final Method METHOD = ReflectTools.findMethod(
      RangeChangedListener.class,
      "rangeChanged",
      RangeChangedListener.RangeChangedEvent.class);

  /**
   * Called when the visible range changes on the timeline.
   *
   * @param event the event details
   */
  void rangeChanged(RangeChangedEvent event);

  /**
   * The event fired when the window has changed on the timeline.
   */
  public static class RangeChangedEvent extends EventObject {

    private final Date startDate;
    private final Date endDate;
    private final boolean byUser;

    /**
     * Constructs the event.
     *
     * @param source the timeline component that generated the event
     * @param startDate the start date of the visible time range
     * @param endDate the end date of the visible time range
     * @param byUser change happened because of a user drag/zoom
     */
    public RangeChangedEvent(Timeline source, Date startDate, Date endDate,
        boolean byUser) {
      super(source);

      this.startDate = startDate;
      this.endDate = endDate;
      this.byUser = byUser;
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

    public boolean isByUser() {
      return byUser;
    }

    /**
     * Returns the timeline source.
     *
     * @return the source component
     */
    public Timeline getTimeline() {
      return (Timeline) getSource();
    }

    @Override
    public String toString() {
      return "RangeChangedEvent{" + "startDate=" + startDate + ", endDate="
          + endDate + ", byUser=" + byUser + '}';
    }
  }

}
