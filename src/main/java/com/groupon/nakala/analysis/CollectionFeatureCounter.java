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

import com.groupon.nakala.core.Analyzable;
import com.groupon.nakala.core.Parameters;
import com.groupon.nakala.core.TextContent;
import com.groupon.nakala.db.CollectionReader;
import com.groupon.nakala.exceptions.AnalyzerFailureException;
import com.groupon.nakala.exceptions.ResourceInitializationException;

/**
 * @author npendar@groupon.com
 */
public class CollectionFeatureCounter extends AbstractCollectionAnalyzer {
    private FeatureCounterAnalyzer featureCounterAnalyzer;
    private StringCountsAnalysisCollector counts;

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        super.initialize(params);
        featureCounterAnalyzer = new FeatureCounterAnalyzer();
        featureCounterAnalyzer.initialize(params);
        counts = new StringCountsAnalysisCollector();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public AnalysisCollector analyze(CollectionReader cr) throws AnalyzerFailureException {
        for (Analyzable a : cr) {
            TextContent textContent = (TextContent) a;
            if (passFilter != null && !passFilter.passes(textContent))
                continue;

            if (blockFilter != null && blockFilter.blocks(textContent))
                continue;

            Analysis unitCounts = featureCounterAnalyzer.analyze(a);
            counts.addAnalysis(unitCounts);
        }
        return counts;
    }
}
