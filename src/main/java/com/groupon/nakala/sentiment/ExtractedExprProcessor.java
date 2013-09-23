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

package com.groupon.nakala.sentiment;

import org.apache.lucene.index.IndexReader;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author npendar@groupon.com
 */
public final class ExtractedExprProcessor {

    private static final Pattern SPACE_PAT = Pattern.compile(" ");
    private static final HashSet<String> stopWordsForOutput = new HashSet<String>();

    static {
        stopWordsForOutput.add("hi");
        stopWordsForOutput.add("please");
        stopWordsForOutput.add("a");
        stopWordsForOutput.add("the");
        stopWordsForOutput.add("of");
        stopWordsForOutput.add("my");
        stopWordsForOutput.add("our");
        stopWordsForOutput.add("is");
        stopWordsForOutput.add("was");
        stopWordsForOutput.add("were");
        stopWordsForOutput.add("been");
    }

    public static void boldWordsOfInterest(
            List<String> accumTerms, Set<String> queryTermsToHighlight)
            throws IOException {

        for (int i = 0; i < accumTerms.size(); ++i) {
            String t = accumTerms.get(i);
            if (queryTermsToHighlight.contains(t)
                    && !stopWordsForOutput.contains(t.toLowerCase())) {
                accumTerms.set(i, PrePostProcessor.HIGHLIGHTSTART + t
                        + PrePostProcessor.HIGHLIGHTEND);
            }
        }
    }

    private static boolean containsTaboo(Map<Integer, List<HitSpan>> tabooSpans,
                                         int docNo, int start, int end) {
        if (tabooSpans == null) {
            return false;
        }
        List<HitSpan> mss = tabooSpans.get(docNo);
        if (mss == null) {
            return false;
        }
        for (HitSpan ms : mss) {
            if (ms.getStart() >= start && ms.getEnd() <= end) {
                return true;
            }
        }
        return false;
    }

    public static void reconstructExcerpt(List<String> accumTerms,
                                          int start, int end, int docNo, IndexReader ir, byte extractionType, Map<Integer, List<HitSpan>> tabooSpans)
            throws IOException {

        String field = ExcerptIndexer.ORIGINAL;
        if (extractionType == ExtractedReviewRecord.TYPE_TITLE) {
            field = ExcerptIndexer.TITLE_ORIGINAL;
        }

        String[] tokens = SPACE_PAT.split(ir.document(docNo).get(field));

        while (start >= 0) {
            if (tokens[start].equals(PrePostProcessor.SENTENCE_BOUNDARY)) {
                ++start;
                break;
            }
            --start;
        }

        while (end < tokens.length) {
            if (tokens[end].equals(PrePostProcessor.SENTENCE_BOUNDARY)) {
                break;
            }
            ++end;
        }

        if (start < 0)
            start = 0;

        if (end > tokens.length)
            end = tokens.length;

        if (!containsTaboo(tabooSpans, docNo, start, end)) {
            for (int i = start; i < end; ++i) {
                accumTerms.add(tokens[i]);
            }
        }
    }
}
