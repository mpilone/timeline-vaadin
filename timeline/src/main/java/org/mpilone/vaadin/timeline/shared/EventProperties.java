package org.mpilone.vaadin.timeline.shared;

import java.util.Date;

/**
 * Properties from an event on the timeline.
 *
 * @author mpilone
 */
public class EventProperties {

  /**
   * group (String, Number, or null): the id of the clicked group.
   */
  public String group;

  /**
   * item (String, Number, or null): the id of the clicked item.
   */
  public String item;

  /**
   * pageX (Number): absolute horizontal position of the click event.
   */
  public int pageX;

  /**
   * pageY (Number): absolute vertical position of the click event.
   */
  public int pageY;

  /**
   * x (Number): relative horizontal position of the click event.
   */
  public int x;

  /**
   * y (Number): relative vertical position of the click event.
   */
  public int y;

  /**
   * time (Date): Date of the clicked event.
   */
  public Date time;

  /**
   * snappedTime (Date): Date of the clicked event, snapped to a nice value.
   */
  public Date snappedTime;

  /**
   * what (String or null): name of the clicked thing: item, background, axis,
   * group-label, custom-time, or current-time.
   */
  public String what;

  // event (Object): the original click event.
  @Override
  public String toString() {
    return "EventProperties{" + "group=" + group + ", item=" + item + ", pageX="
        + pageX + ", pageY=" + pageY + ", x=" + x + ", y=" + y + ", time="
        + time + ", snappedTime=" + snappedTime + ", what=" + what + '}';
  }
}
