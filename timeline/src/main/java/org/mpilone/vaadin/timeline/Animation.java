package org.mpilone.vaadin.timeline;

/**
 *
 * @author mpilone
 */
public class Animation {

  private final Integer duration;
  private final EasingFunction easingFunction;

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
