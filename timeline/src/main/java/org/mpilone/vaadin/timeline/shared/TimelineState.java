
package org.mpilone.vaadin.timeline.shared;

import java.util.List;

import com.vaadin.shared.ui.JavaScriptComponentState;

/**
 * The timeline state.
 *
 * @author mpilone
 */
public class TimelineState extends JavaScriptComponentState {
  public boolean showCurrentTime = true;
  public int zoomMax = Integer.MAX_VALUE;
  public int zoomMin = 1000;
  public boolean updateTime = true;
  public boolean updateGroup = true;
  public boolean selectable = true;
  public List<Event> events;
  public List<Group> groups;
  public boolean showCustomTime = false;

  public static class Group {
    public String id;
    public String content;
    public String className;
  }

  public static class Event {
    public String id;
    public int index;
    public long start;
    public long end;
    public String content;
    public String className;
    public String group;

  }
}
