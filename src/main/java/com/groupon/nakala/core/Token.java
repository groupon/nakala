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

/**
 * @author npendar@groupon.com
 */
public final class Token {

    private String text;
    private Span span;
    private Span chunkSpan;                // chunk span as char offsets
    private Span chunkSpanToken;           // chunk span as token offsets
    private String posTag;
    private String chunkTag;

    public Token(String text, Span span) {
        this.text = text;
        this.span = span;
        this.chunkSpan = null;
        this.posTag = null;
    }

    public void setChunk(String chunkTag, Span span, Span spanToken) {
        this.chunkTag = chunkTag;
        this.chunkSpan = span;
        this.chunkSpanToken = spanToken;
    }

    public String getChunkTag() {
        return chunkTag;
    }

    public Span getChunkSpan() {
        return chunkSpan;
    }

    public Span getChunkSpanAsTokenOffsets() {
        return chunkSpanToken;
    }

    public void setPosTag(String tag) {
        posTag = tag;
    }

    public String getPosTag() {
        return posTag;
    }

    public String getText() {
        return text;
    }

    public Span getSpan() {
        return span;
    }

    public int getStart() {
        return span.getStart();
    }

    public int getEnd() {
        return span.getEnd();
    }

    @Override
    public String toString() {
        return "Token [chunkSpan=" + chunkSpan + ", chunkTag=" + chunkTag
                + ", posTag=" + posTag + ", span=" + span + ", text=" + text
                + "]";
    }
}
