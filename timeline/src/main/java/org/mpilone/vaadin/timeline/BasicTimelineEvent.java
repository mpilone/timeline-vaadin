
package org.mpilone.vaadin.timeline;

import java.util.Objects;

import com.vaadin.ui.components.calendar.event.BasicEvent;

/**
 * A timeline event implementation that stores all the data as a simple Java
 * bean.
 *
 * @author mpilone
 */
public class BasicTimelineEvent extends BasicEvent implements TimelineEvent {

  private String group;

  /**
   * Sets the name of the group that the event belongs to. All events with the
   * same group name will be displayed together on the timeline in a single row.
   *
   * @param group the name of the group or null
   */
  public void setGroup(String group) {
    if (!Objects.equals(this.group, group)) {
      this.group = group;

      fireEventChange();
    }
  }

  
  @Override
  public String getGroup() {
    return group;
  }

}
