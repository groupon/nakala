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

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

import java.io.IOException;

/**
 * @author npendar@groupon.com
 */
public final class SentenceBreakerWrapper {
    private SentenceDetectorME sdetector;

    private SentenceBreakerWrapper() {
    }

    public static SentenceBreakerWrapper getInstance() throws InvalidFormatException, IOException {
        SentenceBreakerWrapper sb = new SentenceBreakerWrapper();
        String modelFile = "/nakala/opennlp_models/en-sent.bin";
        SentenceModel model = new SentenceModel(sb.getClass().getResourceAsStream(modelFile));
        sb.sdetector = new SentenceDetectorME(model);
        return sb;
    }

    public SentenceDetectorME getSentDetector() {
        return sdetector;
    }

    public Span[] getSentSpans(String s) {
        return s == null ? null : sdetector.sentPosDetect(s);
    }

    public String[] getSents(String s) {
        return s == null ? null : sdetector.sentDetect(s);
    }

    public static void main(String[] args) throws InvalidFormatException, IOException {
        SentenceBreakerWrapper sb = SentenceBreakerWrapper.getInstance();
        String para = "I could not help laughing at the ease with which he explained his " +
                "process of deduction. \"When I hear you give your reasons,\" I " +
                "remarked, \"the thing always appears to me to be so ridiculously " +
                "simple that I could easily do it myself, though at each " +
                "successive instance of your reasoning I am baffled until you " +
                "explain your process. And yet I believe that my eyes are as good " +
                "as yours.";
        String[] sents = sb.getSents(para);

        for (String s : sents) {
            System.out.println(s);
        }
    }
}
