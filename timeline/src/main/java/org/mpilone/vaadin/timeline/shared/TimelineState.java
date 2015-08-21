package org.mpilone.vaadin.timeline.shared;

import com.vaadin.shared.ui.JavaScriptComponentState;

/**
 * The timeline state.
 *
 * @author mpilone
 */
public class TimelineState extends JavaScriptComponentState {

  public Options options;

  public static class TimeAxis {

    public String scale;
    public int step;
  }

  public static class Editable {

    public boolean add;
    public boolean remove;
    public boolean updateGroup;
    public boolean updateTime;
  }

  public static class Format {

    public FormatLabels majorLabels;
    public FormatLabels minorLabels;
  }

  public static class FormatLabels {

    public String millisecond;
    public String second;
    public String minute;
    public String hour;
    public String weekday;
    public String day;
    public String month;
    public String year;
  }

  public static class Margin {

    public int axis;
    public MarginItem item;
  }

  public static class MarginItem {

    public int vertical;
    public int horizontal;
  }

  public static class Options {

    public String align;
    public boolean autoResize;
    public boolean clickToUse;
    public Editable editable;
    public Long end;
    public Format format;
    public String groupOrder;
    public Margin margin;
    public Long max;
    public Long min;
    public boolean moveable;
    public boolean multiselect;
    public String orientation;
    public boolean selectable;
    public boolean showCurrentTime;
    public boolean showMajorLabels;
    public boolean showMinorLabels;
    public boolean stack;
    public Long start;
    public TimeAxis timeAxis;
    public String type;
    public boolean zoomable;
    public int zoomMax;
    public int zoomMin;
  }

}
