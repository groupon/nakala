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
import com.groupon.nakala.exceptions.StoreException;
import com.groupon.nakala.exceptions.TextminingException;
import com.groupon.util.collections.EntryValueReverseComparator;

import java.util.*;

/**
 * @author npendar@groupon.com
 */
public final class StringCountsAnalysisCollector implements AnalysisCollector {
    Map<String, Integer> counts;

    public StringCountsAnalysisCollector() {
        counts = new HashMap<String, Integer>();
    }

    public void add(String s) {
        try {
            counts.put(s, counts.get(s) + 1);
        } catch (NullPointerException e) {
            counts.put(s, 1);
        }
    }

    public Map<String, Integer> getCounts() {
        return counts;
    }

    public void merge(AnalysisCollector other) {
        StringCountsAnalysisCollector sc = (StringCountsAnalysisCollector) other;
        for (Map.Entry<String, Integer> e : sc.counts.entrySet()) {
            Integer c = counts.get(e.getKey());
            if (c == null) {
                counts.put(e.getKey(), e.getValue());
            } else {
                counts.put(e.getKey(), c + e.getValue());
            }
        }
    }

    @Override
    public void store(DataStore ds) throws StoreException {
        List<Map.Entry<String, Integer>> values = new ArrayList<Map.Entry<String, Integer>>(counts.size());
        values.addAll(counts.entrySet());
        Collections.sort(values, new EntryValueReverseComparator());
        for (Map.Entry e : values)
            (new KeyValuePairAnalysis(e)).store(ds);
    }

    @Override
    public void addAnalysis(Analysis a) {
        if (a instanceof StringCountAnalysis) {
            StringCountAnalysis sca = (StringCountAnalysis) a;
            for (Map.Entry<String, Integer> e : sca.entrySet()) {
                Integer c = counts.get(e.getKey());
                if (c == null)
                    c = 0;
                counts.put(e.getKey(), c + e.getValue());
            }
        } else {
            throw new TextminingException("Unsupported analysis type.");
        }
    }
}
