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

import com.groupon.nakala.analysis.AbstractCollectionAnalyzer;
import com.groupon.nakala.analysis.AnalysisCollector;
import com.groupon.nakala.analysis.SingletonAnalysisCollector;
import com.groupon.nakala.core.*;
import com.groupon.nakala.db.CollectionReader;
import com.groupon.nakala.exceptions.AnalyzerFailureException;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import libsvm.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author npendar@groupon.com
 */
public class LibSvmTrainer extends AbstractCollectionAnalyzer {

    private boolean findBestParameters = false;
    private int nrFold = 5;
    private double c = 1d;
    private double gamma;
    private TextRepresenter representer;
    private ValueScaler scaler;
    private double sample = 1d;
    private Map<Integer, Double> weights;

    private double log2cBegin = -5;
    private double log2cEnd = 15;
    private double log2cStep = 2;

    private double log2gBegin = 3;
    private double log2gEnd = -15;
    private double log2gStep = -2;

    private List<String> labelList;

    private int numberOfThreads = 1;
    private String targetClass;

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        super.initialize(params);

        if (params.contains(Constants.REPRESENTER)) {
            representer = (TextRepresenter) params.get(Constants.REPRESENTER);
        } else {
            throw new ResourceInitializationException("No text representer specified.");
        }

        if (params.contains(Constants.FIND_BEST_PARAMETERS)) {
            findBestParameters = params.getBoolean(Constants.FIND_BEST_PARAMETERS);
        } else {
            gamma = 1.0 / (double) representer.getFeatures().size();
        }

        if (params.contains(Constants.C)) {
            if (findBestParameters) {
                logger.warn("Finding best parameters. The specified C value will be ignored.");
            } else {
                c = params.getDouble(Constants.C);
            }
        }

        if (params.contains(Constants.WEIGHTS)) {
            // Parse weight specs
            weights = new TreeMap<Integer, Double>();
            for (String piece : params.getString(Constants.WEIGHTS).split(",\\s*")) {
                String[] pair = piece.split(":");
                if (pair.length != 2) {
                    throw new ResourceInitializationException("Invalid weight specs in " +
                            params.getString(Constants.WEIGHTS));
                }
                try {
                    int i = Integer.parseInt(pair[0]);
                    double w = Double.parseDouble(pair[1]);
                    weights.put(i, w);
                } catch (Exception e) {
                    throw new ResourceInitializationException("Invalid weight specs in " +
                            params.getString(Constants.WEIGHTS), e);
                }
            }
        }

        if (params.contains(Constants.GAMMA)) {
            if (findBestParameters) {
                logger.warn("Finding best parameters. The specified gamma value will be ignored.");
            } else {
                gamma = params.getDouble(Constants.GAMMA);
            }
        }

        if (params.contains(Constants.SAMPLE)) {
            if (findBestParameters) {
                sample = params.getDouble(Constants.SAMPLE);
                if (sample <= 0 || sample > 1) {
                    throw new ResourceInitializationException("Invalid sample value " + sample);
                }
            } else {
                logger.warn("Sample value valid for parameter optimization only. Provided sample value will be ignored.");
            }
        }

        if (params.contains(Constants.NUMBER_OF_THREADS)) {
            numberOfThreads = params.getInt(Constants.NUMBER_OF_THREADS);
            if (numberOfThreads < 1) {
                throw new ResourceInitializationException(Constants.NUMBER_OF_THREADS + " must be >= 1");
            }
        }

