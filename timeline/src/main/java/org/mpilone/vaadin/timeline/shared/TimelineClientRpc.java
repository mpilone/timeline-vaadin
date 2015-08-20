
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
   * @param options the options for the method
   */
  void setWindow(long start, long end, MethodOptions.SetWindow options);

  /**
   * Select one or multiple items by their id. The currently selected items will
   * be unselected. To unselect all selected items, call `setSelection([])`.
   *
   * @param ids the ids of the items to select
   * @param options the options for the method
   */
  void setSelection(Object[] ids, MethodOptions.SetSelection options);

  /**
   * Adjust the visible window such that it fits all items.
   *
   * @param options the options for the method
   */
  void fit(MethodOptions.Fit options);

  /**
   * Adjust the visible window such that the selected item (or multiple items)
   * are centered on screen.
   *
   * @param ids the ids of the items to focus on
   * @param options the options for the method
   */
  void focus(Object[] ids, MethodOptions.Focus options);

  /**
   * Move the window such that given time is centered on screen.
   *
   * @param time the time to center on
   * @param options the options for the method
   */
  void moveTo(long time, MethodOptions.MoveTo options);

//  void setItems(Item[] items);
//
//  void setGroups(Group[] groups);

  /**
   * Options that can be passed to specific methods on the timeline.
   */
  public static class MethodOptions {

    public static class Fit {

      public Animation animation;
    }

    public static class Focus {

      public Animation animation;
    }

    public static class MoveTo {

      public Animation animation;
    }

    public static class SetSelection {

      public boolean focus;
      public Animation animation;
    }

    public static class SetWindow {

      public Animation animation;
    }
  }

  public static class Animation {

    public Integer duration;
    public String easingFunction;
  }
}
