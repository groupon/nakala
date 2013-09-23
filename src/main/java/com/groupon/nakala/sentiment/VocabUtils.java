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

import com.groupon.nakala.core.Taboo;
import com.groupon.util.io.IoUtil;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spans.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author npendar@groupon.com
 */
public final class VocabUtils {

    public static final Pattern TAB = Pattern.compile("\t+");
    public static final Pattern COMMENT = Pattern.compile("#.*");
    public static final Pattern NONWORD = Pattern.compile("\\W");
    public static final Pattern SPACE = Pattern.compile("\\s+");

    private static final int DEFAULT_WINDOW = 5;
    private static final Pattern COMMA = Pattern.compile("\\s*,\\s*");

    private Set<SpanQuery> negationsContents, negationsTitle;
    private Set<SpanQuery> hardOverrides;
    private List<String> substitutions;
    private SpanQuery negationSQ;
    private SpanQuery negationTitleSQ;
    private SpanQuery hardOverridesSQ;
    private SpanQuery tabooQ;

    private static final Map<String, String> MACROS = Collections.synchronizedMap(new HashMap<String, String>());

    static {
        MACROS.put("@NUMERAL", PrePostProcessor.NUMERAL + ".+");
    }

    private VocabUtils() {
    }

    public static VocabUtils newInstance() throws IOException {

        VocabUtils tv = new VocabUtils();
        tv.negationsContents = Collections.synchronizedSet(new HashSet<SpanQuery>());
        tv.negationsTitle = Collections.synchronizedSet(new HashSet<SpanQuery>());
        tv.hardOverrides = Collections.synchronizedSet(new HashSet<SpanQuery>());

        String wlPath = "/nakala/sentiment/";

        tv.loadWordList(wlPath + "negation_rvw.txt", tv.negationsContents, tv.negationsTitle);
        tv.loadWordList(wlPath + "hard_overrides.txt", null, tv.hardOverrides);
        tv.loadSubstitutions(wlPath + "substitutions.txt");
        tv.tabooQ = tv.makeTabooQuery();
        return tv;
    }

    private SpanQuery makeTabooQuery() throws IOException {
        Taboo taboo = Taboo.newInstance();
        Set<SpanQuery> qs = new HashSet<SpanQuery>();
        for (String t : taboo.getTabooSet()) {
            String[] pieces = SPACE.split(t);
            if (pieces.length == 1) {
                qs.add(new SpanTermQuery(new Term(ExcerptIndexer.CONTENTS, pieces[0])));
            } else if (pieces.length > 1) {
                SpanQuery[] sqs = new SpanTermQuery[pieces.length];
                for (int i = 0; i < pieces.length; ++i) {
                    sqs[i] = new SpanTermQuery(new Term(ExcerptIndexer.CONTENTS, pieces[i]));
                }
                qs.add(new SpanNearQuery(sqs, 1, true));
            }
        }
        return buildQueryFromSpanQuerySet(qs);
    }

    private void loadSubstitutions(String fileName) throws IOException {
        substitutions = Collections.synchronizedList(new ArrayList<String>());
        BufferedReader in = new BufferedReader(IoUtil.read(VocabUtils.class, fileName));
        String line;
        int n = 0;
        while ((line = in.readLine()) != null) {
            ++n;
            line = COMMENT.matcher(line).replaceFirst("").trim();
            if (line.equals("")) {
                continue;
            }
            substitutions.add(line);
        }
    }

    public static List<String> tokenize(String s, char delim)
            throws VocabularyException {
        /*
           * Tokenize by whitespace; ignore quoted expressions
           */
        List<String> tokens = Collections.synchronizedList(new LinkedList<String>());

        boolean inQuote = false;
        int l = 0;
        char c = delim;
        boolean charDelim = true, lastCharDelim;

        for (int i = 0; i < s.length(); ++i) {
            lastCharDelim = charDelim;
            c = s.charAt(i);
            charDelim = c == delim;
            if (!inQuote && lastCharDelim) {
                // Mark start of new token
                l = i;
            }
            if (c == '"') {
                inQuote = !inQuote;
            } else if (charDelim) {
                if (inQuote)
                    continue;
                if (!lastCharDelim)
                    tokens.add(s.substring(l, i));
            }
        }

        tokens.add(s.substring(l));

        if (inQuote) {
            throw new VocabularyException("Uneven quotes in query '" + s + "'");
        }

        return tokens;
    }

    public static SpanQuery processQuery(String query, String iField, Set<Pattern> wordsOfInterest)
            throws VocabularyException {

        int window = 1; // number of words allowed between two terms
        // By default, allow one word in between.
        boolean inOrder = true; // should terms be in order?
        SpanQuery lastQuery = null;
        for (String t : tokenize(query, ' ')) {
            if (t.startsWith("&") || t.startsWith("~")) {
                inOrder = t.startsWith("&") ? true : false;
                // term specifies a window
                String w = t.substring(1);
                if (w.equals("")) {
                    throw new VocabularyException("Syntax error. No value set for window in '" + query + "'");
                }
                window = Integer.parseInt(w);
                continue;
            }

            SpanQuery newQuery = processTerm(t, iField, wordsOfInterest);

            if (lastQuery != null) {
                lastQuery = new SpanNearQuery(new SpanQuery[]{lastQuery,
                        newQuery}, window, inOrder);
            } else {
                lastQuery = newQuery;
            }
        }
        return lastQuery;
    }

