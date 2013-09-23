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

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author npendar@groupon.com
 */
public final class ChunkerWrapper {
    private ChunkerME chunker;

    private ChunkerWrapper() {
    }

    public static ChunkerWrapper getInstance() throws IOException {
        ChunkerWrapper cw = new ChunkerWrapper();
        String sep = File.separator;
        String chunkerModelFile = sep + "nakala" + sep + "opennlp_models" + sep
                + "en-chunker.bin";
        cw.chunker = new ChunkerME(new ChunkerModel(cw.getClass()
                .getResourceAsStream(chunkerModelFile)));
        return cw;
    }

    public ChunkerME getChunker() {
        return chunker;
    }

    public String[] chunk(String[] words, String[] tags) {
        if (words == null || tags == null)
            return null;
        return chunker.chunk(words, tags);
    }

    public List<String> chunk(List<String> words, List<String> tags) {
        if (words == null || tags == null)
            return null;
        return chunker.chunk(words, tags);
    }

    public Tokens chunk(Tokens tokens) {
        if (tokens == null)
            return null;

        String[] chunks = chunker.chunk(tokens.asStringArray(), tokens.getTags());

        int start = -1;
        String chunkTag = null;
        for (int i = 0; i < chunks.length; ++i) {
            if (chunks[i].charAt(0) == 'B') {
                // found a new boundary
                if (start > -1) {
                    // just finished walking a chunk, add it
                    tokens.assignToSpan(chunkTag, start, i);
                }
                start = i;
                chunkTag = chunks[i].substring(2);
            } else if (chunks[i].equals("O")) {
                // entered non-chunked zone
                if (start > -1) {
                    // just finished walking a chunk, add it
                    tokens.assignToSpan(chunkTag, start, i);
                }
                start = -1;
                chunkTag = null;
            }
        }
        if (start > -1) {
            tokens.assignToSpan(chunkTag, start, chunks.length);
        }

        return tokens;
    }

    public static void main(String[] args) throws IOException {
        ChunkerWrapper c = ChunkerWrapper.getInstance();
        PosTaggerWrapper t = PosTaggerWrapper.getInstance();
        String[] sent = {"My", "program", "will", "conquer", "the", "entire",
                "world", "!"};
        String[] tags = t.tag(sent);
        String[] chunks = c.chunk(sent, tags);
        for (int i = 0; i < sent.length; i++) {
            System.out.print(sent[i] + "/" + tags[i] + "/" + chunks[i] + " ");
        }
        System.out.println();

        String newSent = "The city of Palo Alto has been witnessing a considerable rise in the temperature lately.";
        TokenizerWrapper tokenizer = TokenizerWrapper.getInstance();
        Tokens toks = tokenizer.getTokens(newSent);
        t.tag(toks);
        c.chunk(toks);
        System.out.println(toks);
    }
}
