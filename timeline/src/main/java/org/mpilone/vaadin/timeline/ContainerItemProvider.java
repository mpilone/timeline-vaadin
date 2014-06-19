package org.mpilone.vaadin.timeline;

import java.util.*;

import com.vaadin.data.*;

/**
 * <p>
 * A {@link TimelineItemProvider} that adapts a standard Vaadin
 * {@link Container} by extracting the {@link TimelineItem} values from item
 * properties. Default property IDs are provided that map into the
 * {@link BasicTimelineItem} but the property IDs can be customized to match the
 * properties in the container items.
 * </p>
 * <p>
 * The provider supports two optimizations. The {@code containerSorted} field
 * can be set to true to indicate that the items will be in sorted order by
 * ascending start date. When true, the provider will stop searching for items
 * once a start date is found to be out of range. The provider will also
 * recognized if a container implements {@link Container.Indexed} and use the
 * {@link Container.Indexed#getIdByIndex(int)} method to smartly iterate through
 * the container.
 * </p>
 *
 * @author mpilone
 */
public class ContainerItemProvider extends AbstractItemProvider implements
    TimelineItemProvider,
    TimelineItemProvider.ItemSetChangeNotifier,
    Container.ItemSetChangeListener {

  // Default property ids
  public static final String CONTENT_PROPERTY = "content";
  public static final String STARTDATE_PROPERTY = "start";
  public static final String ENDDATE_PROPERTY = "end";
  public static final String STYLENAME_PROPERTY = "styleName";
  public static final String GROUPID_PROPERTY = "groupId";
  public static final String TYPE_PROPERTY = "type";

  private final Map<String, Object> propertyIdMap;
  private boolean containerSorted;

  /**
   * The container used as datasource.
   */
  private Container container;

  /**
   * Constructs the provider which will adapt the items in the container to
   * timeline items.
   *
   * @param container the backing container
   */
  public ContainerItemProvider(Container container) {
    setContainerDataSource(container);

    propertyIdMap = new HashMap<>(6);
    propertyIdMap.put(CONTENT_PROPERTY, CONTENT_PROPERTY);
    propertyIdMap.put(STARTDATE_PROPERTY, STARTDATE_PROPERTY);
    propertyIdMap.put(ENDDATE_PROPERTY, ENDDATE_PROPERTY);
    propertyIdMap.put(STYLENAME_PROPERTY, STYLENAME_PROPERTY);
    propertyIdMap.put(GROUPID_PROPERTY, GROUPID_PROPERTY);
    propertyIdMap.put(TYPE_PROPERTY, TYPE_PROPERTY);
  }

  /**
   * Sets the container data source to be adapted to container items.
   *
   * @param container the backing container
   */
  public void setContainerDataSource(Container container) {
    if (container == null) {
      throw new IllegalArgumentException(
          "A container is required and must not be null.");
    }

    if (this.container instanceof Container.ItemSetChangeNotifier) {
      ((Container.ItemSetChangeNotifier) this.container).
          removeItemSetChangeListener(this);
    }

    this.container = container;

    if (this.container instanceof Container.ItemSetChangeNotifier) {
      ((Container.ItemSetChangeNotifier) this.container).
          addItemSetChangeListener(this);
    }
  }

  @Override
  public List<TimelineItem> doGetItems(Date startDate, Date endDate) {
    if (container instanceof Container.Indexed) {
      return getItemsIndexed(startDate, endDate);
    }
    else {
      return getItemsNotIndexed(startDate, endDate);
    }
  }

  /**
   * Returns all the items intersecting the given range from a non-indexed
   * container. This is done by iterating over the item IDs in the container.
   *
   * @param startDate the start date of the range
   * @param endDate the end date of the range
   *
   * @return the timeline items that intersect the range
   */
  private List<TimelineItem> getItemsNotIndexed(Date startDate, Date endDate) {

    List<TimelineItem> result = new ArrayList<>();

    Collection<?> propIds = container.getContainerPropertyIds();
    for (Object itemId : container.getItemIds()) {
      Item item = container.getItem(itemId);

      int c = compareTo(propIds, item, startDate, endDate);

      if (c == 0) {
        result.add(convertToTimelineItem(itemId, item));
      }
      else if (c > 0 && containerSorted) {
        // End early because start is out of range and the container is sorted.
        break;
      }
    }

    return result;

  }

  /**
   * Returns all the items intersecting the given range from an indexed
   * container. This is done by getting the item IDs using the item index.
   *
   * @param startDate the start date of the range
   * @param endDate the end date of the range
   *
   * @return the timeline items that intersect the range
   */
  private List<TimelineItem> getItemsIndexed(Date startDate, Date endDate) {

    List<TimelineItem> result = new ArrayList<>();

    Container.Indexed indexedContainer = (Container.Indexed) container;

    Collection<?> propIds = indexedContainer.getContainerPropertyIds();
    int size = indexedContainer.size();
    for (int index = 0; index < size; ++index) {

      Object itemId = indexedContainer.getIdByIndex(index);
      Item item = indexedContainer.getItem(itemId);

      int c = compareTo(propIds, item, startDate, endDate);

      if (c == 0) {
        result.add(convertToTimelineItem(itemId, item));
      }
      else if (c > 0 && containerSorted) {
        // End early because start is out of range and the container is sorted.
        break;
      }
    }

    return result;
  }

  /**
   * Compares the given item to the date range and returns less than 0 if the
   * item is before the range, 0 if the item intersects the range, and greater
   * than 0 if the item is after the range.
   *
   * @param propIds the property IDs on the item/container
   * @param item the item to compare
   * @param startDate the start of the date range to compare with
   * @param endDate the end of the date range to compare with
   *
   * @return less than 0 if the item is before the range, 0 if the item
   * intersects the range, and greater than 0 if the item is after the range
   */
  private int compareTo(Collection<?> propIds, Item item, Date startDate,
      Date endDate) {

    Object propId = propertyIdMap.get(STARTDATE_PROPERTY);
    Date start = (Date) item.getItemProperty(propId).getValue();

    propId = propertyIdMap.get(ENDDATE_PROPERTY);
    Date end = propIds.contains(propId) ? (Date) item.getItemProperty(propId).
        getValue() : null;

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
      // Intersecting the range.
      return 0;
    }
    else if (!endInRange) {
      // Before the range.
      return -1;
    }
    else {
      // After the range.
      return 1;
    }
  }

  /**
   * Converts the container item to a timeline item using the property ID map to
   * locate the properties in the item.
   *
   * @param itemId the ID of the container item
   * @param item the container item to convert
   *
   * @return the new timeline item built from the container item
   */
  private TimelineItem convertToTimelineItem(Object itemId, Item item) {
    BasicTimelineItem ti = new BasicTimelineItem();

    Collection<?> propIds = item.getItemPropertyIds();

    // ID
    ti.setId(itemId);

    // Content
    Object propId = propertyIdMap.get(CONTENT_PROPERTY);
    if (propIds.contains(propId)) {
      ti.setContent(String.valueOf(item.getItemProperty(propId).getValue()));
    }

    // Start date
    propId = propertyIdMap.get(STARTDATE_PROPERTY);
    if (propIds.contains(propId)) {
      ti.setStart((Date) item.getItemProperty(propId).getValue());
    }

    // End date
    propId = propertyIdMap.get(ENDDATE_PROPERTY);
    if (propIds.contains(propId)) {
      ti.setEnd((Date) item.getItemProperty(propId).getValue());
    }

    // Style name
    propId = propertyIdMap.get(STYLENAME_PROPERTY);
    if (propIds.contains(propId)) {
      ti.setStyleName(String.valueOf(item.getItemProperty(propId).getValue()));
    }

    // Group ID
    propId = propertyIdMap.get(GROUPID_PROPERTY);
    if (propIds.contains(propId)) {
      ti.setGroupId(String.valueOf(item.getItemProperty(propId).getValue()));
    }

    // Type
    propId = propertyIdMap.get(TYPE_PROPERTY);
    if (propIds.contains(propId)) {
      ti.setType((TimelineOptions.ItemType) item.getItemProperty(propId).
          getValue());
    }

    return ti;
  }

  @Override
  public void containerItemSetChange(Container.ItemSetChangeEvent event) {
    fireEvent(new ItemSetChangeEvent(this));
  }

  /**
   * Sets the ID of the property in the container's items to use for the
   * {@link TimelineItem#getContent()} value of the timeline item.
   *
   * @param propertyId the ID of the content property
   */
  public void setContentPropertyId(Object propertyId) {
    propertyIdMap.put(CONTENT_PROPERTY, propertyId);
  }

  /**
   * Sets the ID of the property in the container's items to use for the
   * {@link TimelineItem#getStart()} value of the timeline item.
   *
   * @param propertyId the ID of the content property
   */
  public void setStartDatePropertyId(Object propertyId) {
    propertyIdMap.put(STARTDATE_PROPERTY, propertyId);
  }

  /**
   * Sets the ID of the property in the container's items to use for the
   * {@link TimelineItem#getEnd()} value of the timeline item.
   *
   * @param propertyId the ID of the content property
   */
  public void setEndDatePropertyId(Object propertyId) {
    propertyIdMap.put(ENDDATE_PROPERTY, propertyId);
  }

  /**
   * Sets the ID of the property in the container's items to use for the
   * {{@link TimelineItem#getStyleName()} value of the timeline item.
   *
   * @param propertyId the ID of the content property
   */
  public void setStyleNamePropertyId(Object propertyId) {
    propertyIdMap.put(STYLENAME_PROPERTY, propertyId);
  }

  /**
   * Sets the ID of the property in the container's items to use for the
   * {{@link TimelineItem#getGroupId()} value of the timeline item.
   *
   * @param propertyId the ID of the content property
   */
  public void setGroupIdPropertyId(Object propertyId) {
    propertyIdMap.put(GROUPID_PROPERTY, propertyId);
  }

  /**
   * Sets the ID of the property in the container's items to use for the
   * {{@link TimelineItem#getType()} value of the timeline item.
   *
   * @param propertyId the ID of the content property
   */
  public void setTypePropertyId(Object propertyId) {
    propertyIdMap.put(TYPE_PROPERTY, propertyId);
  }
}
