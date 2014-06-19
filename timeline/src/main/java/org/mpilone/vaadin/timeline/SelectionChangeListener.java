
package org.mpilone.vaadin.timeline;

import java.lang.reflect.Method;
import java.util.*;

import com.vaadin.util.ReflectTools;

/**
 * A listener to be notified about selection change events.
 *
 * @author mpilone
 */
public interface SelectionChangeListener {

  /**
   * The event handling method on the {@link SelectionChangeListener}.
   */
  static final Method SELECTION_CHANGE_METHOD = ReflectTools.findMethod(
      SelectionChangeListener.class, "selectionChange",
      SelectionChangeListener.SelectionChangeEvent.class);

  /**
   * Called when the selection in the timeline changes.
   *
   * @param evt the details of the change
   */
  void selectionChange(SelectionChangeEvent evt);

  /**
   * The selection change event which provides the items added and removed. The
   * list of currently selected items is available from the timeline.
   */
  public static class SelectionChangeEvent extends EventObject {

    private final Set<Object> added;
    private final Set<Object> removed;

    /**
     * Constructs the event.
     *
     * @param source the timeline that generated the event
     * @param oldSelection the old set of selected items
     * @param newSelection the new set of selected items
     */
    public SelectionChangeEvent(Timeline source, Collection<Object> oldSelection,
        Collection<Object> newSelection) {
      super(source);

      added = new HashSet<>(newSelection);
      added.removeAll(oldSelection);

      removed = new HashSet<>(oldSelection);
      removed.removeAll(newSelection);
    }

    @Override
    public Timeline getSource() {
      return (Timeline) super.getSource();
    }

    /**
     * Returns all the item IDs that were added to the selection
     *
     * @return the added item IDs or an empty set
     */
    public Set<Object> getAdded() {
      return Collections.unmodifiableSet(added);
    }

    /**
     * Returns all the item IDs that were removed from the selection
     *
     * @return the removed item IDs or an empty set
     */
    public Set<Object> getRemoved() {
      return Collections.unmodifiableSet(removed);
    }
  }
}
