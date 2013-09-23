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

package com.groupon.nakala.core;

import com.groupon.ml.svm.ValueScaler;
import com.groupon.util.io.IoUtil;
import junit.framework.Assert;
import junit.framework.TestCase;
import libsvm.svm_node;
import org.junit.Test;

import java.io.*;


/**
 * @author npendar@groupon.com
 */
public class SparseRepresentationTest extends TestCase {
    double[][] vals = new double[][]{{1, 2}, {5, 3}, {6, 9}, {10, 0.5}};
    String featureText = "this\t0.1\n" +
            "is\t0.2\n" +
            "a\t0.3\n" +
            "test\t1.5\n";

    String sparseRep = "2:2.0 6:3.0 7:9.0 11:0.5";

    SparseRepresentation makeRep() {
        SparseRepresentation rep = new SparseRepresentation(20);
        for (double[] pair : vals) {
            rep.setEntry((int) pair[0], pair[1]);
        }
        return rep;
    }

    @Test
    public void testFromSparseFormat() throws Exception {
        double[][] vals = new double[][]{{1, 2}, {5, 3}, {6, 9}, {10, 0.5}};
        String sparseRep = "2:2.0 6:3.0 7:9.0 11:0.5";
        SparseRepresentation rep = SparseRepresentation.fromSparseFormat(11, sparseRep);
        Assert.assertEquals(vals.length, rep.getNonZeroSize());
        for (int i = 0; i < vals.length; ++i) {
            Assert.assertEquals(vals[i][1], rep.getEntry((int) vals[i][0]));
        }
    }

    @Test
    public void testSerialization() throws Exception {
        String sparseRep = "2:2.0 6:3.0 7:9.0 11:0.5";
        SparseRepresentation rep = SparseRepresentation.fromSparseFormat(11, sparseRep);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(rep);
        objectOutputStream.close();

        byte[] bytes = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        SparseRepresentation rep1 = (SparseRepresentation) objectInputStream.readObject();

        Assert.assertEquals(rep, rep1);
    }

    @Test
    public void testMapMultiply() throws Exception {
        SparseRepresentation rep = makeRep();
        double multiplier = 2d;
        rep = rep.mapMultiply(multiplier);
        for (double[] pair : vals) {
            assertEquals(pair[1] * multiplier, rep.getEntry((int) pair[0]));
        }
    }

    @Test
    public void testMultiplyByFeatureWeights() throws Exception {
        SparseRepresentation rep = makeRep();
        Features features = new Features();
        features.initialize(new StringReader(featureText));
        rep = rep.multiplyByFeatureWeights(features);
        for (double[] pair : vals) {
            int index = (int) pair[0];
            double val = pair[1];
            assertEquals(val * features.getWeight(index), rep.getEntry(index));
        }
    }

    @Test
    public void testScale() throws Exception {
        ValueScaler scaler = new ValueScaler();


        // Trivial identity case
        String rangeContents = "x\n" +
                "0 1\n" +
                "2 0 10\n" +
                "6 0 10\n" +
                "7 0 10\n" +
                "11 0 10\n";
        scaler.load(IoUtil.createTempFile(rangeContents));
        SparseRepresentation rep = makeRep();
        rep = rep.scale(scaler);

        for (double[] pair : vals) {
            int index = (int) pair[0];
            double val = pair[1];
            assertEquals(val / 10, rep.getEntry(index));
        }
    }

    @Test
    public void testGetNonZeroSize() throws Exception {
        SparseRepresentation rep = makeRep();
        assertEquals(vals.length, rep.getNonZeroSize());
    }

    @Test
    public void testToSvmNodes() throws Exception {
        SparseRepresentation rep = makeRep();
        svm_node[] nodes = rep.toSvmNodes();
        for (int i = 0; i < vals.length; ++i) {
            assertEquals((int) vals[i][0], nodes[i].index - 1); // lib_svm assumes 1-based indeces
            assertEquals(vals[i][1], nodes[i].value);
        }
    }

    @Test
    public void testToSparseFormat() throws Exception {
        SparseRepresentation rep = makeRep();
        assertEquals(sparseRep, rep.toSparseFormat().trim());
    }
}
