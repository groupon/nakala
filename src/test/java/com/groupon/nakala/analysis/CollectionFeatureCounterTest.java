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

import com.groupon.nakala.core.Parameters;
import com.groupon.nakala.core.RegexpTokenizerStream;
import com.groupon.nakala.db.CollectionParameters;
import com.groupon.nakala.db.SimpleTextCollectionReader;
import com.groupon.nakala.normalization.CaseNormalizer;
import com.groupon.nakala.normalization.NumberNormalizer;
import com.groupon.nakala.normalization.StringNormalizer;
import com.groupon.util.io.IoUtil;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author npendar@groupon.com
 */
public class CollectionFeatureCounterTest extends TestCase {

    public void testAnalyze() throws Exception {
        String lines = "This is line 1.\n" + "this is line 2.";
        List<StringNormalizer> normalizers = new ArrayList<StringNormalizer>(1);
        normalizers.add(new NumberNormalizer());
        normalizers.add(new CaseNormalizer());

        Parameters params = new Parameters();
        params.set("tokenizer", new RegexpTokenizerStream());
        params.set("normalizers", normalizers);

        CollectionFeatureCounter fc = new CollectionFeatureCounter();
        fc.initialize(params);

        CollectionParameters inputParams = new CollectionParameters();

        inputParams.set(CollectionParameters.FILE_NAME, IoUtil.createTempFile(lines));

        SimpleTextCollectionReader cr = new SimpleTextCollectionReader();
        cr.initialize(inputParams);

        StringCountsAnalysisCollector sc = (StringCountsAnalysisCollector) fc.analyze(cr);
        Map<String, Integer> counts = sc.getCounts();
        assertTrue(counts.containsKey("this"));
        assertEquals(2, (int) counts.get("this"));

        assertTrue(counts.containsKey("is"));
        assertEquals(2, (int) counts.get("is"));

        assertTrue(counts.containsKey("line"));
        assertEquals(2, (int) counts.get("line"));

        assertTrue(counts.containsKey("__num__"));
        assertEquals(2, (int) counts.get("__num__"));

        assertFalse(counts.containsKey("this is"));
        assertFalse(counts.containsKey("."));
    }
}
