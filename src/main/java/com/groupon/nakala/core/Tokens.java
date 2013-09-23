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

import opennlp.tools.util.Span;
import org.apache.commons.lang3.StringUtils;

/**
 * @author npendar@groupon.com
 */
public final class Tokens {

    private Token[] toks;
    private String[] sa;
    private String[] tags;

    public Tokens(String text, Span[] spans) {
        toks = new Token[spans.length];
        for (int i = 0; i < toks.length; ++i) {
            Span s = spans[i];
            String t = text.substring(s.getStart(), s.getEnd());
            toks[i] = new Token(t, s);
        }
    }

    public void assignToSpan(String chunkTag, int startToken, int endToken) {
        int start = toks[startToken].getStart();
        int end = toks[endToken - 1].getEnd();
        Span s = new Span(start, end);
        Span t = new Span(startToken, endToken);
        for (int i = startToken; i < endToken; ++i) {
            toks[i].setChunk(chunkTag, s, t);
        }
    }

    @Override
    public String toString() {
        return StringUtils.join(toks, '\n');
    }

    public Token get(int i) {
        return toks[i];
    }

    public int size() {
        return toks.length;
    }

    public String[] asStringArray() {
        if (sa == null) {
            sa = new String[toks.length];
            for (int i = 0; i < sa.length; ++i) {
                sa[i] = toks[i].getText();
            }
        }
        return sa;
    }

    public String[] getTags() {
        if (tags == null) {
            tags = new String[toks.length];
            for (int i = 0; i < sa.length; ++i) {
                tags[i] = toks[i].getPosTag();
            }
        }
        return tags;
    }
}
