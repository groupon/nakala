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

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author npendar@groupon.com
 */
public class Lexicon {
    public static final String ENLGISH = "english";
    public static final String FRENCH = "french";
    public static final String SPANISH = "spanish";

    private static final Pattern WORD = Pattern.compile("\\w+");
    private HashSet<String> lexicon;

    private Lexicon() {
    }

    public static Lexicon newInstance(String lang) throws Exception {
        Lexicon lex = new Lexicon();
        lex.lexicon = new HashSet<String>();

        if (!(lang.equals("english") || lang.equals("french") || lang.equals("spanish"))) {
            throw new IllegalArgumentException("Unknown language '" + lang + "'.");
        }

        for (String s : IoUtil.readLines(IoUtil.read(Lexicon.class, "/nakala/" + lang + "_words.txt"))) {
            lex.lexicon.add(s);
        }

        return lex;
    }

    public boolean isInLexicon(String w) {
        if (w == null || w.isEmpty())
            return false;
        return lexicon.contains(w.toLowerCase());
    }

    public double percentWordsInLexicon(String txt) {
        if (txt == null || txt.isEmpty()) {
            return 0;
        }

        int n = 0;
        int c = 0;
        Matcher m = WORD.matcher(txt);

        while (m.find()) {
            ++n;
            String w = txt.substring(m.start(), m.end()).toLowerCase();
            if (isInLexicon(w)) {
                ++c;
            }
        }

        return n == 0 ? 0 : (double) c / n;
    }
}
