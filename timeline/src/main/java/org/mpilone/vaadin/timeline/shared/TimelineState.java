
package org.mpilone.vaadin.timeline.shared;

import java.util.List;

import com.vaadin.shared.ui.JavaScriptComponentState;

/**
 *
 * @author mpilone
 */
public class TimelineState extends JavaScriptComponentState {
  public boolean showCurrentTime = true;
  public boolean zoomable = true;
  public int zoomMax = Integer.MAX_VALUE;
  public int zoomMin = 10;
  public List<Event> events;

  public static class Event {

    public int index;
    public long start;
    public long end;
    public String content;
    public String className;
    public String group;

  }
}
