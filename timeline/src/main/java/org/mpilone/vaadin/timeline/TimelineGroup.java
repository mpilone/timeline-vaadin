
package org.mpilone.vaadin.timeline;

/**
 * Defines a group (i.e. a horizontal row) on the timeline. Items can be placed
 * into groups using the {@link TimelineItem#getGroupId()} field. For example,
 * groups may be used to specify resources such as a conference room or phone
 * line that is associated with items using the resource.
 *
 * @author mpilone
 */
public interface TimelineGroup {

  /**
   * The ID of the group to be referenced by items.
   *
   * @return the ID of the group
   */
  public String getId();

  /**
   * The content of the group to be displayed as the group's label.
   *
   * @return the content string
   */
  public String getContent();

  /**
   * The optional CSS style name(s) to be applied to the group.
   *
   * @return the CSS style name(s)
   */
  public String getStyleName();

}
