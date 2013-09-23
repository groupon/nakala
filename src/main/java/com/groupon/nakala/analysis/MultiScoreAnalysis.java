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

import com.groupon.nakala.core.Id;
import com.groupon.nakala.db.DataStore;
import com.groupon.nakala.db.FlatFileStore;
import com.groupon.nakala.exceptions.StoreException;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author npendar@groupon.com
 */
public class MultiScoreAnalysis implements Analysis, Iterable<Map.Entry<String, Double>> {
    Id id;
    Map<String, Double> scores;

    public MultiScoreAnalysis(Id id) {
        this.id = id;
        scores = new HashMap<String, Double>();
    }

    public void put(String label, double score) {
        scores.put(label, score);
    }

    public Double get(String key) {
        return scores.get(key);
    }

    @Override
    public void store(DataStore ds) throws StoreException {
        if (ds instanceof FlatFileStore) {
            PrintStream printStream = ((FlatFileStore) ds).getPrintStream();
            for (Map.Entry<String, Double> e : scores.entrySet()) {
                printStream.println(id.toString() + '\t' + e.getKey() + '\t' + e.getValue());
            }
        } else {
            throw new StoreException("Unsupported data store " + ds.getClass().getName());
        }
    }

    @Override
    public Iterator<Map.Entry<String, Double>> iterator() {
        return scores.entrySet().iterator();
    }

    @Override
    public String toString() {
        return "MultiScoreAnalysis(id = " + id + ", scores = " + scores + ")";
    }
}
