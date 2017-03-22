package org.mpilone.vaadin.timeline;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.EventObject;
import java.util.List;

import com.vaadin.util.ReflectTools;

/**
 * A provider of {@link TimelineItem} instances for a given date range. A
 * provider is normally set on a {@link Timeline} to supply the timeline with
 * data to be displayed. The provider could contain a static list of items or it
 * could be backed by a database.
 *
 * @author mpilone
 */
public interface TimelineItemProvider {

  /**
   * Retrieves the items intersecting the visible timeline window.
   *
   * @param startDate the start date of the visible timeline window
   * @param endDate the end date of the visible timeline window
   *
   * @return the list of events intersecting the window
   */
  public List<TimelineItem> getItems(Date startDate, Date endDate);

  /**
   * The item set change event which indicates that items in the provider have
   * been added or removed.
   */
  public static class ItemSetChangeEvent extends EventObject {

    /**
     * Constructs the event.
     *
     * @param provider the provider that fired the event
     */
    public ItemSetChangeEvent(TimelineItemProvider provider) {
      super(provider);
    }

    @Override
    public TimelineItemProvider getSource() {
      return (TimelineItemProvider) super.getSource();
    }
  }

  /**
   * The listener to be notified when items are added or removed from a timeline
   * item provider.
   */
  public interface ItemSetChangeListener {

    /**
     * The event handling method on the {@link ItemSetChangeListener}.
     */
    public static final Method ITEM_SET_CHANGE_METHOD = ReflectTools.findMethod(
        ItemSetChangeListener.class, "itemSetChange", ItemSetChangeEvent.class);

    /**
     * The method called when the item set in a provider changes.
     *
     * @param evt the details of the change
     */
    public void itemSetChange(ItemSetChangeEvent evt);
  }

  /**
   * A notifier that can fire item set change events.
   */
  public interface ItemSetChangeNotifier {

    /**
     * Adds a listener to be notified of item set changes.
     *
     * @param listener the listener to add
     */
    public void addItemSetChangeListener(ItemSetChangeListener listener);

    /**
     * Removes a listener that was being notified of item set changes.
     *
     * @param listener the listener to remove
     */
    public void removeItemSetChangeListener(ItemSetChangeListener listener);
  }

  /**
   * An item provider that can be edited by having items added or removed. It is
   * recommended that all editable providers also be
   * {@link ItemSetChangeNotifier}s so the timeline is aware of any edits.
   */
  public interface Editable {

    /**
     * Adds an item to the provider.
     *
     * @param item the item to add
     */
    public void addItem(TimelineItem item);

    /**
     * Removes an item from the provider.
     *
     * @param item the item to remove
     */
    public void removeItem(TimelineItem item);
  }

 
}
