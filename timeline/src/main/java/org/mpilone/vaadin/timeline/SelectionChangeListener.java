
package org.mpilone.vaadin.timeline;

import java.util.Collection;
import java.util.EventObject;
import java.util.Set;

/**
 *
 * @author mpilone
 */
public interface SelectionChangeListener {

  void selectionChange(SelectionChangeEvent evt);

  public static class SelectionChangeEvent extends EventObject {

    public SelectionChangeEvent(Timeline source, Collection<Object> oldSelection,
        Collection<Object> newSelection) {
      super(source);
    }

    @Override
    public Timeline getSource() {
      return (Timeline) super.getSource();
    }

    public Set<Object> getAdded() {
      // TODO
      return null;
    }

    public Set<Object> getRemoved() {
      // TODO
      return null;
    }
  }

}
