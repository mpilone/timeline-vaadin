
package org.mpilone.vaadin.timeline.shared;

import com.vaadin.shared.communication.ClientRpc;

/**
 * The server to client RPC operations.
 *
 * @author mpilone
 */
public interface TimelineClientRpc extends ClientRpc {

  /**
   * Sets the current time to be displayed on the client.
   *
   * @param time the current time in milliseconds past the epoch
   */
   void setCurrentTime(long time);

  /**
   * Move the visible range such that the current time is located in the center
   * of the timeline. This method does not trigger a rangechange event.
   */
  void setVisibleChartRangeNow();

  /**
   * Sets the visible range (zoom) to the specified range. Accepts two
   * parameters of type Date that represent the first and last times of the
   * wanted selected visible range. Set start to -1 to include everything from
   * the earliest date to end; set end to -1 to include everything from start to
   * the last date.
   *
   * @param start the start time or -1
   * @param end the end time or -1
   */
  void setVisibleChartRange(long start, long end);

  /**
   * Move the timeline the given movefactor to the left or right. Start and end
   * date will be adjusted, and the timeline will be redrawn. For example, try
   * moveFactor = 0.1 or -0.1. moveFactor is a Number that determines the moving
   * amount. A positive value will move right, a negative value will move left.
   *
   * @param moveFactor
   */
  void move(double moveFactor);

}
