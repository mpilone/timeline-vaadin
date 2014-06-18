
package org.mpilone.vaadin.timeline.shared;

import com.vaadin.shared.communication.ClientRpc;

/**
 * The server to client RPC operations.
 *
 * @author mpilone
 */
public interface TimelineClientRpc extends ClientRpc {

  /**
   * Sets the custom time to be displayed on the client.
   *
   * @param time the custom time in milliseconds past the epoch
   */
  void setCustomTime(long time);

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
