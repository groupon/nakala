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
import com.groupon.nakala.db.CollectionReader;
import com.groupon.nakala.exceptions.AnalyzerFailureException;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import com.groupon.nakala.normalization.CaseNormalizer;
import com.groupon.nakala.normalization.NumberNormalizer;
import com.groupon.nakala.normalization.StringNormalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author npendar@groupon.com
 */
public final class IdfCollectionAnalyzer extends AbstractCollectionAnalyzer {

    TokenizerStream tokenizer;
    List<StringNormalizer> normalizers;
    StringSet stopwords;

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        super.initialize(params);
        if (params.contains(Constants.TOKENIZER)) {
            tokenizer = (TokenizerStream) params.get(Constants.TOKENIZER);
        } else {
            tokenizer = new RegexpTokenizerStream();
        }

        if (params.contains(Constants.NORMALIZERS)) {
            normalizers = (List<StringNormalizer>) params.get(Constants.NORMALIZERS);
        } else {
            normalizers = new ArrayList<StringNormalizer>(2);
            normalizers.add(new CaseNormalizer());
            normalizers.add(new NumberNormalizer());
        }

        if (params.contains(Constants.STOPWORDS)) {
            stopwords = (StringSet) params.get(Constants.STOPWORDS);
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public AnalysisCollector analyze(CollectionReader as) throws AnalyzerFailureException {
        long D = 0;
        KeyValueMapAnalysisCollector<String, Double> idf = new KeyValueMapAnalysisCollector<String, Double>();
        for (Analyzable a : as) {
            TextContent textContent = (TextContent) a;
            if (passFilter != null && !passFilter.passes(textContent))
                continue;

            if (blockFilter != null && blockFilter.blocks(textContent))
                continue;

            ++D;

            String text = ((TextContent) a).getText();
            for (StringNormalizer normalizer : normalizers)
                text = normalizer.normalize(text);

            Set<String> words = TextUtils.getWordSet(text, tokenizer);
            addWords(words, idf);
        }

        for (Entry<String, Double> e : idf.entrySet()) {
            idf.put(e.getKey(), Math.log(D / e.getValue()));
        }

        return idf;
    }

    private void addWords(Set<String> words, KeyValueMapAnalysisCollector<String, Double> df) {
        for (String w : words) {
            if (stopwords != null && stopwords.contains(w))
                continue;
            Double c = df.get(w);
            if (c == null)
                c = 0d;
            df.put(w, c + 1);
        }
    }
}
