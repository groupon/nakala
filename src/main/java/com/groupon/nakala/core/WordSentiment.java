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

import com.groupon.util.io.IoUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * @author npendar@groupon.com
 */
public class WordSentiment {
    private HashSet<String> pos, neg;

    private WordSentiment() {
    }

    public static WordSentiment getInstance() throws IOException {
        WordSentiment ws = new WordSentiment();
        ws.loadVocab();
        return ws;
    }

    private void loadVocab() throws RuntimeException, IOException {
        BufferedReader in = new BufferedReader(IoUtil.read(WordSentiment.class, "/nakala/sentiment/base_sentiment.txt"));
        pos = new HashSet<String>();
        neg = new HashSet<String>();

        int polarity = 0;
        int lineNum = 0;
        String line = null;
        while ((line = in.readLine()) != null) {
            lineNum++;
            int ci = line.indexOf('#');
            if (ci >= 0)
                line = line.substring(0, ci);
            if (line.isEmpty())
                continue;
            if (Character.isWhitespace(line.charAt(0))) {
                if (polarity == 0)
                    throw new RuntimeException("Vocabulary item '" + line.trim() + "' out of scope in line " + lineNum + ".");
                else if (polarity > 0)
                    addItem(line, pos);
                else
                    addItem(line, neg);
            } else if (line.toUpperCase().equals("POSITIVE")) {
                polarity = 1;
            } else if (line.toUpperCase().equals("NEGATIVE")) {
                polarity = -1;
            } else {
                throw new RuntimeException("Unindented vocabulary item.");
            }
        }
    }

    private void addItem(String line, HashSet<String> ss) {
        for (String s : line.trim().toLowerCase().split(",\\s*")) {
            ss.add(s);
        }
    }

    public int polarity(String s) {
        int p = 0;
        if (pos.contains(s))
            p = 1;
        else if (neg.contains(s))
            p = -1;
        return p;
    }
}
