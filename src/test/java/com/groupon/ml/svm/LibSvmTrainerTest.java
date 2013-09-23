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

import com.groupon.nakala.analysis.Analysis;
import com.groupon.nakala.analysis.AnalysisCollector;
import com.groupon.nakala.analysis.SingletonAnalysisCollector;
import com.groupon.nakala.core.*;
import com.groupon.nakala.db.CollectionParameters;
import com.groupon.nakala.db.CollectionReader;
import com.groupon.nakala.db.TsvCategorizedTextCollectionReader;
import com.groupon.nakala.normalization.CaseNormalizer;
import com.groupon.nakala.normalization.StringNormalizer;
import com.groupon.util.io.IoUtil;
import com.mongodb.util.TestCase;
import libsvm.svm_problem;
import org.junit.Test;

import java.io.StringReader;
import java.util.*;

/**
 * @author npendar@groupon.com
 */
public class LibSvmTrainerTest extends TestCase {

    private static final String input = "1\trestaurant,bar\tthis is a restaurant and bar.\n" +
            "2\tbar,nightclub\tthis one is a nightclub and bar.\n" +
            "3\tpark\twelcome to our gorgeous park\n";

    private static final List<Set<Double>> expectedYs = new ArrayList<Set<Double>>();

    static {
        Set<Double> y = new HashSet<Double>();
        y.add(0d); //bar
        y.add(3d); //restaurant
        expectedYs.add(y);
        expectedYs.add(y);

        y = new HashSet<Double>();
        y.add(0d); //bar
        y.add(1d); //nightclub
        expectedYs.add(y);
        expectedYs.add(y);

        y = new HashSet<Double>();
        y.add(2d); //park
        expectedYs.add(y);
    }

    private static final String[] labels = {
            "bar",
            "nightclub",
            "park",
            "restaurant"
    };

    private LibSvmTrainer trainer;


    private void initializeTrainer(boolean sample) {
        Features features = new Features();
        features.initialize(new StringReader("restaurant\t1.0\nbar\t1.0\nnightclub\t1.0\npark\t1.0\n"));

        TokenizerStream tokenizer = new RegexpTokenizerStream();
        List<StringNormalizer> normalizers = new LinkedList<StringNormalizer>();
        normalizers.add(new CaseNormalizer());

        Parameters representerParams = new Parameters();
        representerParams.set(Constants.TOKENIZER, tokenizer);
        representerParams.set(Constants.FEATURES, features);
        representerParams.set(Constants.NORMALIZERS, normalizers);

        TextRepresenter representer = new FeatureWeightTextRepresenter();
        representer.initialize(representerParams);

        Parameters trainerParams = new Parameters();
        trainerParams.set(Constants.REPRESENTER, representer);
        trainerParams.set(Constants.FIND_BEST_PARAMETERS, true);
        trainerParams.set(Constants.NUMBER_OF_THREADS, 4);

        if (sample) {
            trainerParams.set(Constants.SAMPLE, 0.5);
        }

        trainer = new LibSvmTrainer();
        trainer.initialize(trainerParams);

    }

    private CollectionReader getReader(String corpus) throws Exception {
        CollectionParameters params = new CollectionParameters();
        params.set(CollectionParameters.FILE_NAME, IoUtil.createTempFile(corpus));
        params.set(CollectionParameters.ID_FIELD, 0);
        params.set(CollectionParameters.LABEL_FIELD, 1);
        params.set(CollectionParameters.TEXT_FIELD, 2);
        CollectionReader reader = new TsvCategorizedTextCollectionReader();
        reader.initialize(params);
        return reader;
    }

    @Test
    public void testLoadData() throws Exception {
        initializeTrainer(false);
        svm_problem svmProblem = trainer.loadData(getReader(input));
        assertEquals(5, svmProblem.l);

        List<String> inferredLabels = trainer.getLabelList();
        assertEquals(labels.length, inferredLabels.size());
        for (int i = 0; i < labels.length; ++i) {
            assertEquals(labels[i], inferredLabels.get(i));
        }

        assertEquals(svmProblem.l, svmProblem.x.length);
        assertEquals(svmProblem.l, svmProblem.y.length);

        for (int i = 0; i < svmProblem.l; ++i) {
            assertTrue(expectedYs.get(i).contains(svmProblem.y[i]));
        }
    }

    @Test
    public void testTraining() throws Exception {
        initializeTrainer(false);
        String corpus = input + input + input + input + input;
        AnalysisCollector ac = trainer.analyze(getReader(corpus));
        assertTrue(ac instanceof SingletonAnalysisCollector);
        SingletonAnalysisCollector sac = (SingletonAnalysisCollector) ac;
        Analysis a = sac.getAnalysis();
        assertTrue(a instanceof LibSvmTrainerAnalysis);
        assertNotNull(((LibSvmTrainerAnalysis) a).getModel());
        assertNotNull(((LibSvmTrainerAnalysis) a).getScaler());
    }

    @Test
    public void testSampling() throws Exception {
        initializeTrainer(true);
        String corpus = input + input + input + input;
        svm_problem sample = trainer.do_sample(trainer.loadData(getReader(corpus)));
        assertEquals(10, sample.l); // each input contains 5 expanded items
    }
}
