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

import com.groupon.nakala.core.*;
import com.groupon.nakala.db.CollectionReader;
import com.groupon.nakala.exceptions.AnalyzerFailureException;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import com.groupon.nakala.normalization.StringNormalizer;
import com.groupon.util.collections.CollectionUtil;
import com.groupon.util.collections.Histogram;
import com.groupon.util.math.StatUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author npendar@groupon.com
 */
public class BnsWeightCalculator extends AbstractCollectionAnalyzer {

    private static final String OTHER_CATEGORY = "other_category";

    private List<StringNormalizer> normalizers;
    private TokenizerStream tokenizer;
    private StringSet stopwords;
    private int minDF = 3;
    private int maxFeatureSize = 0;
    private double minFeatureWeight = 0d;
    private boolean useAbsoluteValues = true;
    private String targetClass = null;
    private BnsCalculator bnsCalculator;

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        super.initialize(params);

        if (params.contains(Constants.TOKENIZER)) {
            tokenizer = (TokenizerStream) params.get(Constants.TOKENIZER);
        } else {
            throw new ResourceInitializationException("No tokenizer specified.");
        }

        if (params.contains(Constants.NORMALIZERS)) {
            normalizers = (List<StringNormalizer>) params.get(Constants.NORMALIZERS);
        }

        if (params.contains(Constants.MIN_DF)) {
            minDF = params.getInt(Constants.MIN_DF);
        }

        if (params.contains(Constants.STOPWORDS)) {
            stopwords = (StringSet) params.get(Constants.STOPWORDS);
        }

        if (params.contains(Constants.MAX_FEATURE_SIZE)) {
            maxFeatureSize = params.getInt(Constants.MAX_FEATURE_SIZE);
            if (maxFeatureSize < 1) {
                throw new ResourceInitializationException(Constants.MAX_FEATURE_SIZE + " must be > 1");
            }
        }

        if (params.contains(Constants.MIN_FEATURE_WEIGHT)) {
            minFeatureWeight = params.getDouble(Constants.MIN_FEATURE_WEIGHT);
            if (minFeatureWeight <= 0d) {
                throw new ResourceInitializationException(Constants.MIN_FEATURE_WEIGHT + " must be > 0.0");
            }
        }

        if (params.contains(Constants.TARGET_CLASS)) {
            targetClass = params.getString(Constants.TARGET_CLASS);
            if (StringUtils.isEmpty(targetClass)) {
                throw new ResourceInitializationException(Constants.TARGET_CLASS + " specified but empty.");
            }
            logger.debug("Targeting class " + targetClass);
            bnsCalculator = new BinaryClassBnsCalculator(targetClass);
        } else {
            bnsCalculator = new MultiClassBnsCalculator();
        }

        if (params.contains(Constants.USE_ABSOLUTE_VALUES)) {
            useAbsoluteValues = params.getBoolean(Constants.USE_ABSOLUTE_VALUES);
        }
        logger.debug("Using absolute values: " + useAbsoluteValues);

        logger.debug("tokenizer: " + tokenizer.getClass().getName());
        logger.debug("normalizers: ");
        if (normalizers == null) {
            logger.debug("no normalizer");
        } else {
            for (StringNormalizer normalizer : normalizers) {
                logger.debug(normalizer.getClass().getName());
            }
        }
        logger.debug("stopwords: " + (stopwords == null ? 0 : stopwords.size()));
        logger.debug("minDF: " + minDF);

        if (maxFeatureSize > 0) {
            logger.debug("maxFeatureSize: " + maxFeatureSize);
        }

