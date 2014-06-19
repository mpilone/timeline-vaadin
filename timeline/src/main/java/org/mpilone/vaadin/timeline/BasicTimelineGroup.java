
package org.mpilone.vaadin.timeline;

/**
 * An immutable implementation of a timeline group.
 *
 * @author mpilone
 */
public class BasicTimelineGroup implements TimelineGroup {

  private final String id;
  private final String content;
  private final String styleName;

  /**
   * Constructs the group.
   *
   * @param id the unique ID of the group that will be referenced by items
   * @param content the content to display as the group label
   * @param styleName the CSS style name(s) or null
   */
  public BasicTimelineGroup(String id, String content, String styleName) {
    this.id = id;
    this.content = content;
    this.styleName = styleName;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getContent() {
    return content;
  }

  @Override
  public String getStyleName() {
    return styleName;
  }

}
