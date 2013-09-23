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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.Spans;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author npendar@groupon.com
 */
class ReviewExtractor {

    private static final String[] domains = {
            VocabularyReview.AQUARIUM,
//		VocabularyReview.BALLET, 
//		VocabularyReview.BASEBALL,
            VocabularyReview.BEACH,
            VocabularyReview.BOWL,
            VocabularyReview.CLEANLINESS,
//		VocabularyReview.CLOTHING,
//		VocabularyReview.DIVING, 
            VocabularyReview.FAMILY,
            VocabularyReview.FOOD,
//		VocabularyReview.GIRLSGETAWAY,
//		VocabularyReview.GOLF, 
//		VocabularyReview.LOBBY,
//		VocabularyReview.LOUNGE, 
            VocabularyReview.LUXURY,
//		VocabularyReview.MARKET, 
            VocabularyReview.MUSEUM,
//		VocabularyReview.NIGHTLIFE,
//		VocabularyReview.OCEAN, 
            VocabularyReview.OVERALL,
            VocabularyReview.PARKING,
            VocabularyReview.PET,
            VocabularyReview.PLAYGROUND,
            VocabularyReview.QUIET,
            VocabularyReview.ROMANCE,
            VocabularyReview.ROOMCOMFORTABLE,
            VocabularyReview.ROOMSERVICE,
            VocabularyReview.ROOMSIZE,
//		VocabularyReview.SIGHTSEEING,
//		VocabularyReview.SKI, 
            VocabularyReview.SPA,
            VocabularyReview.STAFF,
            VocabularyReview.SWIM,
            VocabularyReview.THEMEPARK,
            VocabularyReview.TRANSPORTATION,
            VocabularyReview.VALUE,
            VocabularyReview.VIEW,
//		VocabularyReview.WATERSPORTS, 
//		VocabularyReview.WOMALONE,
            VocabularyReview.WOULDRETURN,
            VocabularyReview.ZOO
    };

    protected static final Pattern nonWord = Pattern.compile("\\W+");

    private String[] loadedDomains;

    // domain -> query_type -> query
    private Map<String, VocabularyReview> vocabs;
    private Map<String, Map<Byte, SpanQuery>> queries;

    protected static final Logger logger = Logger
            .getLogger(ReviewExtractor.class);
    private static final byte PLUS_ONE = 0,
            MINUS_ONE = 1,
            TITLE_PLUS_ONE = 2,
            TITLE_MINUS_ONE = 3;
    private static Set<String> stopWords;
    private SpanQuery hardOverridesSQ;
    private SpanQuery tabooSQ;
    private boolean polite;

    protected ReviewExtractor() {
    }

    public String[] getDomains() {
        return domains;
    }

    public static ReviewExtractor newInstance(String[] domainsToLoad) throws Exception {
        return newInstance(domainsToLoad, true);
    }

    public static ReviewExtractor newInstance(String[] domainsToLoad, boolean polite) throws IOException {
        ReviewExtractor re = new ReviewExtractor();
        stopWords = new HashSet<String>();
        for (Object w : StopAnalyzer.ENGLISH_STOP_WORDS_SET) {
            stopWords.add(w.toString());
        }

        VocabUtils vocabUtils = VocabUtils.newInstance();
        re.hardOverridesSQ = vocabUtils.getHardOverrides();
        re.queries = new HashMap<String, Map<Byte, SpanQuery>>();
        re.vocabs = new HashMap<String, VocabularyReview>();

        Map<Byte, SpanQuery> type2query = new HashMap<Byte, SpanQuery>();

        String[] ds = domains;
        if (domainsToLoad != null) {
            ds = domainsToLoad;
        }

        re.loadedDomains = ds;

        for (String domain : ds) {
            VocabularyReview vocab = VocabularyReview.newInstance(domain);
            type2query = new HashMap<Byte, SpanQuery>();
            type2query.put(PLUS_ONE, vocabUtils.getPlusOneQueries(vocab));
            type2query.put(MINUS_ONE, vocabUtils.getMinusOneQueries(vocab));
            type2query.put(TITLE_PLUS_ONE, vocabUtils.getTitlePlusOneQueries(vocab));
            type2query.put(TITLE_MINUS_ONE, vocabUtils.getTitleMinusOneQueries(vocab));
            re.queries.put(domain, type2query);
            re.vocabs.put(domain, vocab);
        }

        re.polite = polite;
        if (polite) {
            re.tabooSQ = vocabUtils.getTaboo();
        }

        return re;
    }

    public void setLogLevel(Level l) {
        logger.setLevel(l);
    }

