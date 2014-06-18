
package org.mpilone.vaadin.timeline;

import java.util.Date;
import java.util.EventObject;

/**
 *
 * @author mpilone
 */
public interface WindowRangeChangeListener {

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
     * @param source the source timeline component
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
