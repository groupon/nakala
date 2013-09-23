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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author alasdair@groupon.com
 */
public abstract class MapOfCollections<K, V> implements Map<K, Collection<V>> {
    private Map<K, Collection<V>> fMap;
    private CollectionFactory<V> fFactory;

    public MapOfCollections(Map<K, Collection<V>> map, CollectionFactory<V> cf) {
        fMap = map;
        fFactory = cf;
    }

    public Map<K, Collection<V>> getMap() {
        return fMap;
    }

    public V add(K key, V value) {
        Collection<V> c = fMap.get(key);
        if (c == null)
            fMap.put(key, c = fFactory.newCollection());
        c.add(value);
        return value;
    }

    public int totalSize() {
        int total = 0;
        for (Collection<V> c : fMap.values()) {
            total += c.size();
        }
        return total;
    }

    public void merge(Map<K, Collection<V>> map) {
        for (Entry<K, Collection<V>> e : map.entrySet()) {
            for (V v : e.getValue()) {
                add(e.getKey(), v);
            }
        }
    }

    @Override
    public void clear() {
        fMap.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return fMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return fMap.containsValue(value);
    }

    @Override
    public Set<Entry<K, Collection<V>>> entrySet() {
        return fMap.entrySet();
    }

    @Override
    public Collection<V> get(Object key) {
        return fMap.get(key);
    }

    @Override
    public boolean isEmpty() {
        return fMap.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return fMap.keySet();
    }

    @Override
    public Collection<V> put(K key, Collection<V> value) {
        return fMap.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends Collection<V>> m) {
        fMap.putAll(m);
    }

    @Override
    public Collection<V> remove(Object key) {
        return fMap.remove(key);
    }

    @Override
    public int size() {
        return fMap.size();
    }

    @Override
    public Collection<Collection<V>> values() {
        return fMap.values();
    }
}