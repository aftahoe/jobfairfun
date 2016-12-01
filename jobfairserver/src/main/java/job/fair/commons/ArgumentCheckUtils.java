package job.fair.commons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Provides convenient utility methods validating arguments (to methods or constructors).
 * <p>
 * Created by annawang on 2/22/16.
 */
public class ArgumentCheckUtils {

    public static final SortedSet<Object> EMPTY_SORTED_SET = Collections.unmodifiableSortedSet(new TreeSet<Object>());

    /**
     * Private constructor which prevents accidentally instantiating the class.
     */
    private ArgumentCheckUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <E> SortedSet<E> emptySortedSet() {
        return (SortedSet<E>) EMPTY_SORTED_SET;
    }

    /**
     * Marks a property as required. If value is <code>null</code>,
     * then a <code>NullPointerException</code> is thrown.
     *
     * @param name  The name of the property (as it will appear in any error messages)
     * @param value the value of the property
     * @param <T>   type of value
     * @return value  *
     * @throws NullPointerException if value is <code>null</code>
     */
    public static <T> T require(String name, T value) {
        if (value == null) {
            throw new NullPointerException(name + " is required and cannot be null.");
        }
        return value;
    }


    /**
     * Marks a String property as required and as non-empty.
     *
     * @param name  The name of the property (as it will appear in any error messages)
     * @param value the value of the property
     * @param <T>   type of value
     * @return the original value
     * @throws NullPointerException     if value is <code>null</code>
     * @throws IllegalArgumentException if value is empty.
     */
    public static <T extends CharSequence> T requireNonEmptyValue(String name, T value) {
        require(name, value);
        if (value.length() == 0) {
            throw new IllegalArgumentException(name + " is required and cannot be empty.");
        }
        return value;
    }

    /**
     * Marks a String property as optional. If the value is <code>null</code>, then empty string ("") is returned.
     * Otherwise, the String's value is returned.
     *
     * @param value the value to to test
     * @return empty string ("") or <code>value</code>
     */
    public static String optional(String value) {
        return optional(value, "");
    }

    /**
     * Marks an object property as optional. If the value is <code>null</code>, then the given default value is
     * returned.
     *
     * @param <T>          type of value
     * @param value        the value of the property
     * @param defaultValue default value of the property
     * @return the original value
     */
    public static <T> T optional(T value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Marks a Collection property as optional. If the value is <code>null</code>, then an immutable empty list is returned.
     * Otherwise, the collection is returned unmodified.
     *
     * @param <T>        type of value
     * @param collection the list to mark as optional
     * @return the list, or if <code>null</code>, an empty list
     */

    public static <T> Collection<? extends T> optional(Collection<? extends T> collection) {
        if (collection == null) {
            return Collections.emptyList();
        }
        return collection;
    }

    /**
     * Marks a Map property as optional. If the value is <code>null</code>, then an immutable empty map is returned.
     * Otherwise an unmodified version of the map is returned.
     *
     * @param <K> type of key
     * @param <V> type of value
     * @param map the map to mark as optional
     * @return the map, or if <code>null</code>, an empty map
     */
    public static <K, V> Map<? extends K, ? extends V> optional(Map<? extends K, ? extends V> map) {
        if (map == null) {
            return Collections.emptyMap();
        }
        return map;
    }

    /**
     * Creates an immutable copy of the given list backed by a new LinkedList.
     *
     * @param <T>      type of value
     * @param original original collection
     * @return new copied collection
     */
    public static <T> List<T> anImmutableLinkedListCopyOf(Collection<? extends T> original) {
        return Collections.unmodifiableList(new LinkedList<T>(original));
    }

    /**
     * Creates an immutable copy of the given list backed by a new ArrayList.
     *
     * @param <T>      type of value
     * @param original original collection
     * @return new copied collection
     */
    public static <T> List<T> anImmutableArrayListCopyOf(Collection<? extends T> original) {
        return Collections.unmodifiableList(new ArrayList<T>(original));
    }

    /**
     * Creates an immutable copy of the given map backed by a new HashMap.
     *
     * @param <K>      type of key
     * @param <V>      type of value
     * @param original the original map
     * @return the map, or if <code>null</code>, an empty map
     */
    public static <K, V> Map<K, V> anImmutableHashMapCopyOf(Map<? extends K, ? extends V> original) {
        return Collections.unmodifiableMap(new HashMap<K, V>(original));
    }

    /**
     * Creates an immutable copy of the given map backed by a new LinkedHashMap.
     *
     * @param <K>      type of key
     * @param <V>      type of value
     * @param original the original map
     * @return the map, or if <code>null</code>, an empty map
     */
    public static <K, V> Map<K, V> anImmutableLinkedHashMapCopyOf(Map<? extends K, ? extends V> original) {
        return Collections.unmodifiableMap(new LinkedHashMap<K, V>(original));
    }

    /**
     * Creates an immutable copy of the given sorted set backed by a new TreeSet.
     *
     * @param <T>      type of value
     * @param original original collection
     * @return new copied collection
     */
    public static <T> SortedSet<T> anImmutableTreeSetCopyOf(Collection<? extends T> original) {
        return Collections.unmodifiableSortedSet(new TreeSet<T>(original));
    }

    /**
     * Creates an immutable copy of the given sorted set backed by a new TreeSet.
     *
     * @param <T>      type of value
     * @param original original collection
     * @return new copied collection
     */
    public static <T> Set<T> anImmutableHashSetCopyOf(Collection<? extends T> original) {
        return Collections.unmodifiableSet(new HashSet<T>(original));
    }
}
