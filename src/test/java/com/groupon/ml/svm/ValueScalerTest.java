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

import com.groupon.util.io.IoUtil;
import junit.framework.TestCase;
import libsvm.svm_node;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

/**
 * @author npendar@groupon.com
 */
public class ValueScalerTest extends TestCase {
    @Test
    public void testGetScaledValue() throws Exception {

        ValueScaler scaler = new ValueScaler();


        // Trivial identity case
        String rangeContents = "x\n" +
                "0 1\n" +
                "1 0 1\n" +
                "2 0 1\n" +
                "3 0 1\n";
        scaler.load(IoUtil.createTempFile(rangeContents));

        assertEquals(0d, scaler.getScaledValue(0, 0));
        assertEquals(0d, scaler.getScaledValue(1, 0));
        assertEquals(0d, scaler.getScaledValue(2, 0));

        assertEquals(0.5, scaler.getScaledValue(0, 0.5));
        assertEquals(0.5, scaler.getScaledValue(1, 0.5));
        assertEquals(0.5, scaler.getScaledValue(2, 0.5));

        assertEquals(1d, scaler.getScaledValue(0, 1));
        assertEquals(1d, scaler.getScaledValue(1, 1));
        assertEquals(1d, scaler.getScaledValue(2, 1));

        // Convert from [0,1] to [-1,+1]
        rangeContents = "x\n" +
                "-1 1\n" +
                "1 0 1\n" +
                "2 0 1\n" +
                "3 0 1\n";
        scaler.load(IoUtil.createTempFile(rangeContents));

        assertEquals(-1d, scaler.getScaledValue(0, 0));
        assertEquals(-1d, scaler.getScaledValue(1, 0));
        assertEquals(-1d, scaler.getScaledValue(2, 0));

        assertEquals(-0.5, scaler.getScaledValue(0, 0.25));
        assertEquals(-0.5, scaler.getScaledValue(1, 0.25));
        assertEquals(-0.5, scaler.getScaledValue(2, 0.25));

        assertEquals(0d, scaler.getScaledValue(0, 0.5));
        assertEquals(0d, scaler.getScaledValue(1, 0.5));
        assertEquals(0d, scaler.getScaledValue(2, 0.5));

        assertEquals(0.5, scaler.getScaledValue(0, 0.75));
        assertEquals(0.5, scaler.getScaledValue(1, 0.75));
        assertEquals(0.5, scaler.getScaledValue(2, 0.75));

        assertEquals(1d, scaler.getScaledValue(0, 1));
        assertEquals(1d, scaler.getScaledValue(1, 1));
        assertEquals(1d, scaler.getScaledValue(2, 1));

        // Convert from [-1,1] to [0,1]
        rangeContents = "x\n" +
                "0 1\n" +
                "1 -1 1\n" +
                "2 -1 1\n" +
                "3 -1 1\n";
        scaler.load(IoUtil.createTempFile(rangeContents));

        assertEquals(0d, scaler.getScaledValue(0, -1));
        assertEquals(0d, scaler.getScaledValue(1, -1));
        assertEquals(0d, scaler.getScaledValue(2, -1));

        assertEquals(0.25, scaler.getScaledValue(0, -0.5));
        assertEquals(0.25, scaler.getScaledValue(1, -0.5));
        assertEquals(0.25, scaler.getScaledValue(2, -0.5));

        assertEquals(0.5, scaler.getScaledValue(0, 0d));
        assertEquals(0.5, scaler.getScaledValue(1, 0d));
        assertEquals(0.5, scaler.getScaledValue(2, 0d));

        assertEquals(0.75, scaler.getScaledValue(0, 0.5));
        assertEquals(0.75, scaler.getScaledValue(1, 0.5));
        assertEquals(0.75, scaler.getScaledValue(2, 0.5));

        assertEquals(1d, scaler.getScaledValue(0, 1));
        assertEquals(1d, scaler.getScaledValue(1, 1));
        assertEquals(1d, scaler.getScaledValue(2, 1));

        // Convert from [0,1] to [1,5]
        rangeContents = "x\n" +
                "1 5\n" +
                "1 0 1\n" +
                "2 0 1\n" +
                "3 0 1\n";
        scaler.load(IoUtil.createTempFile(rangeContents));

        assertEquals(1d, scaler.getScaledValue(0, 0d));
        assertEquals(1d, scaler.getScaledValue(1, 0d));
        assertEquals(1d, scaler.getScaledValue(2, 0d));

        assertEquals(2d, scaler.getScaledValue(0, 0.25));
        assertEquals(2d, scaler.getScaledValue(1, 0.25));
        assertEquals(2d, scaler.getScaledValue(2, 0.25));

        assertEquals(3d, scaler.getScaledValue(0, 0.5));
        assertEquals(3d, scaler.getScaledValue(1, 0.5));
        assertEquals(3d, scaler.getScaledValue(2, 0.5));

        assertEquals(4d, scaler.getScaledValue(0, 0.75));
        assertEquals(4d, scaler.getScaledValue(1, 0.75));
        assertEquals(4d, scaler.getScaledValue(2, 0.75));

        assertEquals(5d, scaler.getScaledValue(0, 1));
        assertEquals(5d, scaler.getScaledValue(1, 1));
        assertEquals(5d, scaler.getScaledValue(2, 1));

        // Convert from [1,5] to [0,1]
        rangeContents = "x\n" +
                "0 1\n" +
                "1 1 5\n" +
                "2 1 5\n" +
                "3 1 5\n";
        scaler.load(IoUtil.createTempFile(rangeContents));

        assertEquals(0d, scaler.getScaledValue(0, 1d));
        assertEquals(0d, scaler.getScaledValue(1, 1d));
        assertEquals(0d, scaler.getScaledValue(2, 1d));

        assertEquals(0.25, scaler.getScaledValue(0, 2));
        assertEquals(0.25, scaler.getScaledValue(1, 2));
        assertEquals(0.25, scaler.getScaledValue(2, 2));

        assertEquals(0.5, scaler.getScaledValue(0, 3));
        assertEquals(0.5, scaler.getScaledValue(1, 3));
        assertEquals(0.5, scaler.getScaledValue(2, 3));

        assertEquals(0.75, scaler.getScaledValue(0, 4d));
        assertEquals(0.75, scaler.getScaledValue(1, 4d));
        assertEquals(0.75, scaler.getScaledValue(2, 4d));

        assertEquals(1d, scaler.getScaledValue(0, 5));
        assertEquals(1d, scaler.getScaledValue(1, 5));
        assertEquals(1d, scaler.getScaledValue(2, 5));

        // Convert from [1,5] to [-1,1]
        rangeContents = "x\n" +
                "-1 1\n" +
                "1 1 5\n" +
                "2 1 5\n" +
                "3 1 5\n";
        scaler.load(IoUtil.createTempFile(rangeContents));

        assertEquals(-1d, scaler.getScaledValue(0, 1d));
        assertEquals(-1d, scaler.getScaledValue(1, 1d));
        assertEquals(-1d, scaler.getScaledValue(2, 1d));

        assertEquals(-0.5, scaler.getScaledValue(0, 2));
        assertEquals(-0.5, scaler.getScaledValue(1, 2));
        assertEquals(-0.5, scaler.getScaledValue(2, 2));

        assertEquals(0d, scaler.getScaledValue(0, 3));
        assertEquals(0d, scaler.getScaledValue(1, 3));
        assertEquals(0d, scaler.getScaledValue(2, 3));

        assertEquals(0.5, scaler.getScaledValue(0, 4d));
        assertEquals(0.5, scaler.getScaledValue(1, 4d));
        assertEquals(0.5, scaler.getScaledValue(2, 4d));

        assertEquals(1d, scaler.getScaledValue(0, 5));
        assertEquals(1d, scaler.getScaledValue(1, 5));
        assertEquals(1d, scaler.getScaledValue(2, 5));
    }

