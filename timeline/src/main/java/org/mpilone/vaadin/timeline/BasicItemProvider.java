
package org.mpilone.vaadin.timeline;

import java.lang.reflect.Method;
import java.util.*;

import com.vaadin.event.EventRouter;

/**
 *
 * @author mpilone
 */
public class BasicItemProvider extends EventRouter implements
    TimelineItemProvider,
    TimelineItemProvider.ItemSetChangeNotifier,
    TimelineItemProvider.Editable {

  private static final Method CHANGE_METHOD;

  static {
    try {
      CHANGE_METHOD = ItemSetChangeListener.class.getMethod("itemSetChange",
          ItemSetChangeEvent.class);
    }
    catch (NoSuchMethodException | SecurityException ex) {
      throw new RuntimeException("Unable to find listener method.", ex);
    }
  }

  private final List<TimelineItem> items;

  public BasicItemProvider() {
    this.items = new ArrayList<>();
  }

  @Override
  public List<TimelineItem> getItems(Date startDate, Date endDate) {

    long window = endDate.getTime() - startDate.getTime();
    long preloadWindow = (long) (window * .1);

    startDate = new Date(startDate.getTime() - preloadWindow);
    endDate = new Date(endDate.getTime() + preloadWindow);

    List<TimelineItem> result = new ArrayList<>();

    for (TimelineItem item : items) {
      boolean endInRange = true;

      if (item.getEnd() != null) {
        endInRange = item.getEnd().compareTo(startDate) >= 0;
      }

      boolean startInRange = item.getStart().compareTo(endDate) <= 0;

      if (startInRange && endInRange) {
        result.add(item);
      }
    }

    return result;
  }

  @Override
  public void addItemSetChangeListener(ItemSetChangeListener listener) {
    addListener(ItemSetChangeEvent.class, listener, CHANGE_METHOD);
  }

  @Override
  public void removeItemSetChangeListener(ItemSetChangeListener listener) {
    removeListener(ItemSetChangeEvent.class, listener, CHANGE_METHOD);
  }

  @Override
  public void addItem(TimelineItem item) {
    items.add(item);
  }

  @Override
  public void removeItem(TimelineItem item) {
    items.remove(item);
  }

}
