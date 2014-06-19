
package org.mpilone.vaadin.timeline;

import java.util.Date;

import org.mpilone.vaadin.timeline.shared.TimelineState;

/**
 * An implementation of timeline options that maps all the options into the
 * {@link TimelineState}. This class is normally instantiated and used
 * internally by the {@link Timeline}.
 *
 * @author mpilone
 */
class StateMappingOptions implements TimelineOptions {
  private final Timeline timeline;

  /**
   * Constructs the options which will map all state changes to the state of the
   * given timeline.
   *
   * @param timeline the timeline to modify
   */
  public StateMappingOptions(Timeline timeline) {
    this.timeline = timeline;
    TimelineState.Options o = new TimelineState.Options();
    o.align = TimelineOptions.ItemAlignment.CENTER.name().toLowerCase();
    o.autoResize = true;
    o.editable = new TimelineState.Editable();
    o.editable.add = false;
    o.editable.remove = false;
    o.editable.updateGroup = false;
    o.editable.updateTime = false;
    o.end = null;
    o.groupOrder = null;
    o.margin = new TimelineState.Margin();
    o.margin.axis = 20;
    o.margin.item = 10;
    o.max = null;
    o.min = null;
    o.orientation =
        TimelineOptions.TimeAxisOrientation.BOTTOM.name().toLowerCase();
    o.padding = 5;
    o.selectable = true;
    o.showCurrentTime = false;
    o.showCustomTime = false;
    o.showMajorLabels = true;
    o.showMinorLabels = true;
    o.stack = true;
    o.start = null;
    o.type = TimelineOptions.ItemType.BOX.name().toLowerCase();
    o.zoomMax = Integer.MAX_VALUE;
    o.zoomMin = 10;
    getState().options = o;
  }

  /**
   * Returns the state of the timeline that options should be mapped to.
   *
   * @return the state of the timeline
   */
  private TimelineState getState() {
    return timeline.getState();
  }

  /**
   * Returns the state of the timeline that options should be mapped to.
   *
   * @param markAsDirty true to mark the state as dirty, false otherwise
   *
   * @return the state of the timeline
   */
  private TimelineState getState(boolean markAsDirty) {
    return timeline.getState(markAsDirty);
  }

  @Override
  public boolean isSelectable() {
    return getState(false).options.selectable;
  }

  @Override
  public void setSelectable(boolean selectable) {
    getState().options.selectable = selectable;

    if (!selectable) {
      timeline.deselectAll();
    }
  }

  @Override
  public TimelineOptions.ItemAlignment getAlign() {
    return TimelineOptions.ItemAlignment.valueOf(getState(false).options.align.
        toUpperCase());
  }

  @Override
  public void setAlign(
      TimelineOptions.ItemAlignment align) {
    getState().options.align = align.name().toLowerCase();
  }

  @Override
  public boolean isAutoResize() {
    return getState(false).options.autoResize;
  }

  @Override
  public void setAutoResize(boolean autoResize) {
    getState().options.autoResize = autoResize;
  }

  @Override
  public boolean isEditAdd() {
    return getState(false).options.editable.add;
  }

  @Override
  public void setEditAdd(boolean editAdd) {
    getState().options.editable.add = editAdd;
  }

  @Override
  public boolean isEditRemove() {
    return getState(false).options.editable.remove;
  }

  @Override
  public void setEditRemove(boolean editRemove) {
    getState().options.editable.remove = editRemove;
  }

  @Override
  public boolean isEditUpdateGroup() {
    return getState(false).options.editable.updateGroup;
  }

  @Override
  public void setEditUpdateGroup(boolean editUpdateGroup) {
    getState().options.editable.updateGroup = editUpdateGroup;
  }

  @Override
  public boolean isEditUpdateTime() {
    return getState(false).options.editable.updateTime;
  }

  @Override
  public void setEditUpdateTime(boolean editUpdateTime) {
    getState().options.editable.updateTime = editUpdateTime;
  }

