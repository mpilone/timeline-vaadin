
package org.mpilone.vaadin.timeline.shared;

import com.vaadin.shared.communication.ClientRpc;

/**
 * The server to client RPC operations.
 *
 * @author mpilone
 */
public interface TimelineClientRpc extends ClientRpc {

  /**
   * Sets the current time. This can be used for example to ensure that a
   * client's time is synchronized with a shared server time.
   *
   * @param time the current time in milliseconds past the epoch
   */
  void setCurrentTime(long time);

  /**
   * Sets the custom time to be displayed on the client.
   *
   * @param time the custom time in milliseconds past the epoch
   * @param id the id of the time bar
   */
  void setCustomTime(long time, String id);

  /**
   * Add a new vertical bar representing a custom time that can be dragged by
   * the user.
   *
   * @param time the custom time in milliseconds past the epoch
   * @param id the id of the time bar
   */
  void addCustomTime(long time, String id);

  /**
   * Remove vertical bars previously added to the timeline via addCustomTime
   * method.
   *
   * @param id the id of the time bar
   */
  void removeCustomTime(String id);

  /**
   * Sets the visible range (zoom) to the specified range. Accepts two
   * parameters of type Date that represent the first and last times of the
   * wanted selected visible range.
   *
   * @param start the start time
   * @param end the end time
   */
  void setWindow(long start, long end);


}
