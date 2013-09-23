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

import com.groupon.nakala.core.Id;
import com.groupon.util.collections.CollectionUtil;

import java.util.*;

/**
 * @author npendar@groupon.com
 */

public class ClassificationMetrics {

    private static final int TP_INDEX = 0;
    private static final int FP_INDEX = 1;
    private static final int FN_INDEX = 3;

    private int[][] stats;
    private double threshold;
    private List<String> categories;
    private List<Double> accuracies;

    public ClassificationMetrics(final double threshold,
                                 final List<String> categories,
                                 final Map<Id, Set<String>> id2truecategories,
                                 final ClassificationAnalysisCollector id2predictions) {

        accuracies = new ArrayList<Double>(id2truecategories.size());
        this.categories = categories;
        stats = new int[categories.size()][4];

        for (Map.Entry<Id, Set<String>> e : id2truecategories.entrySet()) {
            Id id = e.getKey();
            Set<String> trueCategories = e.getValue();

            Set<String> predictions = new HashSet<String>();
            for (Map.Entry<String, Double> pred : id2predictions.get(id).getClassifications().entrySet())
                if (pred.getValue() >= threshold)
                    predictions.add(pred.getKey());

            accuracies.add(getItemAccuracy(trueCategories, predictions));

            for (int i = 0; i < categories.size(); ++i) {
                String category = categories.get(i);

                if (trueCategories.contains(category)) {
                    if (predictions.contains(category)) {
                        ++stats[i][TP_INDEX];
                    } else {
                        ++stats[i][FN_INDEX];
                    }
                } else {
                    if (predictions.contains(category)) {
                        ++stats[i][FP_INDEX];
                    }
                }
            }
        }
        this.threshold = threshold;
    }

    private double getItemAccuracy(Set<String> trueLabels, Set<String> predictedLabels) {
        Set<String> union = new HashSet<String>(trueLabels);
        union.addAll(predictedLabels);

        Set<String> intersection = new HashSet<String>(trueLabels);
        intersection.retainAll(predictedLabels);

        return union.size() == 0 ? 0d : ((double) intersection.size()) / ((double) union.size());
    }

    public double getThreshold() {
        return threshold;
    }

    public List<String> getCategories() {
        return categories;
    }

    public int getCategoryIndex(String category) {
        return categories.indexOf(category);
    }

    public int getTp(String category) {
        return stats[getCategoryIndex(category)][TP_INDEX];
    }

    public int getFp(String category) {
        return stats[getCategoryIndex(category)][FP_INDEX];
    }

    public int getFn(String category) {
        return stats[getCategoryIndex(category)][FN_INDEX];
    }

    public double getP(String category) {
        double denum = (double) (getTp(category) + getFp(category));
        return denum > 0d ? (double) getTp(category) / denum : 0d;
    }

    public double getR(String category) {
        double denum = (double) (getTp(category) + getFn(category));
        return denum > 0d ? (double) getTp(category) / denum : 0d;
    }

    public double getF1(String category) {
        double denum = getP(category) + getR(category);
        return denum > 0d ? 2 * (getP(category) * (getR(category)) / denum) : 0d;
    }

    public double getMacroAvgP() {
        double p = 0d;
        for (String cat : getCategories())
            p += getP(cat);
        return getCategories().size() > 0 ? p / getCategories().size() : 0d;
    }

    public double getMacroAvgR() {
        double r = 0d;
        for (String cat : getCategories())
            r += getR(cat);
        return getCategories().size() > 0d ? r / getCategories().size() : 0d;
    }

    public double getMarcoAvgF1() {
        double f = 0d;
        for (String cat : getCategories())
            f += getF1(cat);
        return getCategories().size() > 0d ? f / getCategories().size() : 0d;
    }

    public double getAvgAccuracy() {
        double sum = CollectionUtil.sum(accuracies);
        return accuracies.size() > 0d ? sum / accuracies.size() : 0d;
    }

    public int getTp() {
        int tp = 0;
        for (String cat : getCategories())
            tp += getTp(cat);
        return tp;
    }

    public int getFp() {
        int fp = 0;
        for (String cat : getCategories())
            fp += getFp(cat);
        return fp;
    }

    public int getFn() {
        int fn = 0;
        for (String cat : getCategories())
            fn += getFn(cat);
        return fn;
    }

    public double getMicroAvgP() {
        double denum = (double) (getTp() + getFp());
        return denum > 0d ? (double) getTp() / denum : 0d;
    }

    public double getMicroAvgR() {
        double denum = (double) (getTp() + getFn());
        return denum > 0d ? (double) getTp() / denum : 0d;
    }

    public double getMicroAvgF1() {
        double denum = getMicroAvgP() + getMicroAvgR();
        return denum > 0d ? 2 * (getMicroAvgP() * getMicroAvgR()) / denum : 0d;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String cat : getCategories()) {
            sb.append(cat).append('\n');
            sb.append("  ").append("TP: ").append(getTp(cat)).append('\n');
            sb.append("  ").append("FP: ").append(getFp(cat)).append('\n');
            sb.append("  ").append("FN: ").append(getFn(cat)).append('\n');
            sb.append("  ").append("P: ").append(getP(cat)).append('\n');
            sb.append("  ").append("R: ").append(getR(cat)).append('\n');
            sb.append("  ").append("F1: ").append(getF1(cat)).append('\n').append('\n');
        }
        sb.append("TP: ").append(getTp()).append('\n');
        sb.append("FP: ").append(getFp()).append('\n');
        sb.append("FN: ").append(getFn()).append('\n').append('\n');
        sb.append("Macro avg P: ").append(getMacroAvgP()).append('\n');
        sb.append("Macro avg R: ").append(getMacroAvgR()).append('\n');
        sb.append("Macro avg F1: ").append(getMarcoAvgF1()).append('\n');
        sb.append("Micro avg P: ").append(getMicroAvgP()).append('\n');
        sb.append("Micro avg R: ").append(getMicroAvgR()).append('\n');
        sb.append("Micro avg F1: ").append(getMicroAvgF1()).append('\n');
        sb.append("Avg Acc: ").append(getAvgAccuracy());
        return sb.toString();
    }
}
