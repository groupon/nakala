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

package com.groupon.ml;

import com.groupon.nakala.analysis.AbstractCollectionAnalyzer;
import com.groupon.nakala.analysis.AnalysisCollector;
import com.groupon.nakala.core.*;
import com.groupon.nakala.db.CollectionReader;
import com.groupon.nakala.exceptions.AnalyzerFailureException;
import com.groupon.nakala.exceptions.ResourceInitializationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author npendar@groupon.com
 */

public class ClassifierEvaluator extends AbstractCollectionAnalyzer {
    private Classifier classifier;
    private double minThreshold = -1d;
    private double maxThreshold = -1d;
    private double step = -1d;
    private String targetClass;

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        super.initialize(params);
        if (params.contains(Constants.ANALYZER)) {
            if (params.get(Constants.ANALYZER) instanceof Classifier) {
                classifier = (Classifier) params.get(Constants.ANALYZER);
            } else {
                throw new ResourceInitializationException("Analyzer must extend Classifier. Found " +
                        params.get(Constants.ANALYZER).getClass().getName());
            }
        } else {
            throw new ResourceInitializationException("No classifier specified.");
        }


        if (params.contains(Constants.MIN_THRESHOLD)) {
            minThreshold = params.getDouble(Constants.MIN_THRESHOLD);
        }

        if (params.contains(Constants.MAX_THRESHOLD)) {
            maxThreshold = params.getDouble(Constants.MAX_THRESHOLD);
        }

        if (params.contains(Constants.THRESHOLD_STEP)) {
            step = params.getDouble(Constants.THRESHOLD_STEP);
        }

        if (params.contains(Constants.TARGET_CLASS)) {
            targetClass = params.getString(Constants.TARGET_CLASS);
            logger.debug("Target Class: " + targetClass);
        }
    }

    @Override
    public void shutdown() {
        classifier.shutdown();
    }

    @Override
    public AnalysisCollector analyze(CollectionReader cr) throws AnalyzerFailureException {
        logger.debug("Classifier: " + classifier.getClass().getName());
        logger.debug("Labels: " + classifier.getLabels().size());
        logger.debug("Processing " + cr.getSize() + " documents.");

        Map<Id, Set<String>> id2trueLabels = new HashMap<Id, Set<String>>();
        ClassificationAnalysisCollector cac = new ClassificationAnalysisCollector();
        int count = 0;
        for (Analyzable a : cr) {
            logger.debug("Processing " + ++count + " of " + cr.getSize());
            cac.addAnalysis(classifier.analyze(a));
            CategorizedTextContent ctc = (CategorizedTextContent) a;

            Set<String> trueCategories;
            if (targetClass == null) {
                trueCategories = ctc.getCategories();
            } else {
                trueCategories = new HashSet<String>();
                if (ctc.getCategories().contains(targetClass)) {
                    trueCategories.add(targetClass);
                    if (ctc.getCategories().size() > 1) {
                        trueCategories.add("other"); // Item belongs both to target class and "other".
                    }
                } else {
                    trueCategories.add("other");
                }
            }
            id2trueLabels.put(ctc.getId(), trueCategories);
        }

        logger.debug("Calculating metrics");

        ClassificationEvaluation ce = new ClassificationEvaluation(
                classifier.getLabels(),
                id2trueLabels,
                cac);

        if (minThreshold > -1) {
            ce.setMinThreshold(minThreshold);
        }

        if (maxThreshold > -1) {
            ce.setMaxThreshold(maxThreshold);
        }

        if (step > -1) {
            ce.setStep(step);
        }

        logger.debug("done!");
        return ce;
    }
}
