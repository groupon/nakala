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

package com.groupon.nakala.core;

import com.groupon.util.io.IoUtil;
import opennlp.tools.util.Span;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author npendar@groupon.com
 */
public class GeneralFinder {
    private static final Pattern SPACE_PAT = Pattern.compile("\\s+");

    private GeneralFinder() {
    }

    public static FinderIndex makeIndex(String fileName) throws IOException {
        FinderIndex index = new FinderIndex();
        return makeIndex(fileName, index);
    }

    public static FinderIndex makeIndex(Reader reader) throws IOException {
        FinderIndex index = new FinderIndex();
        return makeIndex(reader, index);
    }

    public static FinderIndex makeIndex(String fileName, FinderIndex index) throws IOException {
        return makeIndex(IoUtil.read(GeneralFinder.class, fileName), index);
    }

    public static FinderIndex makeIndex(Reader reader, FinderIndex index) throws IOException {
        HashSet<String> stopWords = new HashSet<String>();
        stopWords.add("a");
        stopWords.add("an");
        stopWords.add("the");
        stopWords.add("i");
        stopWords.add("of");
        stopWords.add("or");
        stopWords.add("at");
        stopWords.add("el");
        stopWords.add("&");

        Matcher commentMatcher = Pattern.compile("#.*").matcher("");
        Matcher m = Pattern.compile("[\\d\\W]+").matcher("");
        for (String s : IoUtil.readLines(reader)) {
            commentMatcher.reset(s);
            if (commentMatcher.find()) {
                s = commentMatcher.replaceFirst("");
            }
            s = s.trim();
            if (s.isEmpty())
                continue;

            for (String t : SPACE_PAT.split(s)) {
                if (t.isEmpty() || stopWords.contains(t))
                    continue;
                m.reset(t);
                if (m.matches())
                    continue;
                try {
                    index.get(t).add(s);
                } catch (NullPointerException e) {
                    HashSet<String> hs = new HashSet<String>();
                    hs.add(s);
                    index.put(t, hs);
                }
                break; // Index on the first valid token only.
            }
        }
        return index;
    }

    public static boolean isFound(String s, FinderIndex index) {
        if (s == null || s.isEmpty())
            return false;

        Set<String> seenWords = Collections.synchronizedSet(new HashSet<String>());
        String s1 = s.toLowerCase().trim();
        for (String t : SPACE_PAT.split(s1)) {
            if (seenWords.contains(t))
                continue;
            seenWords.add(t);
            Set<String> hs = index.get(t);
            if (hs != null && hs.contains(s1)) {
                return true;
            }
        }
        return false;
    }

    public static List<Span> findSpans(String s, FinderIndex index) {
        List<Span> spans = new ArrayList<Span>(10);

        if (s == null || s.isEmpty())
            return spans;

        Set<String> seenWords = Collections.synchronizedSet(new HashSet<String>());
        String sLc = s.toLowerCase();

        for (String t : SPACE_PAT.split(sLc)) {
            if (seenWords.contains(t))
                continue;
            seenWords.add(t);
            Set<String> hs = index.get(t);
            if (hs == null)
                continue;
            for (String l : hs) {
                int i = sLc.indexOf(l);
                while (i > -1) {
                    int j = i + l.length();
                    boolean add = true;
                    int k = -1;
                    int replace = -1;
                    for (Span sp : spans) {
                        ++k;
                        if (sp.getStart() <= i && sp.getEnd() >= j) {
                            // A longer span already exists
                            add = false;
                            break;
                        } else if (sp.getStart() >= i && sp.getEnd() <= j) {
                            // New span longer; replace old
                            replace = k;
                            break;
                        }
                    }
                    if (add) {
                        Span sp = new Span(i, i + l.length());
                        if (replace > -1) {
                            spans.set(replace, sp);
                        } else {
                            spans.add(sp);
                        }
                    }
                    i = sLc.indexOf(l, i + l.length());
                }
            }
        }
        return spans;
    }

    public static Set<String> getExtractions(String s, FinderIndex index) {
        Set<String> ss = Collections.synchronizedSet(new HashSet<String>());
        for (Span sp : findSpans(s, index))
            ss.add(s.substring(sp.getStart(), sp.getEnd()));
        return ss;
    }
}
