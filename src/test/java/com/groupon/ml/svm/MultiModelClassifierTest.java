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
import com.groupon.nakala.core.*;
import com.groupon.nakala.exceptions.AnalyzerFailureException;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author npendar@groupon.com
 */
public class MultiModelClassifierTest {
    @Test
    public void testAnalyze() throws Exception {
        double[] cs = new double[]{1.0, 2.0, 3.0, 4.0, 5.0};
        IdentifiableTextContent itc = new IdentifiableTextContent(new Id(1), "0123456789");

        List<Classifier> classifiers = new ArrayList<Classifier>(5);
        for (double c : cs) {
            classifiers.add(new DummyClassifier(c));
        }

        Parameters params = new Parameters();
        params.set(Constants.ANALYZERS, classifiers);

        MultiModelClassifier mmc = new MultiModelClassifier();
        mmc.initialize(params);

        ClassificationAnalysis analysis = (ClassificationAnalysis) mmc.analyze(itc);
        Assert.assertEquals(new Id(1), analysis.getId());
        for (double c : cs) {
            String label = String.format("%2.1f", c);
            Assert.assertEquals(itc.getText().length() / c, analysis.getScore(label));
        }
        Assert.assertNull(analysis.getScore("other"));
    }

    private class DummyClassifier extends Classifier {
        double c;

        public DummyClassifier(double c) {
            this.c = c;
        }

        @Override
        public Analysis analyze(Analyzable a) throws AnalyzerFailureException {
            IdentifiableTextContent itc = (IdentifiableTextContent) a;
            ClassificationAnalysis analysis = new ClassificationAnalysis(itc.getId());
            analysis.addClassification(String.format("%2.1f", c), itc.getText().length() / c);
            analysis.addClassification("other", 0d);
            return analysis;
        }
    }
}
