package com.googlecode.whatswrong.javautils;

import java.util.*;

/**
 * A HashMultiMapArrayList is a mapping from keys to array lists of values.
 *
 * @author Sebastian Riedel
 */
public class HashMultiMapArrayList<K, V> extends HashMap<K, List<V>> {
    /**
     * the empty list to be returned when there is key without values.
     */
    protected List<V> emptyList = Collections.unmodifiableList(new ArrayList<V>(0));

    /**
     * Adds a value to the list of values of the given key.
     *
     * @param key   the key value.
     * @param value the value to add to the list of values of the given key.
     */
    public void add(final K key, final V value) {
        List<V> list = get(key);
        if (list.size() == 0) {
            list = new ArrayList<V>();
            put(key, list);
        }
        list.add(value);
    }

    /**
     * Creates a deep copy of this mapping.
     *
     * @return A deep copy of this mapping.
     */
    public HashMultiMapArrayList<K, V> deepcopy() {
        HashMultiMapArrayList<K, V> result = new HashMultiMapArrayList<K, V>();
        for (Map.Entry<K, List<V>> entry : entrySet())
            result.put(entry.getKey(), new ArrayList<V>(entry.getValue()));
        return result;
    }


    /**
     * Returns the list of values associated with the given key.
     *
     * @param o the key to get the values for.
     * @return a list of values for the given keys or the empty list of no such value exist.
     */
    public List<V> get(final Object o) {
        List<V> result = super.get(o);
        return result == null ? emptyList : result;
    }
}
