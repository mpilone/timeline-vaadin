package org.mpilone.vaadin.timeline;

/**
 * Animation options for an operation that moves the timeline.
 *
 * @author mpilone
 */
public class Animation {

  private final Integer duration;
  private final EasingFunction easingFunction;

  /**
   * Creates a default animation with a duration of 500ms and a
   * {@link EasingFunction#easeInOutQuad}.
   */
  public Animation() {
    this(500, EasingFunction.easeInOutQuad);
  }

  public Animation(Integer duration, EasingFunction easingFunction) {
    this.duration = duration;
    this.easingFunction = easingFunction;
  }

  public Integer getDuration() {
    return duration;
  }

  public EasingFunction getEasingFunction() {
    return easingFunction;
  }

  public static enum EasingFunction {

    linear,
    easeInQuad,
    easeOutQuad,
    easeInOutQuad,
    easeInCubic,
    easeOutCubic,
    easeInOutCubic,
    easeInQuart,
    easeOutQuart,
    easeInOutQuart,
    easeInQuint,
    easeOutQuint,
    easeInOutQuint;
  }

}