        if (minFeatureWeight > 0d) {
            logger.debug("minFeatureWeight: " + minFeatureWeight);
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public AnalysisCollector analyze(CollectionReader cr) throws AnalyzerFailureException {

        Map<String, Histogram<String>> word2categoryCount = new HashMap<String, Histogram<String>>();
        Histogram<String> docCountInCategory = new Histogram<String>();

        logger.debug("processing " + cr.getSize() + " documents.");
        // get word dfs per category
        for (Analyzable a : cr) {
            CategorizedTextContent ctc = (CategorizedTextContent) a;
            if (passFilter != null && !passFilter.passes(ctc))
                continue;

            if (blockFilter != null && blockFilter.blocks(ctc))
                continue;

            Set<String> categories;
            if (targetClass != null) {
                categories = new HashSet<String>();
                for (String category : ctc.getCategories()) {
                    if (category.equals(targetClass)) {
                        categories.add(category);
                    } else {
                        categories.add(OTHER_CATEGORY);
                    }
                }
            } else {
                categories = ctc.getCategories();
            }

            for (String category : categories) {
                docCountInCategory.add(category);
            }

            for (String word : tokenizer.getUniqueTokens(ctc.getText(), normalizers)) {
                if (stopwords != null && stopwords.contains(word)) {
                    continue;
                }

                Histogram<String> categoryCount = word2categoryCount.get(word);

                if (categoryCount == null) {
                    categoryCount = new Histogram<String>();
                    word2categoryCount.put(word, categoryCount);
                }

                for (String category : categories) {
                    categoryCount.add(category);
                }
            }
        }

        KeyValueListAnalysisCollector<String, Double> analysisCollector =
                new KeyValueListAnalysisCollector<String, Double>();

        int totalDocs = (int) CollectionUtil.sum(docCountInCategory.values());

        for (Map.Entry<String, Histogram<String>> e : word2categoryCount.entrySet()) {
            String word = e.getKey();
            Histogram<String> wordDfs = e.getValue();

            int df = (int) CollectionUtil.sum(wordDfs.values());
            if (df < minDF) continue;

            double bns = bnsCalculator.calculate(docCountInCategory, totalDocs, wordDfs, useAbsoluteValues);

            if (minFeatureWeight > 0d && bns < minFeatureWeight)
                continue;

            analysisCollector.addAnalysis(new KeyValuePairAnalysis<String, Double>(word, bns));
        }
        analysisCollector.setSortByValues(true);
        analysisCollector.setReverseSort(true);

        if (maxFeatureSize > 0) analysisCollector.setMaxSize(maxFeatureSize);

        return analysisCollector;
    }

    protected double squish(double n) {
        if (n == 0d) n = 0.0000001;
        else if (n == 1d) n -= 0.0000001;
        return n;
    }

    private interface BnsCalculator {
        public double calculate(Histogram<String> docCountInCategories, int totalDocs, Histogram<String> wordDfs,
                                boolean useAbsoluteValues);
    }

    private class MultiClassBnsCalculator implements BnsCalculator {
        /**
         * Calculates BNS score of feature for all categories, returns max BNS value.
         *
         * @param docCountInCategories
         * @param totalDocs
         * @param wordDfs
         * @return
         */
        @Override
        public double calculate(Histogram<String> docCountInCategories, int totalDocs, Histogram<String> wordDfs,
                                boolean useAbsoluteValues) {
            double maxBns = 0d;
            for (Map.Entry<String, Integer> e : docCountInCategories.entrySet()) {
                String category = e.getKey();
                int docsInCategory = e.getValue();
                int docsInOtherCategories = totalDocs - docsInCategory;

                Integer tp = wordDfs.get(category);
                if (tp == null) tp = 0;
                Integer fp = (int) CollectionUtil.sum(wordDfs.values()) - tp;

                double tpr = squish(tp / (double) docsInCategory);
                double fpr = squish(fp / (double) docsInOtherCategories);

                double bns = StatUtil.getInvCDF(tpr, true) - StatUtil.getInvCDF(fpr, true);
                if (useAbsoluteValues) bns = Math.abs(bns);
                if (bns > maxBns) maxBns = bns;
            }
            return maxBns;
        }
    }

    private class BinaryClassBnsCalculator implements BnsCalculator {
        private String targetClass;

        public BinaryClassBnsCalculator(String targetClass) {
            this.targetClass = targetClass;
        }

        /**
         * Calculates BNS score of feature only for the target category
         *
         * @param docCountInCategories
         * @param totalDocs
         * @param wordDfs
         * @return
         */
        @Override
        public double calculate(Histogram<String> docCountInCategories, int totalDocs, Histogram<String> wordDfs,
                                boolean useAbsoluteValues) {
            int docsInCategory = docCountInCategories.get(targetClass);
            int docsInOtherCategories = totalDocs - docsInCategory;

            Integer tp = wordDfs.get(targetClass);
            if (tp == null) tp = 0;
            Integer fp = (int) CollectionUtil.sum(wordDfs.values()) - tp;

            double tpr = squish(tp / (double) docsInCategory);
            double fpr = squish(fp / (double) docsInOtherCategories);

            double bns = StatUtil.getInvCDF(tpr, true) - StatUtil.getInvCDF(fpr, true);
            if (useAbsoluteValues) bns = Math.abs(bns);
            return bns;
        }
    }
}
