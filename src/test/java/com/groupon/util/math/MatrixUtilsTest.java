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

package com.groupon.util.math;

import junit.framework.TestCase;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Test;

/**
 * @author npendar@groupon.com
 */
public class MatrixUtilsTest extends TestCase {
    private static final double[][] data = {
            {0.0, 1.0, 2.0},
            {1.0, 1.0, 1.0},
            {1.0, 0.5, 0.0}};
    private static final double[][] normalizedData = {
            {0.0, 0.3333333333333333, 0.6666666666666666},
            {0.3333333333333333, 0.3333333333333333, 0.3333333333333333},
            {0.6666666666666666, 0.3333333333333333, 0.0}};

    @Test
    public void testCosineNormalize() throws Exception {
        RealMatrix matrix = org.apache.commons.math3.linear.MatrixUtils.createRealMatrix(data);
        MatrixUtils.cosineNormalize(matrix);
        assertEquals(org.apache.commons.math3.linear.MatrixUtils.createRealMatrix(normalizedData), matrix);
    }

    @Test
    public void testSumOfRow() throws Exception {
        RealMatrix matrix = org.apache.commons.math3.linear.MatrixUtils.createRealMatrix(data);
        assertEquals(3.0, MatrixUtils.sumOfRow(matrix, 0));
        assertEquals(3.0, MatrixUtils.sumOfRow(matrix, 1));
        assertEquals(1.5, MatrixUtils.sumOfRow(matrix, 2));
    }
}