        if (params.contains(Constants.TARGET_CLASS)) {
            targetClass = params.getString(Constants.TARGET_CLASS);
            logger.debug("Target Class: " + targetClass);
        }
    }

    @Override
    public void shutdown() {
    }

    private void do_find_best_parameters(svm_problem svmProblem) {
        svm_parameter svmParam = getDefaultSvmParameters();
        setWeights(svmParam);

        int maxIter = ((int) Math.ceil(Math.abs((log2cEnd - log2cBegin) / log2cStep)) + 1) *
                ((int) Math.ceil(Math.abs((log2gEnd - log2gBegin) / log2gStep)) + 1);

        // Run the grid search in separate CV threads
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        List<CvParams> cvParamsList = new ArrayList<CvParams>();

        for (double log2c = log2cBegin;
             (log2cBegin < log2cEnd && log2c <= log2cEnd) ||
                     (log2cBegin >= log2cEnd && log2c >= log2cEnd);
             log2c += log2cStep) {

            double c1 = Math.pow(2, log2c);

            for (double log2g = log2gBegin;
                 (log2gBegin < log2gEnd && log2g <= log2gEnd) ||
                         (log2gBegin >= log2gEnd && log2g >= log2gEnd);
                 log2g += log2gStep) {

                double gamma1 = Math.pow(2, log2g);

                svm_parameter svmParam1 = (svm_parameter) svmParam.clone();
                svmParam1.C = c1;
                svmParam1.gamma = gamma1;

                executorService.execute(new RunnableSvmCrossValidator(svmProblem, svmParam1, nrFold, cvParamsList));
            }
        }

        // now wait for all threads to complete by calling shutdown
        // note that this will NOT terminate the currently running threads, it just signals the thread pool to closeWriter
        // once all work is completed
        executorService.shutdown();

        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // don't care if we get interrupted
            }

            // every second, report statistics
            logger.debug(String.format("%% complete: %5.2f", cvParamsList.size() / (double) maxIter * 100));
            CvParams best = getBestCvParams(cvParamsList);
            CvParams worst = getWorstcvParams(cvParamsList);
            if (best != null) {
                logger.debug("Best accuracy: " + best.accuracy);
                logger.debug("Best C:        " + best.c);
                logger.debug("Best Gamma:    " + best.gamma);
            }
            if (worst != null) {
                logger.debug("Worst accuracy: " + worst.accuracy);
            }
        }

        CvParams best = getBestCvParams(cvParamsList);
        CvParams worst = getWorstcvParams(cvParamsList);
        if (best != null) {
            logger.debug("Best accuracy: " + best.accuracy);
            logger.debug("Best C:        " + best.c);
            logger.debug("Best Gamma:    " + best.gamma);

            c = best.c;
            gamma = best.gamma;
        } else {
            logger.error("Best CV parameters is null.");
        }
        if (worst != null) {
            logger.debug("Worst accuracy: " + worst.accuracy);
        }
    }

    private CvParams getBestCvParams(List<CvParams> cvParamsList) {
        CvParams best = null;
        double bestAccuracy = 0d;
        synchronized (cvParamsList) {
            for (CvParams cvParams : cvParamsList) {
                if (cvParams.accuracy > bestAccuracy) {
                    bestAccuracy = cvParams.accuracy;
                    best = cvParams;
                }
            }
        }
        return best;
    }

    private CvParams getWorstcvParams(List<CvParams> cvParamsList) {
        CvParams worst = null;
        double worstAccuracy = Double.MAX_VALUE;
        synchronized (cvParamsList) {
            for (CvParams cvParams : cvParamsList) {
                if (cvParams.accuracy < worstAccuracy) {
                    worstAccuracy = cvParams.accuracy;
                    worst = cvParams;
                }
            }
        }
        return worst;
    }

    public svm_problem loadData(CollectionReader cr) {
        logger.debug("Loading " + cr.getSize() + " documents.");
        List<svm_node[]> data = new LinkedList<svm_node[]>();
        List<Set<String>> labels = new LinkedList<Set<String>>();
        Set<String> allLabels = new HashSet<String>();

        for (Analyzable a : cr) {
            CategorizedTextContent ctc = (CategorizedTextContent) a;

            Set<String> categories;
            if (targetClass == null) {
                categories = ctc.getCategories();
            } else {
                categories = new HashSet<String>();
                if (ctc.getCategories().contains(targetClass)) {
                    // Not adding any other categories if item belongs to targetClass as well as some other category.
                    categories.add(targetClass);
                } else {
                    categories.add("other");
                }
            }

            labels.add(categories);
            allLabels.addAll(categories);
            data.add(representer.represent(ctc.getText()).toSvmNodes());
        }
        representer.shutdown();

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

        svm_problem svmProblem = new svm_problem();
        svmProblem.l = numberOfExpandedData;
        svmProblem.x = expandedData;
        svmProblem.y = expandedLabels;

        return svmProblem;
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

    public svm_problem do_sample(svm_problem svmProblem) {
        logger.debug("Creating " + sample + " sample");
        Map<Double, List<Integer>> label2index = new HashMap<Double, List<Integer>>();
        for (int i = 0; i < svmProblem.l; ++i) {
            double label = svmProblem.y[i];
            if (label2index.containsKey(label)) {
                label2index.get(label).add(i);
            } else {
                List<Integer> indeces = new LinkedList<Integer>();
                indeces.add(i);
                label2index.put(label, indeces);
            }
        }

        for (List<Integer> indeces : label2index.values()) {
            Collections.shuffle(indeces);
        }

        int newSize = (int) (svmProblem.l * sample);
        logger.debug("Original size: " + svmProblem.l);
        logger.debug("Sample size: " + newSize);
        double[] newlabels = new double[newSize];
        svm_node[][] newdata = new svm_node[newSize][];

        int i = 0;
        for (List<Integer> indeces : label2index.values()) {
            int catSize = (int) (indeces.size() * sample);
            for (int j = 0; j < catSize; ++j) {
                int index = indeces.remove(0);
                newlabels[i] = svmProblem.y[index];
                newdata[i] = svmProblem.x[index];
                if (++i >= newSize) {
                    break;
                }
            }
            if (i >= newSize) {
                break;
            }
        }

        // fill any remaining empty items caused due to rounding
        if (i < newSize) {
            for (List<Integer> indeces : label2index.values()) {
                if (indeces.isEmpty()) {
                    continue;
                }
                int index = indeces.remove(0);
                newlabels[i] = svmProblem.y[index];
                newdata[i] = svmProblem.x[index];
                if (++i >= newSize) {
                    break;
                }
            }
        }

        svm_problem newProblem = new svm_problem();
        newProblem.l = newSize;
        newProblem.x = newdata;
        newProblem.y = newlabels;

        return newProblem;
    }

    private void setWeights(svm_parameter svmParam) {
        if (weights != null) {
            svmParam.nr_weight = weights.size();
            svmParam.weight_label = new int[weights.size()];
            svmParam.weight = new double[weights.size()];
            for (int i = 0; i < weights.size(); ++i) {
                svmParam.weight_label[i] = i;
                svmParam.weight[i] = weights.get(i);
            }
            logger.debug("Class weights: " + weights);
        }
    }

    @Override
    public AnalysisCollector analyze(CollectionReader cr) throws AnalyzerFailureException {
        svm_problem svmProblem = loadData(cr);
        svm_problem sampled = null;
        if (findBestParameters) {
            if (sample < 1d) {
                logger.debug("Sampling.");
                sampled = do_sample(svmProblem);
            }
            logger.debug("Performing grid search.");
            do_find_best_parameters(sampled != null ? sampled : svmProblem);
        }
        svm_parameter svmParam = getDefaultSvmParameters();
        svmParam.probability = 1;
        svmParam.C = c;
        svmParam.gamma = gamma;
        setWeights(svmParam);

        logger.debug("Training with C=" + c + "  gamma=" + gamma);
        svm_model model = svm.svm_train(svmProblem, svmParam);

        logger.debug("Done!");
        return new SingletonAnalysisCollector(new LibSvmTrainerAnalysis(model, scaler, labelList, c, gamma));
    }
}

