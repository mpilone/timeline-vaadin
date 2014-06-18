
package org.mpilone.vaadin.timeline;

/**
 *
 * @author mpilone
 */
public class BasicTimelineGroup implements TimelineGroup {

  private final String id;
  private final String caption;
  private final String styleName;

  public BasicTimelineGroup(String id, String caption, String styleName) {
    this.id = id;
    this.caption = caption;
    this.styleName = styleName;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getCaption() {
    return caption;
  }

  public String getStyleName() {
    return styleName;
  }

}
