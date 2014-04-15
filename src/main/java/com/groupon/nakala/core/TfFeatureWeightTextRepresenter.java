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

import com.groupon.nakala.normalization.StringNormalizer;

import java.util.List;

/**
 * @author npendar@groupon.com
 */
public class TfFeatureWeightTextRepresenter extends TFTextRepresenter {

    @Override
    public SparseRepresentation represent(String text) {
        if (normalizers != null) {
            for (StringNormalizer sn : normalizers) {
                text = sn.normalize(text);
            }
        }

        SparseRepresentation rep = new SparseRepresentation(features.size());
        tokenizer.setText(text);
        int textSize = 0;
        String tok;
        while ((tok = tokenizer.next()) != null) {
            ++textSize;
            int index = features.getIndex(tok);
            if (index == -1) { continue; }
            rep.addToEntry(index, 1);
        }

        // Normalize tfs by number of tokens
        if (normalizeByLength && textSize > 0) {
            rep = rep.mapMultiply(1d / textSize);
        }

        rep = rep.multiplyByFeatureWeights(features);

        // Scale
        if (scaler != null) { rep = rep.scale(scaler); }

        return rep;
    }

    @Override
    public TokenizerStream getTokenizer() {
        return tokenizer;
    }

    @Override
    public List<StringNormalizer> getNormalizers() {
        return normalizers;
    }

    @Override
    public Features getFeatures() {
        return features;
    }
}
