package org.mpilone.vaadin.timeline;

import java.util.Date;

/**
 * The various options that can be configured on the timeline. Refer to
 * http://visjs.org/docs/timeline.html#Configuration_Options for detailed
 * documentation.
 *
 * @author mpilone
 */
public interface TimelineOptions {

  public boolean isMoveable();

  public void setMoveable(boolean moveable);

  public boolean isZoomable();

  public void setZoomable(boolean zoomable);

  public boolean isSelectable();

  public void setSelectable(boolean selectable);

  public ItemAlignment getAlign();

  public void setAlign(ItemAlignment align);

  public boolean isAutoResize();

  public void setAutoResize(boolean autoResize);

  public boolean isEditAdd();

  public void setEditAdd(boolean editAdd);

  public boolean isEditRemove();

  public void setEditRemove(boolean editRemove);

  public boolean isEditUpdateGroup();

  public void setEditUpdateGroup(boolean editUpdateGroup);

  public boolean isEditUpdateTime();

  public void setEditUpdateTime(boolean editUpdateTime);

  public Date getEnd();

  public void setEnd(Date end);

  public String getGroupOrder();

  public void setGroupOrder(String groupOrder);

  public int getMarginAxis();

  public void setMarginAxis(int marginAxis);

  public int getMarginItem();

  public void setMarginItem(int marginItem);

  public Date getMax();

  public void setMax(Date max);

  public Date getMin();

  public void setMin(Date min);

  public TimeAxisOrientation getOrientation();

  public void setOrientation(TimeAxisOrientation orientation);

  public int getPadding();

  public void setPadding(int padding);

  /**
   * If true, the timeline shows a red, vertical line displaying the current
   * time. This time can be synchronized with a server via the method
   * setCurrentTime.
   *
   * @param visible true to enable the vertical time bar, false to disable
   */
  public void setShowCurrentTime(boolean visible);

  /**
   * Returns true if the current time is being shown on the client side.
   *
   * @return true if enabled
   */
  public boolean isShowCurrentTime();

  /**
   * Show a vertical bar displaying a custom time. This line can be dragged by
   * the user. The custom time can be utilized to show a state in the past or in
   * the future.
   *
   * @param visible true to enable the vertical time bar, false to disable
   */
  public void setShowCustomTime(boolean visible);

  /**
   * Returns true if the current time is being shown on the client side.
   *
   * @return true if enabled
   */
  public boolean isShowCustomTime();

  public boolean isShowMajorLabels();

  public void setShowMajorLabels(boolean showMajorLabels);

  public boolean isShowMinorLabels();

  public void setShowMinorLabels(boolean showMinorLabels);

  public boolean isStack();

  public void setStack(boolean stack);

  public Date getStart();

  public void setStart(Date start);

  public ItemType getType();

  public void setType(ItemType type);

  public int getZoomMax();

  public void setZoomMax(int zoomMax);

  public int getZoomMin();

  public void setZoomMin(int zoomMin);

  /**
   * The orientation (i.e. location) of the time axis on the timeline.
   */
  public enum TimeAxisOrientation {

    BOTTOM,
    TOP
  }

  /**
   * The type of the item which controls how it is rendered on the timeline.
   *
   * @author mpilone
   */
  public enum ItemType {

    BOX,
    POINT,
    RANGE,
    RANGEOVERFLOW
  }

  /**
   * The alignment of items with type {@link ItemType#BOX}.
   */
  public enum ItemAlignment {
    LEFT,
    CENTER,
    RIGHT
  }

}
