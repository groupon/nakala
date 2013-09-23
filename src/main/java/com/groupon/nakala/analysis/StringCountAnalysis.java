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
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author npendar@groupon.com
 */
public class StringCountAnalysis implements Analysis {
    Map<String, Integer> counts;

    public StringCountAnalysis() {
        counts = new HashMap<String, Integer>();
    }

    public void add(String s) {
        Integer c = counts.get(s);
        if (c == null)
            c = 0;
        counts.put(s, c + 1);
    }

    public Set<Map.Entry<String, Integer>> entrySet() {
        return counts.entrySet();
    }

    public Integer get(String s) {
        return counts.get(s);
    }

    public int size() {
        return counts.size();
    }

    public String getTsv() {
        List<String> fields = new ArrayList<String>(counts.size());
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            fields.add(e.getKey() + ":" + e.getValue());
        }
        return StringUtils.join(fields, '\t');
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
