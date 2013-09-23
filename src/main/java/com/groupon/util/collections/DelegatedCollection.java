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
import java.util.Iterator;
import java.util.Set;

/**
 * @author alasdair@groupon.com
 */
public class DelegatedCollection<T> implements Collection<T> {

    private Collection<T> fCollection;

    public DelegatedCollection(Collection<T> c) {
        fCollection = c;
    }

    protected Collection<T> getCollection() {
        return fCollection;
    }

    protected void setCollection(Collection<T> c) {
        fCollection = c;
    }

    @Override
    public String toString() {
        return fCollection.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Collection<?>))
            return false;
        return fCollection.equals((Set<?>) obj);
    }


    @Override
    public boolean add(T e) {
        return fCollection.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return fCollection.addAll(c);
    }

    @Override
    public void clear() {
        fCollection.clear();
    }

    @Override
    public boolean contains(Object o) {
        return fCollection.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return fCollection.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return fCollection.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return fCollection.iterator();
    }

    @Override
    public boolean remove(Object o) {
        return fCollection.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return fCollection.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return fCollection.retainAll(c);
    }

    @Override
    public int size() {
        return fCollection.size();
    }

    @Override
    public Object[] toArray() {
        return fCollection.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return fCollection.toArray(a);
    }

}
