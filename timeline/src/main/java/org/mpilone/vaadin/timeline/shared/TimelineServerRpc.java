
package org.mpilone.vaadin.timeline.shared;


import java.util.List;

import com.vaadin.shared.communication.ServerRpc;

/**
 * The client to server RPC operations.
 *
 * @author mpilone
 */
public interface TimelineServerRpc extends ServerRpc {

  /**
   * Acknowledges setting the current time on the timeline. This is normally
   * used to calculate the round-trip lag from the server to the client to
   * support dynamically adjusting the current time to account for the lag.
   */
  void ackSetCurrentTime();

  /**
   * Indicates that the range has changed on the client side.
   *
   * @param start the start time of the visible range
   * @param end the end time of the visible range
   */
  void rangeChanged(long start, long end);

  void select(List<String> clientKeys);
}
