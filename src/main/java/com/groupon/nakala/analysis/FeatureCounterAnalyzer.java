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

import com.groupon.nakala.core.*;
import com.groupon.nakala.exceptions.AnalyzerFailureException;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import com.groupon.nakala.normalization.StringNormalizer;

import java.util.Collection;
import java.util.List;

/**
 * @author npendar@groupon.com
 */
public class FeatureCounterAnalyzer implements Analyzer {
    private StringCountAnalysis counts;
    private TokenizerStream tokenizer;
    private List<StringNormalizer> normalizers;
    private StringSet stopwords;

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        try {
            Collection<String> unset = params.ensureSet(Constants.TOKENIZER);
            if (unset.contains(Constants.TOKENIZER)) {
                throw new ResourceInitializationException("Tokenizer not set.");
            }

            tokenizer = (TokenizerStream) params.get(Constants.TOKENIZER);

            if (params.contains(Constants.NORMALIZERS)) {
                normalizers = (List<StringNormalizer>) params.get(Constants.NORMALIZERS);
            }

            if (params.contains(Constants.STOPWORDS)) {
                stopwords = (StringSet) params.get(Constants.STOPWORDS);
            }
        } catch (ResourceInitializationException e) {
            throw new ResourceInitializationException("CollectionFeatureCounter initialization failed.", e);
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public Analysis analyze(Analyzable a) throws AnalyzerFailureException {
        TextContent textContent = (TextContent) a;
        counts = new StringCountAnalysis();
        tokenizer.setText(textContent.getText());
        String tok = null;
        while ((tok = tokenizer.next()) != null) {
            if (normalizers != null) {
                for (StringNormalizer sn : normalizers) {
                    tok = sn.normalize(tok);
                }
            }
            if (stopwords != null && stopwords.contains(tok)) {
                continue;
            }
            counts.add(tok);
        }
        return counts;
    }
}
