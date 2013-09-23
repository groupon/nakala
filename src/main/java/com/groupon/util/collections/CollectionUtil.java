/*
Copyright (c) 2013, Groupon, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.

Neither the name of GROUPON nor the names of its contributors may be
used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.groupon.util.collections;

import org.apache.commons.lang3.ObjectUtils;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author alasdair@groupon.com
 */
public abstract class CollectionUtil {

    public static double sum(Collection<? extends Number> numbers) {
        double sum = 0d;
        for (Number n : numbers)
            sum += n.doubleValue();
        return sum;
    }

    /**
     * return true of the parameter is null or empty
     *
     * @param objs
     * @return
     */

    public static boolean isEmpty(Iterable<? extends Object> objs) {
        if (objs == null) {
            return false;
        }
        return !(objs.iterator().hasNext());
    }

    /**
     * Add objects to a collection... mostly used when an object instantiation is in the first paramter.
     */
    public static <T extends Object, C extends Collection<? super T>> C add(final C cxn, final T... objs) {
        if (objs != null) {
            for (final T obj : objs) {
                cxn.add(obj);
            }
        }
        return cxn;
    }

    public static <T extends Object, C extends Collection<? super T>> C add(final C cxn, Iterable<T> from) {
        if (from != null) {
            for (T obj : from) {
                cxn.add(obj);
            }
        }
        return cxn;
    }

    /**
     * Add objects to a collection... mostly used when an object instantiation is in the first paramter.
     */
    public static <C extends Collection<Integer>> C ints(final C cxn, final int... objs) {
        if (objs != null) {
            for (final int obj : objs) {
                cxn.add(obj);
            }
        }
        return cxn;
    }

    /**
     * Pass in String key/value pairs to add to a hash map
     *
     * @param keyVals
     * @return
     */
    public static Map<String, String> hashMap(final String... keyVals) {
        if (keyVals.length % 2 == 1) {
            throw new IllegalArgumentException("Must supply even number of arguments: " + keyVals.length);
        }
        final Map<String, String> m = new HashMap<String, String>();
        for (int i = 0; i < keyVals.length; i += 2) {
            m.put(keyVals[i], keyVals[i + 1]);
        }
        return m;
    }

    /**
     * add a bunch of objects to a hash set.
     *
     * @param <T>
     * @param objs
     * @return
     */
    public static <T> Set<T> hashSet(final T... objs) {
        final Set<T> r = new HashSet<T>();
        for (final T obj : objs) {
            r.add(obj);
        }
        return r;
    }

    public static <T> Set<T> hashSetFromArray(final T[] objs) {
        final Set<T> set = new HashSet<T>();
        for (final T obj : objs) {
            set.add(obj);
        }
        return set;
    }

