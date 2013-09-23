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

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author npendar@groupon.com
 */
public final class PosTaggerWrapper {
    private POSTaggerME tagger;

    private PosTaggerWrapper() {
    }

    ;

    public static PosTaggerWrapper getInstance() throws IOException {
        PosTaggerWrapper t = new PosTaggerWrapper();
        String sep = File.separator;
        String taggerModelFile = sep + "nakala" + sep + "opennlp_models" + sep + "en-pos-maxent.bin";
        t.tagger = new POSTaggerME(new POSModel(t.getClass().getResourceAsStream(taggerModelFile)));
        return t;
    }

    public POSTaggerME getTagger() {
        return tagger;
    }

    public List<String> tag(List<String> words) {
        return words == null ? null : tagger.tag(words);
    }

    public String[] tag(String[] words) {
        return words == null ? null : tagger.tag(words);
    }

    public String tag(String sent) {
        return sent == null ? null : tagger.tag(sent);
    }

    public String[][] tag(int numTaggings, String[] words) {
        return words == null ? null : tagger.tag(numTaggings, words);
    }

    public Tokens tag(Tokens tokens) {
        if (tokens == null)
            return null;
        String[] tags = tagger.tag(tokens.asStringArray());
        for (int i = 0; i < tags.length; ++i) {
            tokens.get(i).setPosTag(tags[i]);
        }
        return tokens;
    }

    public static void main(String[] args) throws IOException {
        PosTaggerWrapper t = PosTaggerWrapper.getInstance();
        String[] s = {"This", "is", "a", "test", "."};
        String[] tags = t.tag(s);
        for (int i = 0; i < s.length; i++) {
            System.out.print(s[i] + "/" + tags[i] + " ");
        }
        System.out.println();
    }
}
