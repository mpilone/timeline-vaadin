
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
   * Called when the visible window range has changed on the client side.
   *
   * @param start the start time of the visible range
   * @param end the end time of the visible range
   * @param byUser changed happened because of user drag/zoom
   */
  void rangeChanged(long start, long end, boolean byUser);

  /**
   * Called when the selected items changes on the client side.
   *
   * @param clientKeys the list of selected items or an empty list if no items
   * are selected
   */
  void select(List<String> clientKeys);
}
