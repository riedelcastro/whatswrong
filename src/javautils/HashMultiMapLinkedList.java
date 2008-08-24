package javautils;

import java.util.*;

/**
 * A HashMultiMapLinkedList is a mapping from keys to linked lists of associated
 * values.           
 *
 * @author Sebastian Riedel
 */
public class HashMultiMapLinkedList<K, V> extends HashMap<K, List<V>> {

  /**
   * the empty list to be returned when there is key without values.
   */
  protected List<V> emptyList = Collections.unmodifiableList(new LinkedList<V>());

  /**
   * Adds a value to the list of values of the given key.
   *
   * @param key   the key value.
   * @param value the value to add to the list of values of the given key.
   */
  public void add(final K key, final V value) {
    List<V> list = get(key);
    if (list.size() == 0) {
      list = new LinkedList<V>();
      put(key, list);
    }
    list.add(value);
  }

  /**
   * Creates a deep copy of this mapping.
   *
   * @return A deep copy of this mapping.
   */
  public HashMultiMapLinkedList<K, V> deepcopy() {
    HashMultiMapLinkedList<K, V> result = new HashMultiMapLinkedList<K, V>();
    for (Map.Entry<K, List<V>> entry : entrySet())
      result.put(entry.getKey(), new LinkedList<V>(entry.getValue()));
    return result;
  }


  /**
   * Returns the list of values associated with the given key.
   *
   * @param o the key to get the values for.
   * @return a list of values for the given keys or the empty list of no such
   *         value exist.
   */
  public List<V> get(final Object o) {
    List<V> result = super.get(o);
    return result == null ? emptyList : result;
  }
}
