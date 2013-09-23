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

import java.util.*;

/**
 * @author alasdair@groupon.com
 */
public class Histogram<T> extends CacheMap<T, Integer> {
    public Histogram() {
    }

    public Histogram(Map<T, Integer> map) {
        super(map);
    }

    @Override
    public Integer newInstance(T key) {
        return 0;
    }

    public int add(T it) {
        int prv = get(it);
        put(it, prv + 1);
        return prv;
    }

    public void addAll(Collection<T> items) {
        for (T item : items) {
            add(item);
        }
    }

    public Histogram<T> merge(Histogram<T> countries) {
        for (Map.Entry<T, Integer> e : countries.entrySet()) {
            if (keySet().contains(e.getKey())) {
                put(e.getKey(), get(e.getKey()) + e.getValue());
            } else {
                put(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    public Collection<T> getMaxValues() {
        List<T> values = new ArrayList<T>(keySet().size());
        int max = 0;
        for (Map.Entry<T, Integer> e : entrySet()) {
            if (e.getValue() >= max) {
                max = e.getValue();
                if (e.getValue() > max) {
                    values.clear();
                }
                values.add(e.getKey());
            }
        }
        return values;
    }

    public <C extends Collection<T>> C sort(final Comparator<T> keyCmp, C into) {
        T[] all = (T[]) keySet().toArray();
        Arrays.sort(all, new Comparator<T>() {
            public int compare(T o1, T o2) {
                int c1 = get(o1);
                int c2 = get(o2);
                int cmp = c2 - c1;
                if (cmp == 0) {
                    if (keyCmp == null) {
                        cmp = ((Comparable<T>) o1).compareTo(o2);
                    } else {
                        cmp = keyCmp.compare(o1, o2);
                    }
                }
                return cmp;
            }
        });
        for (T t : all) {
            into.add(t);
        }
        return into;
    }

    public List<T> sort() {
        return sort(null, new ArrayList<T>(size()));
    }
}
