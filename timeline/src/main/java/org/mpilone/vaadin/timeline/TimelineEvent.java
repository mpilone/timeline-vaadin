
package org.mpilone.vaadin.timeline;

import com.vaadin.ui.components.calendar.event.CalendarEvent;

/**
 *
 * @author mpilone
 */
public interface TimelineEvent extends CalendarEvent {

  public String getGroup();
}