  @Override
  public Date getEnd() {
    Long end = getState(false).options.end;
    return end == null ? null : new Date(end);
  }

  @Override
  public void setEnd(Date end) {
    getState().options.end = end == null ? null : end.getTime();
  }

  @Override
  public String getGroupOrder() {
    return getState(false).options.groupOrder;
  }

  @Override
  public void setGroupOrder(String groupOrder) {
    getState().options.groupOrder = groupOrder;
  }

  @Override
  public int getMarginAxis() {
    return getState(false).options.margin.axis;
  }

  @Override
  public void setMarginAxis(int marginAxis) {
    getState().options.margin.axis = marginAxis;
  }

  @Override
  public int getMarginItem() {
    return getState(false).options.margin.item;
  }

  @Override
  public void setMarginItem(int marginItem) {
    getState().options.margin.item = marginItem;
  }

  @Override
  public Date getMax() {
    Long max = getState(false).options.max;
    return max == null ? null : new Date(max);
  }

  @Override
  public void setMax(Date max) {
    getState().options.max = max == null ? null : max.getTime();
  }

  @Override
  public Date getMin() {
    Long min = getState(false).options.min;
    return min == null ? null : new Date(min);
  }

  @Override
  public void setMin(Date min) {
    getState().options.min = min == null ? null : min.getTime();
  }

  @Override
  public TimelineOptions.TimeAxisOrientation getOrientation() {
    return TimelineOptions.TimeAxisOrientation.valueOf(
        getState(false).options.orientation.toUpperCase());
  }

  @Override
  public void setOrientation(
      TimelineOptions.TimeAxisOrientation orientation) {
    getState().options.orientation = orientation.name().toLowerCase();
  }

  @Override
  public int getPadding() {
    return getState(false).options.padding;
  }

  @Override
  public void setPadding(int padding) {
    getState().options.padding = padding;
  }

  @Override
  public void setShowCurrentTime(boolean visible) {
    getState().options.showCurrentTime = visible;
  }

  @Override
  public boolean isShowCurrentTime() {
    return getState(false).options.showCurrentTime;
  }

  @Override
  public void setShowCustomTime(boolean visible) {
    getState().options.showCustomTime = visible;
  }

  @Override
  public boolean isShowCustomTime() {
    return getState(false).options.showCustomTime;
  }

  @Override
  public boolean isShowMajorLabels() {
    return getState(false).options.showMajorLabels;
  }

  @Override
  public void setShowMajorLabels(boolean showMajorLabels) {
    getState().options.showMajorLabels = showMajorLabels;
  }

  @Override
  public boolean isShowMinorLabels() {
    return getState(false).options.showMinorLabels;
  }

  @Override
  public void setShowMinorLabels(boolean showMinorLabels) {
    getState().options.showMinorLabels = showMinorLabels;
  }

  @Override
  public boolean isStack() {
    return getState(false).options.stack;
  }

  @Override
  public void setStack(boolean stack) {
    getState().options.stack = stack;
  }

  @Override
  public Date getStart() {
    Long start = getState(false).options.start;
    return start == null ? null : new Date(start);
  }

  @Override
  public void setStart(Date start) {
    getState().options.start = start == null ? null : start.getTime();
  }

  @Override
  public TimelineOptions.ItemType getType() {
    return TimelineOptions.ItemType.valueOf(getState(false).options.type.
        toUpperCase());
  }

  @Override
  public void setType(TimelineOptions.ItemType type) {
    getState().options.type = type.name().toLowerCase();
  }

  @Override
  public int getZoomMax() {
    return getState(false).options.zoomMax;
  }

  @Override
  public void setZoomMax(int zoomMax) {
    getState().options.zoomMax = zoomMax;
  }

  @Override
  public int getZoomMin() {
    return getState(false).options.zoomMin;
  }

  @Override
  public void setZoomMin(int zoomMin) {
    getState().options.zoomMin = zoomMin;
  }

}
