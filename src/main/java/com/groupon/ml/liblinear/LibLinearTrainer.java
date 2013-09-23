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

package com.groupon.ml.liblinear;

import com.groupon.ml.svm.ValueScaler;
import com.groupon.nakala.analysis.AbstractCollectionAnalyzer;
import com.groupon.nakala.analysis.AnalysisCollector;
import com.groupon.nakala.analysis.SingletonAnalysisCollector;
import com.groupon.nakala.core.*;
import com.groupon.nakala.db.CollectionReader;
import com.groupon.nakala.exceptions.AnalyzerFailureException;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import de.bwaldvogel.liblinear.*;
import libsvm.svm_node;
import libsvm.svm_parameter;

import java.util.*;

/**
 * @author npendar@groupon.com
 */
public class LibLinearTrainer extends AbstractCollectionAnalyzer {

    private static final double EPSILON = 0.01;
    private static final SolverType SOLVER_TYPE = SolverType.L2R_LR;

    private double c = 1d;
    private TextRepresenter representer;
    private ValueScaler scaler;

    private List<String> labelList;

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        super.initialize(params);
        if (params.contains(Constants.REPRESENTER)) {
            representer = (TextRepresenter) params.get(Constants.REPRESENTER);
        } else {
            throw new ResourceInitializationException("No text representer specified.");
        }

        if (params.contains(Constants.C)) {
            c = params.getDouble(Constants.C);
        }
    }


    @Override
    public void shutdown() {
    }

    public Problem loadData(CollectionReader cr) {
        logger.debug("Loading " + cr.getSize() + " documents.");
        List<svm_node[]> data = new LinkedList<svm_node[]>();
        List<Set<String>> labels = new LinkedList<Set<String>>();
        Set<String> allLabels = new HashSet<String>();

        for (Analyzable a : cr) {
            CategorizedTextContent ctc = (CategorizedTextContent) a;
            labels.add(ctc.getCategories());
            allLabels.addAll(ctc.getCategories());
            data.add(representer.represent(ctc.getText()).toSvmNodes());
        }

        labelList = new ArrayList<String>(allLabels);
        Collections.sort(labelList);

        logger.debug("Total labels: " + labelList.size());
        logger.trace("Labels: " + labelList);

        int numberOfExpandedData = 0;
        for (Set<String> ls : labels) {
            numberOfExpandedData += ls.size();
        }

        // Scale data
        logger.debug("Scaling data.");
        scaler = new ValueScaler(0, 1, data.toArray(new svm_node[0][]));
        svm_node[][] scaledData = scaler.getScaledData();

        // For every label, generate a copy of the data item.
        logger.debug("Generating " + numberOfExpandedData + " records.");
        svm_node[][] expandedData = new svm_node[numberOfExpandedData][];
        double[] expandedLabels = new double[numberOfExpandedData];
        int i = 0;
        int xi = 0;
        for (svm_node[] dataItem : scaledData) {
            Set<String> trueLabels = labels.get(i++);
            for (String label : trueLabels) {
                double labelIndex = labelList.indexOf(label);
                expandedData[xi] = dataItem.clone();
                expandedLabels[xi++] = labelIndex;
            }
        }
        Problem problem = new Problem();
        problem.l = numberOfExpandedData;
        problem.n = representer.getFeatures().size();
        problem.x = LibLinearUtils.toLibLinearDataSet(expandedData);
        problem.y = expandedLabels;

        return problem;
    }

    public List<String> getLabelList() {
        return labelList;
    }

    public svm_parameter getDefaultSvmParameters() {
        svm_parameter param = new svm_parameter();
        // default values
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.degree = 3;
        param.gamma = 0;    // 1/num_features
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 100;
        param.C = 1;
        param.eps = 1e-3;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 0;
        param.nr_weight = 0;
        param.weight_label = new int[0];
        param.weight = new double[0];
        return param;
    }

    @Override
    public AnalysisCollector analyze(CollectionReader cr) throws AnalyzerFailureException {
        Problem problem = loadData(cr);
        Parameter parameter = new Parameter(SOLVER_TYPE, c, EPSILON);

        logger.debug("Training with C=" + c);
        Model model = Linear.train(problem, parameter);

        logger.debug("Done!");
        return new SingletonAnalysisCollector(new LibLinearTrainerAnalysis(model, scaler, labelList));
    }
}