    public ExtractedRecordCollector extract(IndexSearcher searcher, PrePostProcessor prePostProcessor)
            throws Exception {

        ExtractedRecordCollector erc = new ExtractedRecordCollector(prePostProcessor);
        IndexReader ir = searcher.getIndexReader();

        Map<Integer, List<HitSpan>> tabooSpans = null;

        if (polite) {
            tabooSpans = new HashMap<Integer, List<HitSpan>>();

            TopDocs topDocs = searcher.search(tabooSQ, 5000);
            if (topDocs.totalHits > 0) {
                Spans spans = tabooSQ.getSpans(ir);
                while (spans.next()) {
                    List<HitSpan> ms = tabooSpans.get(spans.doc());
                    if (ms != null) {
                        ms.add(new HitSpan(spans.start(), spans.end()));
                    } else {
                        ms = new LinkedList<HitSpan>();
                        ms.add(new HitSpan(spans.start(), spans.end()));
                        tabooSpans.put(spans.doc(), ms);
                    }
                }
            }
        }

        runSpan(searcher, hardOverridesSQ, -1, "", erc, null,
                ExtractedReviewRecord.TYPE_OVERRIDE,
                null);

        for (String domain : loadedDomains) {
            logger.debug("Domain: " + domain);
            Map<Byte, SpanQuery> type2query = queries.get(domain);

            SpanQuery sq = type2query.get(PLUS_ONE);
            if (sq != null) {
                runSpan(searcher, sq, 1, domain, erc,
                        formSetForHighLighting(sq,
                                vocabs.get(domain).getWordsOfInterest()),
                        ExtractedReviewRecord.TYPE_CONTENT,
                        tabooSpans);
            }

            sq = type2query.get(MINUS_ONE);
            if (sq != null) {
                runSpan(searcher, sq, -1, domain, erc,
                        formSetForHighLighting(sq,
                                vocabs.get(domain).getWordsOfInterest()),
                        ExtractedReviewRecord.TYPE_CONTENT,
                        tabooSpans);
            }

            sq = type2query.get(TITLE_PLUS_ONE);
            if (sq != null) {
                runSpan(searcher, sq, 1, domain, erc, null,
                        ExtractedReviewRecord.TYPE_TITLE,
                        null);
            }

            sq = type2query.get(TITLE_MINUS_ONE);
            if (sq != null) {
                runSpan(searcher, sq, -1, domain, erc, null,
                        ExtractedReviewRecord.TYPE_TITLE,
                        null);
            }
        }

        try {
            searcher.close();
        } catch (IOException ioe) {
            logger.error("Problem closing indexSearcher", ioe);
        }
        return erc;
    }

    private void runSpan(IndexSearcher searcher, SpanQuery spanQuery, double weight, String domain,
                         ExtractedRecordCollector erc,
                         Set<String> queryTermsToHighlight,
                         byte extractionType,
                         Map<Integer, List<HitSpan>> tabooSpans)
            throws Exception {

        IndexReader ir = searcher.getIndexReader();

        Spans spans = null;
        try {
            TopDocs topDocs = searcher.search(spanQuery, 5000);
            if (topDocs.totalHits == 0) {
                return;
            }
            spans = spanQuery.getSpans(ir);
        } catch (RuntimeException e) {
            logger.error("Runtime exception. Error running query "
                    + spanQuery.toString(), e);
        } catch (Exception e) {
            logger.error("Error running query " + spanQuery.toString(), e);
            return;
        } catch (Error e) {
            logger.error("Error running query " + spanQuery.toString(), e);
        }

        try {
            int currStart = -1,
                    currEnd = -1,
                    currDoc = -1;
            String title = "";
            Set<String> titleWords = null;

            while (spans.next()) {
                int docNo = spans.doc(),
                        start = spans.start(),
                        end = spans.end();
                Document doc = ir.document(docNo);

                if (docNo != currDoc) {
                    currStart = -1;
                    currEnd = -1;
                    // Get title words for current desc/review
                    title = doc.get(ExcerptIndexer.TITLE_ORIGINAL);
                    titleWords = new HashSet<String>();
                    for (String w : nonWord.split(title)) {
                        w = w.toLowerCase();
                        if (!stopWords.contains(w)) {
                            titleWords.add(w);
                        }
                    }
                } else if (start >= currStart
                        && start < currEnd
                        && end >= currEnd) {
                    continue;
                }

                currStart = start;
                currEnd = end;
                currDoc = docNo;

                List<String> accumTerms = new ArrayList<String>();

                if (extractionType != ExtractedReviewRecord.TYPE_OVERRIDE) {
                    ExtractedExprProcessor.reconstructExcerpt(accumTerms, start,
                            end, docNo, ir, extractionType, tabooSpans);
                    if (accumTerms.isEmpty()) {
                        continue;
                    }
                    if (queryTermsToHighlight != null) {
                        ExtractedExprProcessor.boldWordsOfInterest(accumTerms,
                                queryTermsToHighlight);
                    }
                }

                int descIdInt = 0;

                try {
                    descIdInt = Integer.parseInt(doc.get(ExcerptIndexer.DESC_ID));
                } catch (NumberFormatException e) {
                    descIdInt = 0;
                }

                ExtractedReviewRecord rec = new ExtractedReviewRecord(
                        descIdInt, start, end, weight, domain,
                        accumTerms, title, extractionType);
                rec.setTitleWords(titleWords);
                if (erc.add(rec)) {
                    logger.debug("Added hit: " + rec.toDebugString());
                }
            }
        } catch (Exception e) {
            logger.error("NEXT: Error running query " + spanQuery.toString(), e);
        } catch (Error e) {
            logger.error("NEXT: Error running query " + spanQuery.toString(), e);
        }
    }

    protected Set<String> formSetForHighLighting(Query plusOneQ,
                                                 Set<Pattern> featureSet) {

        if (plusOneQ == null) {
            return null;
        }

        Set<String> queryTermsToHighlight = new HashSet<String>();

        if (featureSet.size() == 0) {
            return queryTermsToHighlight;
        }

        Set<Term> termSet = new HashSet<Term>();
        plusOneQ.extractTerms(termSet);
        for (Pattern p : featureSet) {
            Matcher m = p.matcher("");
            for (Term term : termSet) {
                String w = term.text();
                m.reset(w);
                if (m.matches()) {
                    queryTermsToHighlight.add(w);
                }
            }
        }
        return queryTermsToHighlight;
    }
}