class CvParams {
    double gamma;
    double c;
    double accuracy;

    CvParams(double gamma, double c, double accuracy) {
        this.gamma = gamma;
        this.accuracy = accuracy;
        this.c = c;
    }
}

class RunnableSvmCrossValidator implements Runnable {

    svm_problem svmProblem;
    svm_parameter svmParam;
    int nrFold;
    List<CvParams> cvParamsList;

    public RunnableSvmCrossValidator(final svm_problem svmProblem,
                                     final svm_parameter svmParam,
                                     int nrFold,
                                     List<CvParams> cvParamsList) {
        this.svmProblem = svmProblem;
        this.svmParam = svmParam;
        this.nrFold = nrFold;
        this.cvParamsList = cvParamsList;
    }

    @Override
    public void run() {
        double[] target = new double[svmProblem.l];
        svm.svm_cross_validation(svmProblem, svmParam, nrFold, target);
        int totalCorrect = 0;
        for (int i = 0; i < svmProblem.l; ++i) {
            if (target[i] == svmProblem.y[i]) {
                ++totalCorrect;
            }
        }
        double accuracy = (double) totalCorrect / (double) svmProblem.l;
        synchronized (cvParamsList) {
            cvParamsList.add(new CvParams(svmParam.gamma, svmParam.C, accuracy));
        }
    }
}