    /**
     * Join a bunch of objects into a string delimited by the separator
     *
     * @param sep
     * @param c
     * @return
     */
    public static String join(final String sep, final Collection<? extends Object> c) {
        if (c == null) {
            return null;
        }
        if (c.size() == 1) {
            return String.valueOf(c.iterator().next());
        }
        if (c.size() == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        String s = "";
        for (final Object o : c) {
            sb.append(s).append(String.valueOf(o));
            s = sep;
        }
        return sb.toString();
    }

    /**
     * Join a bunch of objects into a string delimited by the separator
     *
     * @param sep
     * @param ol
     * @return
     */
    public static String join(final String sep, final Object... ol) {
        if (ol == null || ol.length == 0) {
            return "";
        }
        if (ol.length == 1) {
            return String.valueOf(ol[0]);
        }
        final StringBuilder sb = new StringBuilder();
        String s = "";
        for (final Object o : ol) {
            sb.append(s).append(String.valueOf(o));
            s = sep;
        }
        return sb.toString();
    }

    /**
     * @param sep
     * @param sl
     * @return
     */
    public static String join(final String sep, final String... sl) {
        return join(sep, (Object[]) sl);
    }

    /**
     * @param <K>
     * @param <V>
     * @param src
     * @param map
     * @return
     */
    public static <K, V> List<V> map(final Collection<K> src, final Mapper<K, V> map) {
        return map(src, map, new LinkedList<V>());
    }

    /**
     * @param <K>
     * @param <V>
     * @param <C>
     * @param src
     * @param map
     * @param dst
     * @return
     */
    public static <K, V, C extends Collection<V>> C map(final Collection<K> src, final Mapper<K, V> map, final C dst) {
        for (final K k : src) {
            dst.add(map.map(k));
        }
        return dst;
    }

    public static <V> List<V> select(Collection<V> src, Predicate<V> filter) {
        return select(src, filter, new ArrayList<V>(src.size()));
    }

    public static <V, C extends Collection<V>> C select(Collection<V> src, Predicate<V> filter, C into) {
        if (src == into) {
            filter(src, filter);
        } else {
            for (V v : src) {
                if (filter.matches(v)) {
                    into.add(v);
                }
            }
        }
        return into;
    }

    public static <V> void filter(Collection<V> c, Predicate<V> filter) {
        for (Iterator<V> i = c.iterator(); i.hasNext(); ) {
            if (!filter.matches(i.next())) {
                i.remove();
            }
        }
    }

    /**
     * @param regex
     * @param value
     * @return
     */
    public static Collection<String> split(final String regex, final String value) {
        return split(regex, value, new LinkedList<String>());
    }

    /**
     * @param <T>
     * @param regex
     * @param value
     * @param l
     * @return
     */
    public static <T extends Collection<String>> T split(final String regex, final String value, final T l) {
        final String[] bits = value.split(regex);
        for (final String bit : bits) {
            l.add(bit);
        }
        return l;
    }

    /**
     * @param c
     * @return
     */
    public static int[] toIntArray(final Collection<? extends Number> c) {
        if (c == null) {
            return null;
        }
        final int[] r = new int[c.size()];
        int i = 0;
        for (final Number n : c) {
            r[i++] = n.intValue();
        }
        return r;
    }

    /**
     * Create a new array by pulling a slice out of an existing one.
     *
     * @param <T>   the type of the array elements
     * @param array the array
     * @param off   the start offset of the slice
     * @return a subset of the array from the offset to the end of the array
     */
    public static <T extends Object> T[] slice(T[] array, int off) {
        return slice(array, off, array.length - off);
    }

    /**
     * Create a new array by slicing a subset out of an existing one.
     *
     * @param <T>   the type of the array elements
     * @param array the array
     * @param off   the starting offset of the slice
     * @param len   the length of the slice.
     * @return a slice of the array starting at the given offset and with the specified length
     */
    public static <T extends Object> T[] slice(T[] array, int off, int len) {
        len = Math.min(len, array.length - off);
        T[] res = (T[]) Array.newInstance(array.getClass().getComponentType(), len);
        System.arraycopy(array, off, res, 0, len);
        return res;
    }

    public static <T extends Object, V extends T> T[] set(T[] array, V value) {
        for (int i = 0; i < array.length; i++)
            array[i] = value;
        return array;
    }

    public static <K extends Object, T extends Object> T getFirst(Map<K, T> map, K... keys) {
        if (map == null)
            return null;
        for (K key : keys) {
            if (map.containsKey(key))
                return map.get(key);
        }
        return null;
    }

    public static <T extends Object> T getFirst(Collection<T> items, T otherwise) {
        if (items == null || items.size() == 0)
            return otherwise;
        return items.iterator().next();
    }

    public static <T extends Object> T getFirst(T... items) {
        for (T item : items) {
            if (item != null)
                return item;
        }
        return null;
    }

    public static <T extends Object> Map<T, Integer> indexesOf(Collection<T> c) {
        return indexesOf(c, new HashMap<T, Integer>());
    }

    public static <T extends Object, M extends Map<T, Integer>> M indexesOf(Collection<T> c, M map) {
        int i = 0;
        for (T it : c) {
            map.put(it, i++);
        }
        return map;
    }

    public static <K extends Object, V extends Object, M extends Map<V, List<K>>> M partition(Collection<K> c, Mapper<K, V> m, M result) {
        for (K obj : c) {
            V v = m.map(obj);
            List<K> l = result.get(v);
            if (l == null) {
                result.put(v, l = new LinkedList<K>());
            }
            l.add(obj);
        }
        return result;
    }

    public static <T1 extends Object, T2 extends Object> Iterator<T2> iterate(Iterable<T1> it, MapFn<T1, T2> map) {
        return new MappedIterator<T1, T2>(it.iterator(), map);
    }

    public static <T1 extends Object, T2 extends Object> Iterable<T2> iterable(final Iterable<T1> it, final MapFn<T1, T2> map) {
        return new Iterable<T2>() {
            @Override
            public Iterator<T2> iterator() {
                return iterate(it, map);
            }
        };
    }

    public static <T extends Object> List<T> repeat(int n, T... items) {
        return repeat(n, new ArrayList<T>(n * items.length), items);
    }

    public static <T extends Object, C extends Collection<T>> C repeat(int n, C into, T... items) {
        for (int i = 0; i < n; i++) {
            for (T item : items) {
                into.add(item);
            }
        }
        return into;
    }

    public static <T extends Object, C extends Collection<T>> C intersectInto(C into, Collection<T>... from) {
        if (from.length > 0) {
            into.addAll(from[0]);
            for (int i = 1; i < from.length; i++) {
                into.retainAll(from[i]);
            }
        }
        return into;
    }

    public static <T extends Object> Set<T> intersect(Collection<T>... from) {
        return intersectInto(new HashSet<T>(), from);
    }

    public static <T extends Object> boolean hasIntersection(Collection<? extends T> s1, Collection<? extends T> s2) {
        if (s1 == null || s2 == null)
            return false;
        int n1 = s1.size();
        int n2 = s2.size();
        if (n2 < n1) {
            Collection<? extends T> t = s1;
            s1 = s2;
            s2 = t;
        }
        for (T e : s1) {
            if (s2.contains(e))
                return true;
        }
        return false;
    }

    public static <T extends Object> int countIntersection(Collection<? extends T> s1, Collection<? extends T> s2) {
        if (s1 == null || s2 == null)
            return 0;
        int n1 = s1.size();
        int n2 = s2.size();
        if (n2 < n1) {
            Collection<? extends T> t = s1;
            s1 = s2;
            s2 = t;
        }
        int n = 0;
        for (T e : s1) {
            if (s2.contains(e))
                n++;
        }
        return n;
    }

    public static <T extends Object> int countComplement(Collection<? extends T> s1, Collection<? extends T> s2) {
        int n1 = s1.size();
        int n2 = s2.size();
        if (n2 < n1) {
            Collection<? extends T> t = s1;
            s1 = s2;
            s2 = t;
        }
        int n = s2.size() - s1.size();
        for (T e : s1) {
            if (!s2.contains(e))
                n++;
        }
        return n;
    }

    public static <K, T, M extends Map<K, T>> M putAll(M into, Object... kv) {
        for (int i = 0; i < kv.length; i += 2) {
            into.put((K) kv[i], (T) kv[i + 1]);
        }
        return into;
    }

    public static <T, M extends Map<T, T>> M put(M into, T... kv) {
        for (int i = 0; i < kv.length; i += 2) {
            into.put(kv[i], kv[i + 1]);
        }
        return into;
    }

    public static <T, C extends Collection<T>> C removeAll(C c, T obj) {
        for (Iterator<T> it = c.iterator(); it.hasNext(); ) {
            if (ObjectUtils.equals(it.next(), obj))
                it.remove();
        }
        return c;
    }

    public static <T extends Object> boolean hasComplement(Collection<T> c1, Collection<T> c2) {
        if (c1 == null || c2 == null)
            return false;
        for (T t1 : c1) {
            if (!c2.contains(t1))
                return true;
        }
        for (T t2 : c2) {
            if (!c1.contains(t2))
                return true;
        }
        return false;
    }

    public static int size(Collection<?> c) {
        return (c == null) ? 0 : c.size();
    }
}
