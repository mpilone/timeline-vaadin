
package org.mpilone.vaadin.timeline;

import java.util.Date;
import java.util.List;

/**
 *
 * @author mpilone
 */
public interface TimelineItemProvider {

  public static class ItemSetChangeEvent {

    private final TimelineItemProvider provider;

    public ItemSetChangeEvent(TimelineItemProvider provider) {
      this.provider = provider;
    }

    public TimelineItemProvider getProvider() {
      return provider;
    }
  }

  public interface ItemSetChangeListener {

    public void itemSetChange(ItemSetChangeEvent evt);
  }

  public interface ItemSetChangeNotifier {

    public void addItemSetChangeListener(ItemSetChangeListener listener);

    public void removeItemSetChangeListener(ItemSetChangeListener listener);
  }

  public interface Editable {

    public void addItem(TimelineItem item);

    public void removeItem(TimelineItem item);
  }

  public List<TimelineItem> getItems(Date startDate, Date endDate);
}
