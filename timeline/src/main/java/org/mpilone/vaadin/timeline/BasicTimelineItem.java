
package org.mpilone.vaadin.timeline;

import java.util.*;

import org.mpilone.vaadin.timeline.TimelineOptions.ItemType;


/**
 * A timeline event implementation that stores all the data as a simple Java
 * bean.
 *
 * @author mpilone
 */
public class BasicTimelineItem implements TimelineItem {

  private String groupId;
  private Date start;
  private Date end;
  private String content;
  private ItemType type;
  private String styleName;
  private Object id;

  public BasicTimelineItem() {
    this(UUID.randomUUID().toString(), null, null, null);
  }

  public BasicTimelineItem(Object id, Date start, Date end, String content) {
    this.start = start;
    this.end = end;
    this.content = content;
    this.id = id;

    this.type = end == null ? ItemType.POINT : ItemType.RANGE;
  }

  @Override
  public Object getId() {
    return id;
  }

  public void setId(Object id) {
    this.id = id;
  }

  @Override
  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  @Override
  public Date getStart() {
    return start;
  }

  public void setStart(Date start) {
    this.start = start;
  }

  @Override
  public Date getEnd() {
    return end;
  }

  public void setEnd(Date end) {
    this.end = end;
  }

  @Override
  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public ItemType getType() {
    return type;
  }

  public void setType(ItemType type) {
    this.type = type;
  }

  @Override
  public String getStyleName() {
    return styleName;
  }

  public void setStyleName(String styleName) {
    this.styleName = styleName;
  }


}
