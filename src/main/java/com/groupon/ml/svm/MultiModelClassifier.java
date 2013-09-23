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

package com.groupon.ml.svm;

import com.groupon.ml.ClassificationAnalysis;
import com.groupon.ml.Classifier;
import com.groupon.nakala.analysis.Analysis;
import com.groupon.nakala.core.Analyzable;
import com.groupon.nakala.core.Constants;
import com.groupon.nakala.core.Identifiable;
import com.groupon.nakala.core.Parameters;
import com.groupon.nakala.exceptions.AnalyzerFailureException;
import com.groupon.nakala.exceptions.ResourceInitializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author npendar@groupon.com
 *         <p/>
 *         This convenience class wraps around a number of smaller classifiers. The output is
 *         an aggregation of the output of all the smaller classifiers.
 *         The label "other" from the wrapped classifiers is ignored.
 */

public class MultiModelClassifier extends Classifier {
    private List<Classifier> classifiers;
    private ExecutorService executorService;
    private int numberOfThreads;

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        if (params.contains(Constants.ANALYZERS)) {
            classifiers = (List<Classifier>) params.get(Constants.ANALYZERS);
        } else {
            throw new ResourceInitializationException("No classifiers specified.");
        }

        if (params.contains(Constants.NUMBER_OF_THREADS)) {
            numberOfThreads = params.getInt(Constants.NUMBER_OF_THREADS);
            if (numberOfThreads < 1) {
                throw new ResourceInitializationException(Constants.NUMBER_OF_THREADS + " must be >= 1");
            }
            numberOfThreads = Math.min(numberOfThreads, classifiers.size());
        } else {
            numberOfThreads = classifiers.size();
        }

        executorService = Executors.newFixedThreadPool(numberOfThreads);
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
        for (Classifier classifier : classifiers) {
            classifier.shutdown();
        }
    }

    @Override
    public Analysis analyze(Analyzable a) throws AnalyzerFailureException {
        if (!(a instanceof Identifiable)) {
            throw new AnalyzerFailureException("Input not Identifiable.");
        }

        Identifiable identifiable = (Identifiable) a;

        List<Future<ClassificationAnalysis>> analyses = new ArrayList<Future<ClassificationAnalysis>>(classifiers.size());
        for (Classifier classifier : classifiers) {
            analyses.add(executorService.submit(new CallableClassifier(classifier, a)));
        }

        ClassificationAnalysis analysis = new ClassificationAnalysis(identifiable.getId());
        for (Future<ClassificationAnalysis> future : analyses) {
            try {
                for (Map.Entry<String, Double> e : future.get().getClassifications().entrySet()) {
                    if (e.getKey().equals("other")) continue;
                    analysis.addClassification(e.getKey(), e.getValue());
                }
            } catch (Exception e) {
                throw new AnalyzerFailureException(e);
            }
        }
        return analysis;
    }

    class CallableClassifier implements Callable {
        private Classifier classifier;
        private Analyzable analyzable;

        public CallableClassifier(Classifier classifier, Analyzable analyzable) {
            this.classifier = classifier;
            this.analyzable = analyzable;
        }

        @Override
        public ClassificationAnalysis call() throws Exception {
            return (ClassificationAnalysis) classifier.analyze(analyzable);
        }
    }
}
