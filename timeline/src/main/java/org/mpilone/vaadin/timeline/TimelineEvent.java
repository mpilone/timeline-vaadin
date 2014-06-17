
package org.mpilone.vaadin.timeline;

import com.vaadin.ui.components.calendar.event.CalendarEvent;

/**
 * A timeline specific event that supports additional options over the standard
 * calendar event. Use of this interface is encouraged in order to use the
 * advanced features but the timeline can also work directly with existing
 * calendar events.
 *
 * @author mpilone
 */
public interface TimelineEvent extends CalendarEvent {

  /**
   * Returns the name of the group that the event belongs to. All events with
   * the same group name will be displayed together on the timeline in a single
   * row.
   *
   * @return the name of the group or null
   */
  public String getGroup();
}
