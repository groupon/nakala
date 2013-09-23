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

import com.groupon.nakala.core.SimpleSentenceBreaker;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author npendar@groupon.com
 */
public final class PrePostProcessor {

    public static final String NUMERAL = "UTNUM";
    public static final String SENTENCE_BOUNDARY = "UTSB";

    public static final char HIGHLIGHTSTART = '\u0000';
    public static final char HIGHLIGHTEND = '\u0001';

    private static final Pattern attachedPunc = Pattern
            .compile("(\\p{Alpha})([!\"#$%&'*+,./:;<=>?@\\[\\\\\\]^_`{|}~]+)(\\D)");
    private static final Pattern attachedDash = Pattern
            .compile("(\\p{Alnum})-(\\p{Alnum})");
    private static final Pattern camelCasePat = Pattern
            .compile("([a-z\\.])([A-Z])");
    private static final Pattern crLfTabPat = Pattern.compile("[\n\r\t]+");
    private static final Pattern cleanSentBoundariesPat = Pattern.compile("(\\w+[!.?]+)\\s+([a-z])");
    private static final Pattern contPat = Pattern.compile(
            " (\\w+) '?(ve|s|d|re|ll) ", Pattern.CASE_INSENSITIVE);
    private static final Pattern detachedLBr = Pattern
            .compile("([\\(\\[\\{<-]) ");
    private static final Pattern detachedRBr = Pattern
            .compile("\\s+([\\)\\}\\]>])");
    private static final Pattern imPat = Pattern.compile(" i m ",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern negPat = Pattern.compile("n '?t ",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern numCommaPat = Pattern
            .compile("(\\d+),\\s+(\\d+)");
    private static final Pattern numPat = Pattern
            .compile("(\\b)(\\d+|one|two|three|four|five|six|seven|eight|nine|ten|eleven|tweleve|(?:thir|four|fif|six|seven|eigh|nine)teen|(?:twen|thir|for|fif|six|seven|eigh|nine)ty|hundred|thousand|million|billion)");
    private static final Pattern puncSpPat = Pattern
            .compile("\\s+([!\\)\\}\\]:;'\",>\\.?-])");
    private static final Pattern junkBegin = Pattern
            .compile("^[!#%&*+,./:;<=>?@\\[\\\\\\]^_`{|}~ ]+");
    private static final Pattern junkLast = Pattern
            .compile("[#%&*+,/:;<=>@\\[\\\\\\]^_`{|}~ ]+$");
    private static final Pattern spacePat = Pattern.compile("\\s\\s+");
    private static final Pattern tagPat = Pattern.compile("<.*?\\s*/?>",
            Pattern.CASE_INSENSITIVE);

    private final Matcher attachedPuncMatcher = attachedPunc.matcher("");
    private final Matcher attachedDashMatcher = attachedDash.matcher("");
    private final Matcher camelCaseMatcher = camelCasePat.matcher("");
    private final Matcher cleanSentBoundariesMatcher = cleanSentBoundariesPat.matcher("");
    private final Matcher crLfTabMatcher = crLfTabPat.matcher("");
    private final Matcher contMatcher = contPat.matcher("");
    private final Matcher detachedLBrMatcher = detachedLBr.matcher("");
    private final Matcher detachedRBrMatcher = detachedRBr.matcher("");
    private final Matcher imMatcher = imPat.matcher("");
    private final Matcher negMatcher = negPat.matcher("");
    private final Matcher numCommaMatcher = numCommaPat.matcher("");
    private final Matcher numMatcher = numPat.matcher("");
    private final Matcher puncSpMatcher = puncSpPat.matcher("");
    private final Matcher spaceMatcher = spacePat.matcher("");
    private final Matcher tagMatcher = tagPat.matcher("");
    private final Matcher junkBeginMatcher = junkBegin.matcher("");
    private final Matcher junkLastMatcher = junkLast.matcher("");

    private Set<Matcher> substitutions;

    private PrePostProcessor() {
    }

    public static PrePostProcessor newInstance() throws IOException {
        PrePostProcessor pp = new PrePostProcessor();
        VocabUtils tv = VocabUtils.newInstance();

        pp.substitutions = Collections.synchronizedSet(new HashSet<Matcher>());
        for (String sub : tv.getPreTokenizerSubstitutions()) {
            Pattern p = Pattern.compile(sub, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher("");
            pp.substitutions.add(m);
        }

        return pp;
    }

    public String preprocess(String line) {
        if (line == null)
            return null;
        // Get rid of HTML tags
        tagMatcher.reset(line);
        line = tagMatcher.replaceAll(" ");
        camelCaseMatcher.reset(line);
        int offset = 0;
        while (camelCaseMatcher.find(offset)) {
            line = camelCaseMatcher.replaceFirst(camelCaseMatcher.group(1)
                    + ' ' + camelCaseMatcher.group(2));
            offset = camelCaseMatcher.start();
            if (offset >= line.length()) {
                break;
            }
            camelCaseMatcher.reset(line);
        }

        // Replace any HTML entities with their corresponding character
        line = line.replace("&nbsp;", " ");
        line = StringEscapeUtils.unescapeHtml4(line) + ' ';

        line = line.replace("\"", "");

        // Remove \r \n \t
        crLfTabMatcher.reset(line);
        if (crLfTabMatcher.find()) {
            line = crLfTabMatcher.replaceAll(" ");
        }

        // Identify sentence boundaries
        cleanSentBoundariesMatcher.reset(line);
        offset = 0;
        while (cleanSentBoundariesMatcher.find(offset)) {
            if (!SimpleSentenceBreaker.abbr.contains(cleanSentBoundariesMatcher.group(1))) {
                line = cleanSentBoundariesMatcher.replaceFirst(
                        cleanSentBoundariesMatcher.group(1) + ' ' +
                                cleanSentBoundariesMatcher.group(2).toUpperCase());
            }
            offset = cleanSentBoundariesMatcher.end();
            if (offset >= line.length()) {
                break;
            }
            cleanSentBoundariesMatcher.reset(line);
        }

        StringBuffer sb = new StringBuffer();
        for (String s : new SimpleSentenceBreaker(line)) {
            sb.append(s).append(' ').append(SENTENCE_BOUNDARY).append(' ');
        }
        line = sb.toString();

        // Detach punctuations from words
        attachedPuncMatcher.reset(line);
        offset = 0;
        while (attachedPuncMatcher.find(offset)) {
            line = attachedPuncMatcher.replaceFirst(attachedPuncMatcher
                    .group(1).replace("$", "\\$")
                    + ' '
                    + attachedPuncMatcher.group(2).replace("$", "\\$")
                    + attachedPuncMatcher.group(3).replace("$", "\\$"));
            offset = attachedPuncMatcher.start();
            if (offset >= line.length()) {
                break;
            }
            attachedPuncMatcher.reset(line);
        }

        line = line.replace("(", " ( ").replace("[", " [ ").replace("{", " { ")
                .replace("<", " < ").replace(")", " ) ").replace("]", " ] ")
                .replace("}", " } ").replace(">", " > ");

        // Detach single dashes
        attachedDashMatcher.reset(line);
        offset = 0;
        while (attachedDashMatcher.find(offset)) {
            line = attachedDashMatcher.replaceFirst(attachedDashMatcher
                    .group(1)
                    + " - " + attachedDashMatcher.group(2));
            offset = attachedDashMatcher.start();
            if (offset >= line.length()) {
                break;
            }
            attachedDashMatcher.reset(line);
        }

        line = line.replace("$", "$ ");

        spaceMatcher.reset(line);
        line = spaceMatcher.replaceAll(" ");

        numMatcher.reset(line);
        offset = 0;
        while (numMatcher.find(offset)) {
            line = numMatcher.replaceFirst(numMatcher.group(1) + NUMERAL
                    + numMatcher.group(2));
            offset = numMatcher.start();
            if (offset >= line.length()) {
                break;
            }
            numMatcher.reset(line);
        }

        char[] chars = line.toCharArray();
        for (Matcher m : substitutions) {
            m.reset(line);
            offset = 0;
            while (m.find(offset)) {
                for (int i = m.start(); i < m.end() - 1; ++i) {
                    if (chars[i] == ' ') {
                        chars[i] = '_';
                    }
                }
                offset = m.end();
                if (offset >= line.length()) {
                    break;
                }
            }
        }
        line = new String(chars);

        return line.trim();
    }

    private String cleanup(String quote) {
        int posBrOpen = quote.lastIndexOf("(");
        int posBrClosing = quote.lastIndexOf(")");
        if (posBrOpen * posBrClosing < 0) {// only one exists, clean both just
            // in case
            quote = quote.replace("(", "").replace(")", "");
        }
        quote = quote.replace("_", " ");
        quote = quote.replace(SENTENCE_BOUNDARY, "");
        quote = quote.replace(" (s)", "(s)");

        imMatcher.reset(quote);
        quote = imMatcher.replaceAll(" I'm ");

        contMatcher.reset(quote);
        int offset = 0;
        while (contMatcher.find(offset)) {
            quote = contMatcher.replaceFirst(" " + contMatcher.group(1) + "'"
                    + contMatcher.group(2) + " ");
            offset = contMatcher.start();
            if (offset >= quote.length())
                break;
            contMatcher.reset(quote);
        }

        char[] chars = quote.toCharArray();
        negMatcher.reset(quote);
        offset = 0;
        while (negMatcher.find(offset)) {
            for (int i = negMatcher.start(); i < negMatcher.end() - 1; ++i) {
                if (chars[i] == ' ') {
                    chars[i] = '_';
                }
            }
            offset = negMatcher.end();
            if (offset > quote.length()) {
                break;
            }
        }
        quote = (new String(chars)).replace("_", "");

        numCommaMatcher.reset(quote);
        offset = 0;
        while (numCommaMatcher.find(offset)) {
            quote = numCommaMatcher.replaceFirst(numCommaMatcher.group(1) + ","
                    + numCommaMatcher.group(2));
            offset = numCommaMatcher.start();
            if (offset >= quote.length())
                break;
            numCommaMatcher.reset(quote);
        }

        puncSpMatcher.reset(quote);
        offset = 0;
        while (puncSpMatcher.find(offset)) {
            quote = puncSpMatcher.replaceFirst(puncSpMatcher.group(1));
            offset = puncSpMatcher.start();
            if (offset >= quote.length()) {
                break;
            }
            puncSpMatcher.reset(quote);
        }

        detachedRBrMatcher.reset(quote);
        offset = 0;
        while (detachedRBrMatcher.find(offset)) {
            quote = detachedRBrMatcher
                    .replaceFirst(detachedRBrMatcher.group(1));
            offset = detachedRBrMatcher.start();
            if (offset >= quote.length()) {
                break;
            }
            detachedRBrMatcher.reset(quote);
        }

        detachedLBrMatcher.reset(quote);
        offset = 0;
        while (detachedLBrMatcher.find(offset)) {
            quote = detachedLBrMatcher
                    .replaceFirst(detachedLBrMatcher.group(1));
            offset = detachedLBrMatcher.start();
            if (offset >= quote.length()) {
                break;
            }
            detachedLBrMatcher.reset(quote);
        }

        spaceMatcher.reset(quote);
        quote = spaceMatcher.replaceAll(" ").replace(" i ", " I ").replace(
                "$ ", "$");

        junkBeginMatcher.reset(quote);
        if (junkBeginMatcher.find())
            quote = junkBeginMatcher.replaceFirst("");
        junkLastMatcher.reset(quote);
        if (junkLastMatcher.find())
            quote = junkLastMatcher.replaceFirst("");

        return quote.trim();
    }

    public String postProcessWords(List<String> list) {
        return postProcessString(StringUtils.join(list, ' '));
    }

    public String postProcessString(String inStr) {
        String s = cleanup(inStr);
        s = StringEscapeUtils.escapeHtml4(s);
        s = s.replace(NUMERAL, "");
        return s;
    }

    public void postprocessRow(ExtractedReviewRecord row,
                               int maxQuoteLength, int maxAnnotatedQuoteLength, int maxTitleSize) {

        String annotatedQuote = postProcessWords(row.getWords());
        String title = postProcessString(row.getTitle());

        List<HitSpan> highlightSpans = new LinkedList<HitSpan>();

        StringBuilder annotatedQuoteSb = new StringBuilder();
        StringBuilder quoteSb = new StringBuilder();
        int start = -1, end = -1;
        for (int i = 0; i < annotatedQuote.length(); ++i) {
            char c = annotatedQuote.charAt(i);
            if (c == HIGHLIGHTSTART) {
                start = quoteSb.length();
                annotatedQuoteSb.append("<B>");
            } else if (c == HIGHLIGHTEND) {
                end = quoteSb.length();
                annotatedQuoteSb.append("</B>");
                if (start > -1) {
                    highlightSpans.add(new HitSpan(start, end));
                }
                start = -1;
            } else {
                quoteSb.append(c);
                annotatedQuoteSb.append(c);
            }
        }

        if (start > -1) {
            end = quoteSb.length();
            annotatedQuoteSb.append("</B>");
            highlightSpans.add(new HitSpan(start, end));
        }

        annotatedQuote = trimAnnotatedQuote(annotatedQuoteSb.toString(),
                maxAnnotatedQuoteLength);
        String quote = trimNewQuoteForDBRecord(quoteSb.toString(), maxQuoteLength);

        title = trimNewQuoteForDBRecord(title, maxTitleSize);

        row.setQuote(quote);
        row.setAnnotatedQuote(annotatedQuote);
        row.setTitle(title);
        row.setHighlightSpans(highlightSpans);
    }

    private String trimNewQuoteForDBRecord(String quote, int maxSize) {
        String result = quote;
        if (result.length() > maxSize) {// length of quote field in DB
            result = quote.substring(0, maxSize - 5) + "...";
        }
        return result;
    }

    /**
     * if the truncated string has unclosed annotation truncate further NOTE
     * this method also exist in BestSentence. These two project should be
     * merged at some point
     *
     * @param quote
     * @return
     */
    public String trimAnnotatedQuote(String quote, int maxSize) {
        String q = quote;
        if (q.length() > maxSize) {
            q = q.substring(0, maxSize - 5) + "...";
            q = tidyAnnotations(q);
        }
        return q;
    }

    /**
     * truncate the quote further if we risk truncating the part way through a
     * <b></b> tag
     *
     * @param quote
     * @return
     */
    private static String tidyAnnotations(String quote) {
        int e = quote.lastIndexOf(HIGHLIGHTEND);
        int s = quote.lastIndexOf(HIGHLIGHTSTART);
        String result = quote;
        if (s > -1) {
            if (e > s) {
                if (result.lastIndexOf(">") < e) {
                    result = result.substring(0, s);
                }
            } else {
                result = result.substring(0, s);
            }
        }
        return result;
    }
}