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
import com.groupon.ml.TextClassifier;
import com.groupon.nakala.analysis.Analysis;
import com.groupon.nakala.core.*;
import com.groupon.nakala.exceptions.AnalyzerFailureException;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

import java.io.IOException;

/**
 * @author npendar@groupon.com
 */
public final class LibSvmTextClassifier extends TextClassifier {

    private int[] labelIndeces;
    private svm_model model;

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        super.initialize(params);
        try {
            model = svm.svm_load_model(((ResourceReader) params.get(Constants.MODEL)).getReader());
        } catch (IOException e) {
            throw new ResourceInitializationException("Failed to load SVM model.", e);
        }

        labelIndeces = new int[labels.size()];
        svm.svm_get_labels(model, labelIndeces);
        representer = (TextRepresenter) params.get(Constants.REPRESENTER);
    }

    @Override
    public Analysis analyze(Analyzable a) throws AnalyzerFailureException {
        if (a == null) return null;

        if (!(a instanceof IdentifiableTextContent)) {
            throw new AnalyzerFailureException("Analyzable not identifiable. This analyzer requires an IdentifiableTextContent.");
        }

        IdentifiableTextContent tc = (IdentifiableTextContent) a;

        try {
            svm_node[] rep = representer.represent(tc.getText()).toSvmNodes();

            // Calculate probabilities of class membership
            double[] probs = new double[labels.size()];
            svm.svm_predict_probability(model, rep, probs);

            ClassificationAnalysis analysis = new ClassificationAnalysis(tc.getId());
            for (int i = 0; i < labelIndeces.length; ++i) {
                analysis.addClassification(labels.get(labelIndeces[i]), probs[i] >= threshold ? probs[i] : 0d);
            }
            return analysis;
        } catch (Exception e) {
            throw new AnalyzerFailureException("Classifier failed on record " + tc.getId(), e);
        }
    }
}
