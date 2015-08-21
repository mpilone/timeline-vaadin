
package org.mpilone.vaadin.timeline;

import org.mpilone.vaadin.timeline.shared.TimelineClientRpc;

/**
 * Options used for individual method calls on the timeline.
 *
 * @author mpilone
 */
public class TimelineMethodOptions {

  /**
   * Prevent construction.
   */
  private TimelineMethodOptions() {
  }

  /**
   * Options for the setSelection method.
   */
  public static class SetSelection extends Fit {

    private final boolean focus;

    public SetSelection(boolean focus, Animation animation) {
      super(animation);
      this.focus = focus;
    }

    public boolean isFocus() {
      return focus;
    }
  }

  /**
   * Options for the setWindow method.
   */
  public static class SetWindow extends Fit {
    public SetWindow(Animation animation) {
      super(animation);
    }
  }

  /**
   * Options for the moveTo method.
   */
  public static class MoveTo extends Fit {

    public MoveTo(Animation animation) {
      super(animation);
    }
  }

  /**
   * Options for the focus method.
   */
  public static class Focus extends Fit {

    public Focus(Animation animation) {
      super(animation);
    }
  }

  /**
   * Options for the fit method.
   */
  public static class Fit {

    private final Animation animation;

    /**
     * Constructs the options.
     *
     * @param animation the animation options or null for no animation
     */
    public Fit(Animation animation) {
      this.animation = animation;
    }

    /**
     * Returns the animation to use for the operation. If null, no animation
     * will be used.
     *
     * @return the animation for the operation
     */
    public Animation getAnimation() {
      return animation;
    }
  }

  /**
   * Maps an {@link Animation} to a {@link TimelineClientRpc.Animation}.
   *
   * @param animation the animation to map or null
   *
   * @return the client RPC animation or null
   */
  static TimelineClientRpc.Animation map(Animation animation) {
    TimelineClientRpc.Animation rpcAnimation = null;

    if (animation != null) {
      rpcAnimation = new TimelineClientRpc.Animation();
      rpcAnimation.duration = animation.getDuration();
      rpcAnimation.easingFunction =
          animation.getEasingFunction() == null ? null : animation.
              getEasingFunction().name();
    }

    return rpcAnimation;
  }

}
