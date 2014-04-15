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
import com.groupon.nakala.exceptions.ResourceInitializationException;
import libsvm.svm_node;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.*;


/**
 * @author npendar@groupon.com
 */
public class SparseRepresentation extends OpenMapRealVector {
    public static SparseRepresentation fromSparseFormat(int size, String s) throws ResourceInitializationException {
        SparseRepresentation rep = new SparseRepresentation(size);
        try {
            for (String pair : s.trim().split(" ")) {
                int indexOfColon = pair.indexOf(":");
                int index = Integer.parseInt(pair.substring(0, indexOfColon)) - 1;
                double value = Double.parseDouble(pair.substring(indexOfColon + 1));
                rep.setEntry(index, value);
            }
        } catch (Exception e) {
            throw new ResourceInitializationException("Could not parse sparse format.", e);
        }
        return rep;
    }

    public SparseRepresentation() {
        super();
    }

    public SparseRepresentation(int size) {
        super(size);
    }

    public SparseRepresentation(RealVector vector) {
        super(vector);
    }

    public SparseRepresentation mapMultiply(double d) {
        return new SparseRepresentation(super.mapMultiply(d));
    }

    public SparseRepresentation multiplyByFeatureWeights(Features features) {
        SparseRepresentation rep = new SparseRepresentation(getDimension());
        Iterator<Entry> iterator = sparseIterator();
        while (iterator.hasNext()) {
            Entry e = iterator.next();
            int index = e.getIndex();
            double val = e.getValue();
            rep.setEntry(index, val * features.getWeight(index));
        }
        return rep;
    }

    public SparseRepresentation scale(ValueScaler scaler) {
        SparseRepresentation rep = new SparseRepresentation(getDimension());
        for (int i = 0; i < getDimension(); ++i) {
            double unscaled = getEntry(i);
            if (unscaled != 0) rep.setEntry(i, scaler.getScaledValue(i, getEntry(i)));
        }
        return rep;
    }

    /**
     * @return the number of non-zero values
     */

    public int getNonZeroSize() {
        int count = 0;
        Iterator<Entry> iterator = sparseIterator();
        while (iterator.hasNext()) {
            iterator.next();
            ++count;
        }
        return count;
    }

    /**
     * @return if all values are zeros
     */
    public boolean isZeroVector() {
        Iterator<Entry> iterator = sparseIterator();
        if (iterator.hasNext()) {
            return false;
        } else {
            return true;
        }
    }

    public svm_node[] toSvmNodes() {
        List<svm_node> nodes = new ArrayList<svm_node>(getNonZeroSize());
        Iterator<Entry> iterator = sparseIterator();
        while (iterator.hasNext()) {
            Entry e = iterator.next();
            svm_node node = new svm_node();
            node.index = e.getIndex() + 1; // lib_svm assumes 1-based indices
            node.value = e.getValue();
            nodes.add(node);
        }
        Collections.sort(nodes, new SvmNodeComparator());
        return nodes.toArray(new svm_node[0]);
    }

    public String toSparseFormat() {
        List<Integer> keys = new LinkedList<Integer>();
        Iterator<Entry> iterator = sparseIterator();
        while (iterator.hasNext()) {
            keys.add(iterator.next().getIndex());
        }
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (Integer index : keys) {
            sb.append(index + 1).append(':').append(getEntry(index)).append(' ');
        }
        return sb.toString();
    }
}

class SvmNodeComparator implements Comparator<svm_node> {
    @Override
    public int compare(svm_node n1, svm_node n2) {
        return n1.index - n2.index;
    }
}