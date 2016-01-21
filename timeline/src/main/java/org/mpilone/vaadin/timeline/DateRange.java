
package org.mpilone.vaadin.timeline;

import java.util.Date;
import java.util.Objects;

/**
 * A date range with a start and end.
 */
public class DateRange {
  private final Date start;
  private final Date end;

  /**
   * Constructs the date range.
   *
   * @param start the start date
   * @param end the end date
   */
  public DateRange(Date start, Date end) {
    this.start = start;
    this.end = end;
  }

  /**
   * Returns the start date.
   *
   * @return the start date
   */
  public Date getStart() {
    return start;
  }

  /**
   * Returns the end date.
   *
   * @return the end date
   */
  public Date getEnd() {
    return end;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 59 * hash + Objects.hashCode(this.start);
    hash = 59 * hash + Objects.hashCode(this.end);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DateRange other = (DateRange) obj;
    if (!Objects.equals(this.start, other.start)) {
      return false;
    }
    if (!Objects.equals(this.end, other.end)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "DateRange{" + "start=" + start + ", end=" + end + '}';
  }

}
