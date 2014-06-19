package org.mpilone.vaadin.timeline;

import java.util.*;

import com.vaadin.event.EventRouter;

/**
 * Base implementation of an item provider that can adjust the window to support
 * a window expansion factor and registration of item set change listeners.
 *
 * @author mpilone
 */
public abstract class AbstractItemProvider implements
    TimelineItemProvider,
    TimelineItemProvider.ItemSetChangeNotifier {

  private final EventRouter eventRouter;
  private float windowExpandFactor;

  /**
   * Constructs the provider.
   */
  public AbstractItemProvider() {
    this.eventRouter = new EventRouter();
    this.windowExpandFactor = 0.2f;
  }

  /**
   * Sets the amount that the window will be expanded by (half before, half
   * after) when getting items specified as decimal value. By specifying a value
   * greater than 0, events will be loaded outside the visible window which
   * should make scrolling and zooming a little nicer for the user because data
   * will be available immediately outside the window. A larger value means more
   * data to the user. The default is 0.2 (i.e. 20%).
   *
   * @param factor the amount to expand the window specified as a decimal
   * normally between 0 and 1.
   */
  public void setWindowExpandFactor(float factor) {
    this.windowExpandFactor = Math.max(factor, 0.0f);
  }

  @Override
  public List<TimelineItem> getItems(Date startDate, Date endDate) {

    long window = endDate.getTime() - startDate.getTime();
    long expandMillis = (long) (window * windowExpandFactor) / 2;

    startDate = new Date(startDate.getTime() - expandMillis);
    endDate = new Date(endDate.getTime() + expandMillis);

    return doGetItems(startDate, endDate);
  }

  /**
   * Performs the actual item retrieval (and possible conversion). This method
   * is called by {@link #getItems(java.util.Date, java.util.Date) } after the
   * start and end date have been adjusted to include the preload time.
   *
   * @param startDate the start date of the visible timeline window
   * @param endDate the end date of the visible timeline window
   *
   * @return the list of events intersecting the window
   */
  protected abstract List<TimelineItem> doGetItems(Date startDate, Date endDate);

  @Override
  public void addItemSetChangeListener(ItemSetChangeListener listener) {
    eventRouter.addListener(ItemSetChangeEvent.class, listener,
        ItemSetChangeListener.ITEM_SET_CHANGE_METHOD);
  }

  @Override
  public void removeItemSetChangeListener(ItemSetChangeListener listener) {
    eventRouter.addListener(ItemSetChangeEvent.class, listener,
        ItemSetChangeListener.ITEM_SET_CHANGE_METHOD);
  }

  /**
   * Fires the item set change event to all registered listeners.
   *
   * @param evt the event to fire
   */
  protected void fireEvent(ItemSetChangeEvent evt) {
    eventRouter.fireEvent(evt);
  }

}
