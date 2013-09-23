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

import com.groupon.nakala.normalization.CaseNormalizer;
import com.groupon.nakala.normalization.StringNormalizer;
import junit.framework.TestCase;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

/**
 * @author npendar@groupon.com
 */
public class FeatureWeightTextRepresenterTest extends TestCase {
    @Test
    public void testRepresent() throws Exception {
        String text = "This tests for every possible calamity in the world!! Tests are good...";
        Features features = new Features();
        features.initialize(new StringReader("television\t5.0\ntests\t1.0\npossible\t2.0\ncalamity\t3.0\nworld\t1.5\n"));

        TokenizerStream tokenizer = new RegexpTokenizerStream();
        List<StringNormalizer> normalizers = new LinkedList<StringNormalizer>();
        normalizers.add(new CaseNormalizer());

        Parameters params = new Parameters();
        params.set("tokenizer", tokenizer);
        params.set("features", features);
        params.set("normalizers", normalizers);

        TextRepresenter representer = new FeatureWeightTextRepresenter();
        representer.initialize(params);
        RealVector rep = representer.represent(text);

        System.out.println(features);
        for (int i = 0; i < rep.getDimension(); ++i)
            System.out.print(rep.getEntry(i) + " ");
        System.out.println();

        assertEquals(5, rep.getDimension());
        assertEquals(0d, rep.getEntry(0));
        assertEquals(1d, rep.getEntry(1));
        assertEquals(2d, rep.getEntry(2));
        assertEquals(3d, rep.getEntry(3));
        assertEquals(1.5, rep.getEntry(4));
    }
}
