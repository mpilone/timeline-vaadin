
package org.mpilone.vaadin.timeline;

import java.util.*;


/**
 * An item provider that simply keeps a list of {@link TimelineItem}s in memory.
 *
 * @author mpilone
 */
public class BasicItemProvider extends AbstractItemProvider implements
    TimelineItemProvider,
    TimelineItemProvider.ItemSetChangeNotifier,
    TimelineItemProvider.Editable {

  private final List<TimelineItem> items;

  /**
   * Constructs the provider.
   */
  public BasicItemProvider() {
    this.items = new ArrayList<>();
  }

  @Override
  public List<TimelineItem> doGetItems(Date startDate, Date endDate) {

    List<TimelineItem> result = new ArrayList<>();

    for (TimelineItem ti : items) {

      Date start = ti.getStart();
      Date end = ti.getEnd();

      boolean startInRange = start.compareTo(endDate) <= 0;
      boolean endInRange = true;
      if (end != null) {
        endInRange = end.compareTo(startDate) >= 0;
      }
      else {
        // No end date. Assume point data.
        startInRange = startInRange && start.compareTo(startDate) >= 0;
      }

      if (startInRange && endInRange) {
        result.add(ti);
      }
    }

    return result;
  }

  @Override
  public void addItem(TimelineItem item) {
    items.add(item);

    fireEvent(new ItemSetChangeEvent(this));
  }

  @Override
  public void removeItem(TimelineItem item) {
    items.remove(item);

    fireEvent(new ItemSetChangeEvent(this));
  }

}
