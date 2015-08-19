
package org.mpilone.vaadin.timeline;

import java.lang.reflect.Method;
import java.util.*;

import com.vaadin.util.ReflectTools;

/**
 * A listener to be notified about selection change events.
 *
 * @author mpilone
 */
public interface SelectListener {

  /**
   * The event handling method on the {@link SelectListener}.
   */
  static final Method SELECTION_CHANGE_METHOD = ReflectTools.findMethod(
      SelectListener.class, "select",
      SelectListener.SelectEvent.class);

  /**
   * Called when the selection in the timeline changes.
   *
   * @param evt the details of the change
   */
  void select(SelectEvent evt);

  /**
   * The selection change event which provides the items added and removed. The
   * list of currently selected items is available from the timeline.
   */
  public static class SelectEvent extends EventObject {

    private final Set<Object> items;

    /**
     * Constructs the event.
     *
     * @param source the timeline that generated the event
     * @param items the selected item IDs
     */
    public SelectEvent(Timeline source, Collection<Object> items) {
      super(source);

      this.items = Collections.unmodifiableSet(new HashSet<>(items));
    }

    /**
     * Returns the timeline/source component that generated the event.
     *
     * @return the timeline
     */
    public Timeline getTimeline() {
      return (Timeline) getSource();
    }

    /**
     * Returns all the item IDs that were selected.
     *
     * @return the selected item IDs or an empty set
     */
    public Set<Object> getItems() {
      return items;
    }

    @Override
    public String toString() {
      return "SelectEvent{" + "items=" + items + '}';
    }

  }
}
