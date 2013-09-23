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
import java.util.regex.Pattern;

/**
 * @author alasdair@groupon.com
 */
public class PatternMap<V> extends DelegatedMap<String, V> {

    private class Match {
        private Pattern fPattern;
        private V fValue;

        Match(Pattern p, V v) {
            fPattern = p;
            fValue = v;
        }

        public Pattern getPattern() {
            return fPattern;
        }

        public V getValue() {
            return fValue;
        }
    }

    private List<Match> fPatternValues = new LinkedList<Match>();
    private Set<String> fNoMatch = new HashSet<String>();

    public PatternMap() {
        this(new HashMap<String, V>());
    }

    public PatternMap(Map<String, V> m) {
        super(m);
    }

    public void putPattern(Pattern p, V value) {
        fPatternValues.add(0, new Match(p, value));
        if (value == null) {
            for (Entry<String, V> e : entrySet()) {
                if (p.matcher(e.getKey()).matches()) {
                    e.setValue(value);
                }
            }
        }
    }

    @Override
    public V get(Object key) {
        if (super.keySet().contains(key))
            return super.get(key);
        if (fNoMatch.contains(key))
            return null;
        String s = (String) key;
        for (Match m : fPatternValues) {
            if (m.getPattern().matcher(s).matches()) {
                put(s, m.getValue());
                return m.getValue();
            }
        }
        fNoMatch.add(s);
        return null;
    }
}
