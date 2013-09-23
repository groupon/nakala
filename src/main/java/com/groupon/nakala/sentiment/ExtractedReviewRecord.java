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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author npendar@groupon.com
 */
public class ExtractedReviewRecord {

    public static final byte TYPE_CONTENT = 0;
    public static final byte TYPE_TITLE = 1;
    public static final byte TYPE_OVERRIDE = 2;

    protected long descId = 0;
    protected int startToken = 0;
    protected int endToken = 0;
    protected String quote = "";
    protected String annotatedQuote = "";
    protected List<String> words = null;
    protected String domain = "";
    protected String title = "";
    protected double score = 0;
    protected byte extractionType;
    protected List<HitSpan> highlightSpans;

    private static double TITLE_WEIGHT = 2;
    private boolean overridenByTitle = false;
    private double originalScore = 0;

    private boolean bAmplifiedByTitle = false;
    private Set<String> titleWords = null;

    protected ExtractedReviewRecord() {
    }

    public ExtractedReviewRecord(long descID, int startToken,
                                 int endToken, double score, String domain,
                                 List<String> words, String title, byte type) {
        this.descId = descID;
        this.startToken = startToken;
        this.endToken = endToken;
        this.score = score;
        this.domain = domain;
        this.words = words;
        this.title = title;
        this.bAmplifiedByTitle = false;
        this.extractionType = type;
    }

    public byte getExtractionType() {
        return extractionType;
    }

    public void overrideScoreByTitle(double s) {
        score = s;
        overridenByTitle = true;
    }

    public void overrideScore(double s) {
        originalScore = score;
        score = s;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(descId)
                .append("\t")
                .append(score)
                .append("\t")
                .append(domain)
                .append("\t")
                .append(title)
                .append("\t")
                .append(quote)
                .append("\t")
                .append(annotatedQuote)
                .append("\t")
                .append(bAmplifiedByTitle)
                .append("\t")
                .append(overridenByTitle)
                .append("\t")
                .append(originalScore);

        return sb.toString();
    }

    public String toDebugString() {
        StringBuffer sb = new StringBuffer();
        sb.append(toString())
                .append("  [")
                .append(startToken)
                .append(", ")
                .append(endToken)
                .append("] ");
        return sb.toString();
    }

    public String getTitle() {
        return title;
    }

    public Set<String> getTitleWords() {
        return titleWords;
    }

    public void normalizeScoreForDB() {
        score = (score + TITLE_WEIGHT) / (TITLE_WEIGHT * 2);
    }

    public void amplifyByTitleWords() {
        if (titleWords == null) {
            return;
        }
        if (Math.abs(score) >= 0) {

            if (titleWords == null || titleWords.isEmpty())
                return;

            int titleWordsInQuote = 0;
            Set<String> words = Collections.synchronizedSet(new HashSet<String>());
            for (String w : getWords()) {
                words.add(w.toLowerCase());
            }
            for (String w : words) {
                if (titleWords.contains(w)) {
                    ++titleWordsInQuote;
                }
            }
            if (titleWordsInQuote > 0) {
                double wt = TITLE_WEIGHT * (double) titleWordsInQuote
                        / titleWords.size();
                score *= wt;
                bAmplifiedByTitle = true;
            }
        }
    }

    @Override
    public int hashCode() {
        int c = ((int) descId + domain.hashCode()) * quote.hashCode();
        return c;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExtractedReviewRecord other = (ExtractedReviewRecord) obj;
        if (this.descId != other.descId) {
            return false;
        }
        if ((this.domain == null) ? (other.domain != null) : !this.domain
                .equals(other.domain)) {
            return false;
        }
        if ((this.quote == null) ? (other.quote != null) : !this.quote
                .equals(other.quote)) {
            return false;
        }
        return true;
    }

    public void prepareForDB(PrePostProcessor pp, int maxQuoteLenth,
                             int maxAnnotatedQuoteLength, int maxTitleSize) {
        pp.postprocessRow(this, maxQuoteLenth,
                maxAnnotatedQuoteLength, maxTitleSize);
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAnnotatedQuote(String annotatedQuote) {
        this.annotatedQuote = annotatedQuote;
    }

    public void setHighlightSpans(List<HitSpan> hitSpans) {
        this.highlightSpans = hitSpans;
    }

    public List<HitSpan> getHighlightSpans() {
        return highlightSpans;
    }

    public String getQuote() {
        return quote;
    }

    public String getAnnotatedQuote() {
        return annotatedQuote;
    }

    public void setTitleWords(Set<String> titleWords2) {
        this.titleWords = titleWords2;
    }

    public boolean getAmplTitle() {
        return bAmplifiedByTitle;
    }

    public double getScore() {
        return score;
    }

    public String getDomain() {
        return domain;
    }

    public long getDescID() {
        return descId;
    }

    public int getStartToken() {
        return startToken;
    }

    // to insert leading zeros for proper sorting
    public String getStartTokenToStringLeading0s() {
        String num = Integer.toString(startToken);
        String numLeAD0S = "";
        for (int i = 0; i < 3 - num.length(); i++) {
            numLeAD0S += "0";
        }
        return numLeAD0S + num;
    }

    public int getEndToken() {
        return endToken;
    }

    public List<String> getWords() {
        return words;
    }
}
