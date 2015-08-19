package org.mpilone.vaadin.timeline;

import java.lang.reflect.Method;

import org.mpilone.vaadin.timeline.shared.EventProperties;

import com.vaadin.util.ReflectTools;

/**
 * Listener for right-click events on the timeline.
 */
public interface ContextMenuListener {
  /**
   * The handler method in the listener.
   */
  static final Method METHOD =
      ReflectTools.findMethod(ContextMenuListener.class, "contextMenu",
      ContextMenuEvent.class);

  /**
   * The handler method called when the timeline is right-clicked.
   *
   * @param evt the event details
   */
  void contextMenu(ContextMenuEvent evt);

  public static class ContextMenuEvent extends ClickListener.ClickEvent {

    public ContextMenuEvent(Timeline source, Object itemId,
        EventProperties props) {
      super(source, itemId, props);
    }
  }

}