    private static SpanQuery processTerm(String t, String iField,
                                         Set<Pattern> wordsOfInterest) {

        if (t.indexOf(',') > -1) {
            // term is a disjunction of other terms
            List<String> ts = tokenize(t, ',');
            SpanQuery[] qs = new SpanQuery[ts.size()];
            for (int i = 0; i < ts.size(); ++i) {
                String ti = ts.get(i);
                if (ti.startsWith("\"")) {
                    qs[i] = processQuery(ti.substring(1, ti.length() - 1)
                            .trim(), iField, wordsOfInterest);
                } else {
                    qs[i] = processTerm(ti, iField, wordsOfInterest);
                }
            }
            return new SpanOrQuery(qs);
        }

        // Process macros
        if (t.startsWith("@")) {
            t = MACROS.get(t.toUpperCase());
            if (t == null) {
                throw new VocabularyException("Unknown macro '" + t + "'");
            }
        }

        String tLower = t.toLowerCase();
        if (t.toUpperCase().equals(t) && wordsOfInterest != null) {
            // add individual terms to words of interest only if they are ALL
            // CAPS
            wordsOfInterest.add(Pattern.compile(tLower,
                    Pattern.CASE_INSENSITIVE));
        }

        // terms is a word
        SpanTermQuery stq = new SpanTermQuery(new Term(iField, tLower));
        return stq;
    }

    private void loadWordList(String fileName, Set<SpanQuery> negationsContents2, Set<SpanQuery> negationsTitle2)
            throws IOException {
        if (negationsContents2 != null)
            negationsContents2.clear();
        if (negationsTitle2 != null)
            negationsTitle2.clear();
        BufferedReader in = new BufferedReader(IoUtil.read(VocabUtils.class, fileName));
        String line;
        while ((line = in.readLine()) != null) {
            line = COMMENT.matcher(line).replaceFirst("").trim();
            if (line.equals("")) {
                continue;
            }

            // get rid of accidental spaces before or after commas
            Matcher matcher = COMMA.matcher(line);
            if (matcher.find()) {
                line = matcher.replaceAll(",");
            }

            if (negationsContents2 != null) {
                SpanQuery sqContent = processQuery(line, ExcerptIndexer.CONTENTS, null);
                negationsContents2.add(sqContent);
            }

            if (negationsTitle2 != null) {
                SpanQuery sqTitle = processQuery(line, ExcerptIndexer.TITLE, null);
                negationsTitle2.add(sqTitle);
            }
        }
        in.close();
    }

    public SpanQuery getTaboo() {
        return tabooQ;
    }

    public SpanQuery getHardOverrides() {
        if (hardOverridesSQ == null) {
            hardOverridesSQ = buildQueryFromSpanQuerySet(hardOverrides);
        }
        return hardOverridesSQ;
    }

    public SpanQuery getNegation() {
        if (negationSQ == null) {
            negationSQ = buildQueryFromSpanQuerySet(negationsContents);
        }
        return negationSQ;
    }

    public SpanQuery getNegationTitle() {
        if (negationTitleSQ == null) {
            negationTitleSQ = buildQueryFromSpanQuerySet(negationsTitle);
        }
        return negationTitleSQ;
    }

    public List<String> getPreTokenizerSubstitutions() {
        return substitutions;
    }

    public static SpanQuery buildQueryFromSpanQuerySet(Set<SpanQuery> querySet) {
        if (querySet == null || querySet.isEmpty()) {
            return null;
        }

        ArrayList<SpanQuery> sqs = new ArrayList<SpanQuery>(querySet);
        if (sqs.size() == 1) {
            return sqs.get(0);
        } else {
            SpanQuery[] sq = (SpanQuery[]) sqs.toArray(new SpanQuery[1]);
            return new SpanOrQuery(sq);
        }
    }

    private SpanQuery[] makeSpanQueryList(SpanQuery... qs) {
        ArrayList<SpanQuery> sqs = new ArrayList<SpanQuery>();
        for (SpanQuery sq : qs) {
            if (sq != null) {
                sqs.add(sq);
            }
        }
        return sqs.toArray(new SpanQuery[1]);
    }

    public SpanQuery getPlusOneQueries(VocabularyReview vocab) {
        return doGetPlusOneQueries(
                vocab.getFeatures(),
                vocab.getAttitudePlusOne(),
                vocab.getAttitudeMinusOne(),
                vocab.getProhibited(),
                punctuationContents,
                getNegation());
    }

