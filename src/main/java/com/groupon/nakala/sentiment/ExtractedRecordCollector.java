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

import com.groupon.nakala.analysis.Analysis;
import com.groupon.nakala.db.DataStore;
import com.groupon.nakala.db.FlatFileStore;
import com.groupon.nakala.exceptions.StoreException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author npendar@groupon.com
 */
public class ExtractedRecordCollector implements Analysis {

    public static int maxQuoteLength = 512;
    public static int maxAnnotatedQuoteLength = 640;
    public static int maxTitleSize = 100;

    protected static final Logger logger = Logger.getLogger(ExtractedRecordCollector.class);

    protected Map<String, ExtractedReviewRecord> processedRecordCollector =
            new HashMap<String, ExtractedReviewRecord>(10000, 0.75f);

    protected PrePostProcessor prePostProcessor;

    public ExtractedRecordCollector(PrePostProcessor prePostProcessor) {
        this.prePostProcessor = prePostProcessor;
    }

    /**
     * Add a new extraction.
     * If overlapping extraction exists, keep longer extraction.
     */
    public boolean add(ExtractedReviewRecord rec) {

        boolean add = true;
        String toRemove = null;

        for (Map.Entry<String, ExtractedReviewRecord> e : processedRecordCollector.entrySet()) {
            String keyPrev = e.getKey();
            ExtractedReviewRecord erPrev = e.getValue();
            // If same descID, domain and type
            if (rec.getDescID() == erPrev.getDescID()
                    && rec.getExtractionType() == erPrev.getExtractionType()
                    && rec.getDomain().equals(erPrev.getDomain())) {
                // If new longer than old; remove old and add new
                if (rec.getStartToken() <= erPrev.getStartToken()
                        && rec.getEndToken() >= erPrev.getEndToken()) {
                    toRemove = keyPrev;
                    break;
                    // If new starts before old and overlaps and is negative
                    // remove old, add new
                } else if (rec.getStartToken() < erPrev.getStartToken()
                        && rec.getEndToken() > erPrev.getStartToken()
                        && erPrev.getScore() > 0
                        && rec.getScore() < 0) {
                    toRemove = keyPrev;
                    break;
                    // If no overlap add new and keep old
                } else if (rec.getEndToken() <= erPrev.getStartToken()
                        || erPrev.getEndToken() <= rec.getStartToken()) {
                    continue;
                    // Else, new is shorter than old; ignore new
                } else { // if (erPrev.getScore()*rec.getScore() > 0) {
                    add = false;
                    break;
                }
            }
        }

        if (toRemove != null) {
            processedRecordCollector.remove(toRemove);
        }

        if (add) {
            String newKey = rec.getDomain() + " " + rec.getDescID() + " "
                    + rec.getStartTokenToStringLeading0s() + " "
                    + rec.getEndToken() + " " + rec.getScore();
            processedRecordCollector.put(newKey, rec);
        }
        return add;
    }

    public void setLogLevel(Level l) {
        logger.setLevel(l);
    }

    public int size() {
        return processedRecordCollector.size();
    }

    public boolean isEmpty() {
        return processedRecordCollector.isEmpty();
    }

    public Set<String> keySet() {
        return processedRecordCollector.keySet();
    }

    public Collection<ExtractedReviewRecord> values() {
        return processedRecordCollector.values();
    }

    public ExtractedReviewRecord get(String k) {
        return processedRecordCollector.get(k);
    }

    public Set<ExtractedReviewRecord> getTableToOutput() {
        overrideByTitleSentiment();
        amplifyByTitleWordsAndNormalizeScoresForDB();

        Map<String, List<ExtractedReviewRecord>> tmpTable =
                new HashMap<String, List<ExtractedReviewRecord>>();

        // group records by identical quotes for same domain
        for (ExtractedReviewRecord rec : processedRecordCollector.values()) {
            rec.prepareForDB(prePostProcessor, maxQuoteLength, maxAnnotatedQuoteLength, maxTitleSize);
            String key = rec.getDomain() + " " + rec.getQuote();
            List<ExtractedReviewRecord> ers = tmpTable.get(key);
            if (ers == null) {
                ers = new LinkedList<ExtractedReviewRecord>();
                tmpTable.put(key, ers);
            }
            ers.add(rec);
        }

        // now average out scores of identical quotes
        Set<ExtractedReviewRecord> table = new HashSet<ExtractedReviewRecord>();
        for (List<ExtractedReviewRecord> ers : tmpTable.values()) {
            Double avg = 0.0;
            for (ExtractedReviewRecord er : ers) {
                avg += er.getScore();
            }
            avg /= ers.size();
            ExtractedReviewRecord er = ers.get(0);
            if (er.getScore() != avg) {
                er.overrideScore(avg);
            }
            table.add(er);
        }
        return table;
    }

    /*
      * If title nakala.sentiment in a category contradicts a quote assign the value
      * of title to quote
      */
    private void overrideByTitleSentiment() {
        if (processedRecordCollector.isEmpty()) {
            return;
        }

        List<String> keys = new ArrayList<String>(processedRecordCollector.keySet());
        for (int i = 0; i < keys.size(); i++) {
            ExtractedReviewRecord er = processedRecordCollector.get(keys.get(i));
            if (er == null) {
                continue;
            }

            if (er.getExtractionType() == ExtractedReviewRecord.TYPE_OVERRIDE) {
                long titleDescId = er.getDescID();

                for (int j = 0; j < keys.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    ExtractedReviewRecord er1 = processedRecordCollector.get(keys.get(j));
                    if (er1 == null || titleDescId != er1.getDescID()) {
                        continue;
                    }
                    er1.overrideScoreByTitle(-1);
                    logger.debug("Hard Override: " + er1);
                }
                processedRecordCollector.remove(keys.get(i));
            } else if (er.getExtractionType() == ExtractedReviewRecord.TYPE_TITLE) {
                String titleDomain = er.getDomain();
                double titleScore = er.getScore();
                long titleDescId = er.getDescID();

                for (int j = 0; j < keys.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    ExtractedReviewRecord er1 = processedRecordCollector.get(keys.get(j));
                    if (er1 == null || titleDescId != er1.getDescID()) {
                        continue;
                    }
                    String domain = er1.getDomain();
                    if (!titleDomain.equals(domain)) {
                        continue;
                    }
                    double score = er1.getScore();
                    if (titleScore * score < 0) {
                        er1.overrideScoreByTitle(-1 * score);
                        logger.debug("Override by title score: " + er1);
                    }
                }
                processedRecordCollector.remove(keys.get(i));
            }
        }
    }

    private void amplifyByTitleWordsAndNormalizeScoresForDB() {
        if (processedRecordCollector.isEmpty()) {
            return;
        }

        for (ExtractedReviewRecord er : processedRecordCollector.values()) {
            if (er == null) {
                continue;
            }
            er.amplifyByTitleWords();
            er.normalizeScoreForDB();
        }
    }

    @Override
    synchronized public void store(DataStore ds) throws StoreException {
        if (ds instanceof FlatFileStore) {
            for (ExtractedReviewRecord rec : getTableToOutput()) {
                ((FlatFileStore) ds).getPrintStream().println(rec);
            }
        } else {
            throw new StoreException("Unsupported data store " + ds.getClass().getName());
        }
    }
}
