
package org.mpilone.vaadin.timeline;

import static java.lang.String.format;

import java.util.TimeZone;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractJavaScriptExtension;

/**
 * <p>
 * An extension for {@link Timeline} that loads "moment-timezone" to enable time
 * zone support when constructing moment instances. Refer to
 * http://momentjs.com/timezone/ for more information on time zone support in
 * moment and http://visjs.org/docs/timeline/#Time_zone for more information on
 * configuring the moment constructor in Timeline.
 * </p>
 * <p>
 * This extension is used with {@link Timeline} when time zone support is needed
 * in the {@link TimelineOptions#setMoment(java.lang.String) }
 * function. This class will load an additional instance of moment.js as well as
 * moment-timezone.js with data from 2010 to 2020. Therefore it increases the
 * resource requirements of the page so it should only be used when time zone
 * support is required and the local time zone of the browser, a fixed TZ
 * offset, or UTC is not adequate.
 * </p>
 * <p>
 * If the extension is removed using the {@link #remove() } method, the last
 * configured moment value is left in the options. Therefore to completely
 * remove the time zone configuration you must remove this extension and reset
 * the option on the timeline to a new value (or null).
 * </p>
 *
 * @author mpilone
 */
@JavaScript(value = {"moment/moment.min.js",
  "moment-timezone/moment-timezone-with-data-1970-2030.min.js"})
public class TimelineTimeZone extends AbstractJavaScriptExtension {

  private final Timeline timeline;

  /**
   * Constructs the extension.
   *
   * @param timeline the timeline to extend
   */
  private TimelineTimeZone(Timeline timeline) {
    super(timeline);
    this.timeline = timeline;
  }

  /**
   * Extends the given timeline by adding time zone support. The given time zone
   * will be immediately applied.
   *
   * @param timeline the timeline to extend
   * @param timeZoneId the ID time zone to apply to the timeline (e.g.
   * "America/New_York")
   *
   * @return the new extension instance
   */
  public static TimelineTimeZone extend(Timeline timeline, String timeZoneId) {
    final TimelineTimeZone e = new TimelineTimeZone(timeline);
    e.setTimeZone(timeZoneId);

    return e;
  }

  /**
   * A convenience method for calling
   * <code>extend(timeline, timeZone.getId())</code>.
   *
   * @param timeline the timeline to extend
   * @param timeZone the time zone to apply to the timeline
   *
   * @return the new extension instance
   */
  public static TimelineTimeZone extend(Timeline timeline, TimeZone timeZone) {
    return extend(timeline, timeZone.getID());
  }

  /**
   * <p>
   * Calls {@link TimelineOptions#setMoment(java.lang.String) } with a
   * JavaScript function that will properly initialize the moment with the given
   * named time zone. It is possible to set the 'moment' property directly on
   * the original timeline if more customization is required. The only
   * requirement is that the page level 'moment' instance is used rather than
   * the 'vis.moment' instance because the former will be the one enhanced with
   * TZ support.
   * </p>
   * <p>
   * Note that moment-timezone defines its own time zone IDs and therefore may
   * not support the same set of IDs as the {@link TimeZone} class. Be sure to
   * verify that a particular ID is supported by the moment library before using
   * it. Do not attempt to blindly map all Java TZs to the timeline.
   * <p>
   * For example: <code>extension.setTimeZone("America/Los_Angeles");</code>
   * </p>
   *
   * @param timeZoneId the ID of time zone to apply to the timeline (e.g.
   * "America/New_York")
   */
  public void setTimeZone(String timeZoneId) {
    timeline.getOptions().setMoment(format(
        "function(date) { return moment(date).tz('%s'); }", timeZoneId));
  }

  /**
   * A convenience method for calling
   * <code>setTimeZone(timeZone.getId())</code>.
   *
   * @param timeZone the time zone to apply to the timeline
   */
  public void setTimeZone(TimeZone timeZone) {
    setTimeZone(timeZone.getID());
  }
}
