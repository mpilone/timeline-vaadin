
package org.mpilone.vaadin.timeline.shared;

import java.util.List;

import com.vaadin.shared.ui.JavaScriptComponentState;

/**
 * The timeline state.
 *
 * @author mpilone
 */
public class TimelineState extends JavaScriptComponentState {
  public List<Item> items;
  public List<Group> groups;
  public Options options;

  public static class Editable {

    public boolean add;
    public boolean remove;
    public boolean updateGroup;
    public boolean updateTime;
  }

  public static class Margin {

    public int axis;
    public int item;
  }

  public static class Options {

    public String align;
    public boolean autoResize;
    public Editable editable;
    public Long end;
    public String groupOrder;
    public Margin margin;
    public Long max;
    public Long min;
    public boolean moveable;
    public String orientation;
    public int padding;
    public boolean selectable;
    public boolean showCustomTime;
    public boolean showCurrentTime;
    public boolean showMajorLabels;
    public boolean showMinorLabels;
    public boolean stack;
    public Long start;
    public String type;
    public boolean zoomable;
    public int zoomMax;
    public int zoomMin;
  }

  public static class Group {
    public String id;
    public String content;
    public String className;
  }

  public static class Item {
    public String id;
    public long start;
    public long end;
    public String content;
    public String className;
    public String group;
    public String type;
  }
}
