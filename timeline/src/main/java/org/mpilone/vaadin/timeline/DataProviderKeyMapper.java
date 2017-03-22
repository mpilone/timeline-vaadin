
package org.mpilone.vaadin.timeline;

import java.io.Serializable;
import java.util.*;

/**
 * ItemId to Key to ItemId mapper.
 * <p>
 * This class is a copy of the implementation at
 * com.vaadin.data.RpcDataProviderExtension but with package protected methods
 * exposed.</p>
 *
 * @author mpilone
 */
class DataProviderKeyMapper implements Serializable {

  private final Map<Object, String> itemIdToKey = new HashMap();
  private final Set<Object> pinnedItemIds = new HashSet<>();
  private long rollingIndex = 0;

  public DataProviderKeyMapper() {
    // private implementation
  }

  /**
   * Sets the currently active rows. This will purge any unpinned rows from
   * cache.
   *
   * @param itemIds collection of itemIds to map to row keys
   */
  public void setActiveRows(Collection<?> itemIds) {
    Set<Object> itemSet = new HashSet<>(itemIds);
    Set<Object> itemsRemoved = new HashSet<>();
    for (Object itemId : itemIdToKey.keySet()) {
      if (!itemSet.contains(itemId) && !isPinned(itemId)) {
        itemsRemoved.add(itemId);
      }
    }

    for (Object itemId : itemsRemoved) {
      itemIdToKey.remove(itemId);
    }

    for (Object itemId : itemSet) {
      itemIdToKey.put(itemId, getKey(itemId));
    }
  }

  private String nextKey() {
    return String.valueOf(rollingIndex++);
  }

  public String getKey(Object itemId) {
    String key = itemIdToKey.get(itemId);
    if (key == null) {
      key = nextKey();
      itemIdToKey.put(itemId, key);
    }
    return key;
  }

  /**
   * Gets keys for a collection of item ids.
   * <p>
   * If the itemIds are currently cached, the existing keys will be used.
   * Otherwise new ones will be created.
   *
   * @param itemIds the item ids for which to get keys
   * @return keys for the {@code itemIds}
   */
  public List<String> getKeys(Collection<Object> itemIds) {
    if (itemIds == null) {
      throw new IllegalArgumentException("itemIds can't be null");
    }

    ArrayList<String> keys = new ArrayList<String>(itemIds.size());
    for (Object itemId : itemIds) {
      keys.add(getKey(itemId));
    }
    return keys;
  }

  /**
   * Gets the registered item id based on its key.
   * <p>
   * A key is used to identify a particular row on both a server and a client.
   * This method can be used to get the item id for the row key that the client
   * has sent.
   *
   * @param key the row key for which to retrieve an item id
   * @return the item id corresponding to {@code key}
   * @throws IllegalStateException if the key mapper does not have a record of
   * {@code key} .
   */
  public Object getItemId(String key) throws IllegalStateException {
    Object itemId = getByValue(itemIdToKey, key);
    if (itemId != null) {
      return itemId;
    } else {
      throw new IllegalStateException("No item id for key " + key
          + " found.");
    }
  }

  private <K, V> K getByValue(Map<K, V> map, V value) {
    return map.entrySet().stream().filter(entry -> {
      return Objects.equals(entry.getValue(), value);
    }).map(entry -> entry.getKey()).findFirst().orElse(null);

  }

  /**
   * Gets corresponding item ids for each of the keys in a collection.
   *
   * @param keys the keys for which to retrieve item ids
   * @return a collection of item ids for the {@code keys}
   * @throws IllegalStateException if one or more of keys don't have a
   * corresponding item id in the cache
   */
  public Collection<Object> getItemIds(Collection<String> keys)
      throws IllegalStateException {
    if (keys == null) {
      throw new IllegalArgumentException("keys may not be null");
    }

    ArrayList<Object> itemIds = new ArrayList<Object>(keys.size());
    for (String key : keys) {
      itemIds.add(getItemId(key));
    }
    return itemIds;
  }

  /**
   * Pin an item id to be cached indefinitely.
   * <p>
   * Normally when an itemId is not an active row, it is discarded from the
   * cache. Pinning an item id will make sure that it is kept in the cache.
   * <p>
   * In effect, while an item id is pinned, it always has the same key.
   *
   * @param itemId the item id to pin
   * @throws IllegalStateException if {@code itemId} was already pinned
   * @see #unpin(Object)
   * @see #isPinned(Object)
   * @see #getItemIds(Collection)
   */
  public void pin(Object itemId) throws IllegalStateException {
    if (isPinned(itemId)) {
      throw new IllegalStateException("Item id " + itemId
          + " was pinned already");
    }
    pinnedItemIds.add(itemId);
  }

  /**
   * Unpin an item id.
   * <p>
   * This cancels the effect of pinning an item id. If the item id is currently
   * inactive, it will be immediately removed from the cache.
   *
   * @param itemId the item id to unpin
   * @throws IllegalStateException if {@code itemId} was not pinned
   * @see #pin(Object)
   * @see #isPinned(Object)
   * @see #getItemIds(Collection)
   */
  public void unpin(Object itemId) throws IllegalStateException {
    if (!isPinned(itemId)) {
      throw new IllegalStateException("Item id " + itemId
          + " was not pinned");
    }

    pinnedItemIds.remove(itemId);
  }

  /**
   * Checks whether an item id is pinned or not.
   *
   * @param itemId the item id to check for pin status
   * @return {@code true} iff the item id is currently pinned
   */
  public boolean isPinned(Object itemId) {
    return pinnedItemIds.contains(itemId);
  }
}