    @Test
    public void testDatasetScaling() throws Exception {
        double[][][] data = {
                {{1, 2}, {3, 4}, {5, 6}, {7, 8}},
                {{1, 1}, {3, 6}, {5, 5}},
                {{3, 3}, {5, 7}, {7, 3}, {9, 2}}};

        double[][][] scaled = {
                {{1, 1}, {3, 0.33}, {5, 0.5}, {7, 1}},
                {{1, 0}, {3, 1}, {5, 0}},
                {{3, 0}, {5, 1}, {7, 0}, {9, 1}}};

        String saved = "x\n" +
                "0.0 1.0\n" +
                "1 1.0 2.0\n" +
                "3 3.0 6.0\n" +
                "5 5.0 7.0\n" +
                "7 3.0 8.0\n";

        svm_node[][] dataset = new svm_node[data.length][];
        for (int i = 0; i < data.length; ++i) {
            dataset[i] = new svm_node[data[i].length];
            for (int j = 0; j < data[i].length; ++j) {
                dataset[i][j] = new svm_node();
                dataset[i][j].index = (int) data[i][j][0];
                dataset[i][j].value = data[i][j][1];
            }
        }

        ValueScaler scaler = new ValueScaler(0d, 1d, dataset);
        svm_node[][] scaledData = scaler.getScaledData();
        for (int i = 0; i < scaledData.length; ++i) {
            for (int j = 0; j < scaledData[i].length; ++j) {
                assertEquals((int) scaled[i][j][0], scaledData[i][j].index);
                assertEquals(scaled[i][j][1], scaledData[i][j].value, 0.01);
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaler.save(out);
        String str = out.toString(IoUtil.getDefaultEncoding());
        assertEquals(saved, str);

        scaler = new ValueScaler();
        scaler.load(IoUtil.createTempFile(saved));
        for (int i = 0; i < data.length; ++i) {
            for (int j = 0; j < data[i].length; ++j) {
                assertEquals(scaled[i][j][1], scaler.getScaledValue((int) data[i][j][0] - 1, data[i][j][1]), 0.01);
            }
        }
    }
}
