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

package com.groupon.nakala.analysis;

import com.groupon.nakala.core.Id;
import com.groupon.nakala.core.IdentifiableTextContent;
import com.groupon.nakala.core.Parameters;
import com.groupon.nakala.core.RegexpTokenizerStream;
import com.groupon.nakala.normalization.CaseNormalizer;
import com.groupon.nakala.normalization.NumberNormalizer;
import com.groupon.nakala.normalization.StringNormalizer;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author npendar@groupon.com
 */
public class FeatureCounterAnalyzerTest extends TestCase {
    @Test
    public void testAnalyze() throws Exception {
        String line = "This is sentence 1. this is sentence 2. and sentence 3 is here.";
        List<StringNormalizer> normalizers = new ArrayList<StringNormalizer>(1);
        normalizers.add(new NumberNormalizer());
        normalizers.add(new CaseNormalizer());

        Parameters params = new Parameters();
        params.set("tokenizer", new RegexpTokenizerStream());
        params.set("normalizers", normalizers);

        FeatureCounterAnalyzer featureCounter = new FeatureCounterAnalyzer();
        featureCounter.initialize(params);

        Analysis analysis = featureCounter.analyze(new IdentifiableTextContent(new Id(1), line));
        assertTrue(analysis instanceof StringCountAnalysis);
        StringCountAnalysis sca = (StringCountAnalysis) analysis;
        assertEquals(3, (int) sca.get("__num__"));
        assertEquals(3, (int) sca.get("is"));
        assertEquals(3, (int) sca.get("sentence"));
        assertEquals(1, (int) sca.get("here"));
        assertEquals(2, (int) sca.get("this"));
        assertEquals(1, (int) sca.get("and"));
        assertEquals(6, sca.size());

        analysis = featureCounter.analyze(new IdentifiableTextContent(new Id(2), "My desk is clean."));
        sca = (StringCountAnalysis) analysis;
        assertEquals(1, (int) sca.get("my"));
        assertEquals(1, (int) sca.get("desk"));
        assertEquals(1, (int) sca.get("is"));
        assertEquals(1, (int) sca.get("clean"));
        assertEquals(4, sca.size());
    }
}
