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

import com.groupon.nakala.analysis.Initializable;
import com.groupon.nakala.core.Constants;
import com.groupon.nakala.core.Parameters;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import com.groupon.util.io.IoUtil;
import libsvm.svm_node;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * @author npendar@groupon.com
 */

public class ValueScaler implements Initializable {

    private static final Logger logger = Logger.getLogger(ValueScaler.class);

    svm_node[][] scaledDataSet;
    private Map<Integer, Range> ranges;
    private double targetRange;
    private double targetMin;
    private double targetMax;

    public ValueScaler() {

    }

    public ValueScaler(double targetMin, double targetMax, svm_node[][] dataSet) {
        this.targetMin = targetMin;
        this.targetMax = targetMax;
        this.targetRange = this.targetMax - this.targetMin;
        ranges = new HashMap<Integer, Range>();
        for (int i = 0; i < dataSet.length; ++i) {
            for (int j = 0; j < dataSet[i].length; ++j) {
                Range range = ranges.get(dataSet[i][j].index);
                if (range == null) {
                    ranges.put(dataSet[i][j].index, new Range(dataSet[i][j].value, dataSet[i][j].value));
                } else {
                    if (range.getMax() < dataSet[i][j].value) {
                        range.setMax(dataSet[i][j].value);
                    } else if (range.getMin() > dataSet[i][j].value) {
                        range.setMin(dataSet[i][j].value);
                    }
                }
            }
        }

        scaledDataSet = new svm_node[dataSet.length][];
        for (int i = 0; i < dataSet.length; ++i) {
            scaledDataSet[i] = new svm_node[dataSet[i].length];
            for (int j = 0; j < dataSet[i].length; ++j) {
                scaledDataSet[i][j] = new svm_node();
                scaledDataSet[i][j].index = dataSet[i][j].index;
                scaledDataSet[i][j].value = getScaledValue(dataSet[i][j].index, dataSet[i][j].value);
            }
        }
    }

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        if (params.contains(Constants.FILE_NAME)) {
            load(params.getString(Constants.FILE_NAME));
        } else if (params.contains(Constants.RESOURCE)) {
            load(getClass(), params.getString(Constants.RESOURCE));
        } else if (params.contains(Constants.RESOURCE_STREAM)) {
            Object streamObj = params.get(Constants.RESOURCE_STREAM);
            if (!(streamObj instanceof InputStream)) {
                throw new ResourceInitializationException("Expected RESOURCE_STREAM to be instance of InputStream");
            }
            _load(new InputStreamReader((InputStream) streamObj), null);
        } else {
            throw new ResourceInitializationException("No file or resource name specified.");
        }
    }

    @Override
    public void shutdown() {

    }

    public svm_node[][] getScaledData() {
        return scaledDataSet;
    }

    public void save(OutputStream outputStream) throws IOException {
        PrintStream out = new PrintStream(outputStream);
        out.println("x");
        out.println(targetMin + " " + targetMax);

        List<Integer> indeces = new ArrayList<Integer>(ranges.keySet());
        Collections.sort(indeces);

        for (int i = 0; i < indeces.size(); ++i) {
            Integer index = indeces.get(i);
            Range range = ranges.get(index);
            // Ignore zero ranges
            if (range.getMin() == range.getMax()) {
                continue;
            }
            out.println(index + " " + range.getMin() + " " + range.getMax());
        }
    }

    public void load(Class cls, String resource) {
        try {
            _load(IoUtil.read(cls, resource), resource);
        } catch (IOException e) {
            throw new ResourceInitializationException("Failed to read " + resource, e);
        }
    }

    public void load(String fileName) throws ResourceInitializationException {
        try {
            _load(IoUtil.read(fileName), fileName);
        } catch (IOException e) {
            throw new ResourceInitializationException("Failed to read " + fileName, e);
        }
    }

    public void _load(Reader reader, String fileName) {
        ranges = new HashMap<Integer, Range>();
        int lineNum = 0;
        try {
            for (String line : IoUtil.readLines(reader)) {
                if (++lineNum == 1) {
                    continue;
                }

                if (lineNum == 2) {
                    String[] minmax = line.trim().split(" ");
                    targetMin = Double.parseDouble(minmax[0]);
                    targetMax = Double.parseDouble(minmax[1]);
                    targetRange = targetMax - targetMin;
                    continue;
                }

                String[] triple = line.trim().split(" ");
                int index = Integer.parseInt(triple[0]) - 1;
                double min = Double.parseDouble(triple[1]);
                double max = Double.parseDouble(triple[2]);
                ranges.put(index, new Range(min, max));
            }
        } catch (Exception e) {
            throw new ResourceInitializationException("Failed to load range file " + fileName);
        }
    }

    public double getScaledValue(int index, double value) {
        Range range = ranges.get(index);
        if (range == null || (range.getMin() == range.getMax())) {
            return targetMax; // feature is present and has only one value
        }
        return ((value - range.getMin()) / range.getRange() * targetRange) + targetMin;
    }

    static class Range {
        double min, max, range;

        Range(double min, double max) {
            this.min = min;
            this.max = max;
            this.range = max - min;
        }

        public void setMin(double min) {
            this.min = min;
            range = max - this.min;
        }

        public void setMax(double max) {
            this.max = max;
            range = this.max - min;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        public double getRange() {
            return range;
        }
    }
}