    public SpanQuery getTitlePlusOneQueries(VocabularyReview vocab) {
        return doGetPlusOneQueries(
                vocab.getFeaturesTitle(),
                vocab.getAttitudePlusOneTitle(),
                vocab.getAttitudeMinusOneTitle(),
                vocab.getProhibitedTitle(),
                punctuationTitle,
                getNegationTitle());
    }

    private SpanQuery doGetPlusOneQueries(
            SpanQuery features,
            SpanQuery plusOne,
            SpanQuery minusOne,
            SpanQuery prohibited,
            SpanQuery punctuation,
            SpanQuery negation) {

        SpanQuery query = plusOne;

        if (query == null) {
            return null;
        }

        SpanQuery attitude_feature0 = null;
        SpanQuery not_bad = null;

        if (features != null)
            attitude_feature0 = new SpanNearQuery(new SpanQuery[]{query,
                    features}, DEFAULT_WINDOW, false);
        else
            attitude_feature0 = query;

        if (minusOne != null) {
            // place not bad
            not_bad =
                    new SpanNotQuery(
                            new SpanNearQuery(
                                    makeSpanQueryList(
                                            features,
                                            new SpanNearQuery(
                                                    new SpanQuery[]{negation, minusOne}, DEFAULT_WINDOW, true)),
                                    DEFAULT_WINDOW, false),
                            new SpanOrQuery(makeSpanQueryList(
                                    punctuation,
                                    prohibited)));
        }

        SpanQuery attitude_feature =
                new SpanNotQuery(
                        attitude_feature0,
                        new SpanOrQuery(makeSpanQueryList(
                                punctuation,
                                prohibited,
                                negation,
                                minusOne)));

        if (not_bad != null) {
            attitude_feature = new SpanOrQuery(
                    new SpanQuery[]{not_bad, attitude_feature});
        }
        return attitude_feature;
    }

    public SpanQuery getMinusOneQueries(VocabularyReview vocab) {
        return doGetMinusOneQueries(
                vocab.getFeatures(),
                vocab.getAttitudePlusOne(),
                vocab.getAttitudeMinusOne(),
                vocab.getProhibited(),
                getNegation(),
                punctuationContents);
    }

    public SpanQuery getTitleMinusOneQueries(VocabularyReview vocab) {
        return doGetMinusOneQueries(
                vocab.getFeaturesTitle(),
                vocab.getAttitudePlusOneTitle(),
                vocab.getAttitudeMinusOneTitle(),
                vocab.getProhibitedTitle(),
                getNegationTitle(),
                punctuationTitle);
    }

    private SpanQuery doGetMinusOneQueries(
            SpanQuery features,
            SpanQuery plusOne,
            SpanQuery minusOne,
            SpanQuery prohibited,
            SpanQuery negation,
            SpanQuery punctuation) {

        SpanQuery bad_place = null;
        SpanQuery not_place = null;
        SpanQuery not_nice = null;

        // bad place
        SpanQuery[] sqs = makeSpanQueryList(minusOne, features);
        if (sqs.length == 1) {
            bad_place = sqs[0];
        } else if (sqs.length > 1) {
            bad_place = new SpanNearQuery(sqs, DEFAULT_WINDOW, false);
        }

        // not a place
        if (features != null)
            not_place = new SpanNearQuery(makeSpanQueryList(negation,
                    features), 3, true);

        // not a nice place
        SpanQuery nice = null;
        SpanQuery very_ok = null;

        if (plusOne != null && very_ok != null) {
            nice = new SpanOrQuery(new SpanQuery[]{plusOne, very_ok});
        } else if (plusOne != null) {
            nice = plusOne;
        } else {
            nice = very_ok;
        }

        if (nice != null && negation != null) {
            not_nice =
                    new SpanNearQuery(
                            makeSpanQueryList(
                                    features,
                                    new SpanNearQuery(makeSpanQueryList(negation, nice),
                                            DEFAULT_WINDOW, true)),
                            DEFAULT_WINDOW, false);
        }

        SpanQuery query =
                new SpanOrQuery(
                        makeSpanQueryList(
                                bad_place, not_place, not_nice));

        // negative, to exclude
        SpanQuery not_bad =
                new SpanNearQuery(
                        makeSpanQueryList(negation, minusOne), 3, true);

        sqs = makeSpanQueryList(
                punctuation,
                prohibited,
                not_bad);

        if (sqs.length == 1) {
            query = new SpanNotQuery(query, sqs[0]);
        } else if (sqs.length > 1) {
            query = new SpanNotQuery(query, new SpanOrQuery(sqs));
        }

        return query;
    }

    final public static SpanQuery punctuationContents =
            new SpanTermQuery(new Term(ExcerptIndexer.CONTENTS, ExcerptIndexer.PUNCT));

    final public static SpanQuery punctuationTitle =
            new SpanTermQuery(new Term(ExcerptIndexer.TITLE, ExcerptIndexer.PUNCT));
}
