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

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

import java.io.File;
import java.io.IOException;

/**
 * @author npendar@groupon.com
 */
public final class TokenizerWrapper {
    private TokenizerME tokenizer;

    private TokenizerWrapper() {
    }

    public static TokenizerWrapper getInstance() throws InvalidFormatException, IOException {
        TokenizerWrapper tw = new TokenizerWrapper();
        String sep = File.separator;
        String tokenizerModelFile = sep + "nakala" + sep + "opennlp_models" + sep
                + "en-token.bin";
        tw.tokenizer = new TokenizerME(new TokenizerModel(tw.getClass()
                .getResourceAsStream(tokenizerModelFile)));
        return tw;
    }


    public TokenizerME getTokenizer() {
        return tokenizer;
    }

    public Span[] tokenize(String s) {
        return tokenizer.tokenizePos(s);
    }

    public Tokens getTokens(String s) {
        return new Tokens(s, tokenize(s));
    }

    public static void main(String[] args) throws InvalidFormatException, IOException {
        TokenizerWrapper t = TokenizerWrapper.getInstance();
        String s = "This, my friend, is a pretty good sentence.";
        Span[] spans = t.tokenize(s);
        System.out.println(s);
        for (int i = 0; i < spans.length; i++) {
            System.out.println(spans[i]);
        }
    }
}
