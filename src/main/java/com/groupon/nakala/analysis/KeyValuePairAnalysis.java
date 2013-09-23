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

package com.groupon.nakala.analysis;

import com.groupon.nakala.db.DataStore;
import com.groupon.nakala.db.FlatFileStore;
import com.groupon.nakala.exceptions.StoreException;

import java.util.Map;

/**
 * @author npendar@groupon.com
 */
public class KeyValuePairAnalysis<T extends Comparable, U extends Comparable>
        implements Analysis, Map.Entry<T, U>, Comparable<KeyValuePairAnalysis<T, U>> {
    T key;
    U value;

    public KeyValuePairAnalysis(Map.Entry<T, U> entry) {
        this.key = entry.getKey();
        this.value = entry.getValue();
    }

    public KeyValuePairAnalysis(T key, U value) {
        this.key = key;
        this.value = value;
    }

    public T getKey() {
        return key;
    }

    public U getValue() {
        return value;
    }


    @Override
    public U setValue(U o) {
        return value = o;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KeyValuePairAnalysis))
            return false;
        KeyValuePairAnalysis e = (KeyValuePairAnalysis) o;
        return (getKey() == null ?
                e.getKey() == null :
                getKey().equals(e.getKey()))
                &&
                (getValue() == null ?
                        e.getValue() == null :
                        getValue().equals(e.getValue()));
    }

    @Override
    public int hashCode() {
        return (getKey() == null ? 0 : getKey().hashCode()) ^
                (getValue() == null ? 0 : getValue().hashCode());
    }

    public String getTsv() {
        return key + "\t" + value;
    }

    @Override
    public int compareTo(KeyValuePairAnalysis<T, U> other) {
        int cmp = getKey().compareTo(other.getKey());
        return cmp != 0 ? cmp : getValue().compareTo(other.getValue());
    }

    @Override
    public void store(DataStore ds) throws StoreException {
        if (ds instanceof FlatFileStore) {
            ((FlatFileStore) ds).getPrintStream().println(getTsv());
        } else {
            throw new StoreException("Unsupported data store " + ds.getClass().getName());
        }
    }
}