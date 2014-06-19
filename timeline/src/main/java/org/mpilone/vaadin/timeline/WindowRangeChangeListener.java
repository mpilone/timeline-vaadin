
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
public interface WindowRangeChangeListener {

  /**
   * The event handling method on the {@link WindowRangeChangeListener}.
   */
  static final Method WINDOW_RANGE_CHANGE_METHOD = ReflectTools.findMethod(
      WindowRangeChangeListener.class,
      "windowRangeChange",
      WindowRangeChangeListener.WindowRangeChangeEvent.class);

  /**
   * Called when the visible range changes on the timeline.
   *
   * @param event the event details
   */
  void windowRangeChange(WindowRangeChangeEvent event);

  /**
   * The event fired when an event is selected on the timeline.
   */
  public static class WindowRangeChangeEvent extends EventObject {

    private final Date startDate;
    private final Date endDate;

    /**
     * Constructs the event.
     *
     * @param source the timeline component that generated the event
     * @param startDate the start date of the visible time range
     * @param endDate the end date of the visible time range
     */
    public WindowRangeChangeEvent(Timeline source, Date startDate, Date endDate) {
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

    @Override
    public Timeline getSource() {
      return (Timeline) super.getSource();
    }
  }

}
