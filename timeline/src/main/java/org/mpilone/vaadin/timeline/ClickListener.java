package org.mpilone.vaadin.timeline;

import java.lang.reflect.Method;
import java.util.Date;

import org.mpilone.vaadin.timeline.shared.EventProperties;

import com.vaadin.ui.Component;
import com.vaadin.util.ReflectTools;

/**
 * Listener for click events on the timeline.
 */
public interface ClickListener {
  /**
   * The handler method in the listener.
   */
  static final Method METHOD =
      ReflectTools.findMethod(ClickListener.class, "click", ClickEvent.class);

  /**
   * The handler method called when the timeline is left-clicked.
   *
   * @param evt the event details
   */
  void click(ClickEvent evt);

  /**
   * Event details for a click on the timeline.
   */
  public static class ClickEvent extends Component.Event {

    private final EventProperties props;
    private final Object itemId;

    /**
     * Constructs the event.
     *
     * @param source the source component
     * @param itemId the ID of the item clicked or null
     * @param props the event properties relayed from the client side
     */
    public ClickEvent(Timeline source, Object itemId, EventProperties props) {
      super(source);
      this.props = props;
      this.itemId = itemId;
    }

    public Date getSnappedTime() {
      return props.snappedTime;
    }

    public Date getTime() {
      return props.time;
    }

    public String getWhat() {
      return props.what;
    }

    public Object getItem() {
      return itemId;
    }

    public String getGroup() {
      return props.group;
    }

    public int getY() {
      return props.y;
    }

    public int getX() {
      return props.x;
    }

    public int getPageY() {
      return props.pageY;
    }

    public int getPageX() {
      return props.pageX;
    }

    public Timeline getTimeline() {
      return (Timeline) getSource();
    }

    @Override
    public String toString() {
      return props.toString();
    }
  }

}
