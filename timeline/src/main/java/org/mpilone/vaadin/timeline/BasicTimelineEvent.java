
package org.mpilone.vaadin.timeline;

import java.util.Objects;

import com.vaadin.ui.components.calendar.event.BasicEvent;

/**
 *
 * @author mpilone
 */
public class BasicTimelineEvent extends BasicEvent implements TimelineEvent {

  private String group;

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
