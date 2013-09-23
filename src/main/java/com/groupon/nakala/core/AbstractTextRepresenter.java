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
import com.groupon.nakala.normalization.StringNormalizer;
import com.groupon.util.collections.CollectionUtil;

import java.util.Collection;
import java.util.List;

/**
 * @author npendar@groupon.com
 */
public abstract class AbstractTextRepresenter implements TextRepresenter {
    protected TokenizerStream tokenizer;
    protected Features features;
    protected List<StringNormalizer> normalizers;
    protected ValueScaler scaler;

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        Collection<String> unset = params.ensureSet(Constants.FEATURES, Constants.TOKENIZER, Constants.NORMALIZERS);
        if (!unset.isEmpty()) {
            throw new ResourceInitializationException("Unspecified parameters: " + CollectionUtil.join(", ", unset));
        }
        tokenizer = (TokenizerStream) params.get(Constants.TOKENIZER);
        features = (Features) params.get(Constants.FEATURES);
        normalizers = (List<StringNormalizer>) params.get(Constants.NORMALIZERS);

        if (params.contains(Constants.SCALER)) {
            scaler = (ValueScaler) params.get(Constants.SCALER);
        }
    }

    @Override
    public void shutdown() {

    }
}
