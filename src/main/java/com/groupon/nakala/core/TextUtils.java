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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author npendar@groupon.com
 */
public class TextUtils {
    private static final Pattern WORD = Pattern.compile("\\w+");

    /**
     * Returns the word immediately before a position in text.
     *
     * @param text: Input text
     * @param idx:  Specified position in text
     * @return : The word immediately before idx.
     */
    public static String wordBefore(String text, int idx) {
        if (idx == 0) {
            return "";
        }
        int r = idx - 1;
        for (; r >= 0; --r) {
            if (Character.isLetterOrDigit(text.charAt(r))) {
                ++r;
                break;
            }
        }
        if (r == 0) {
            return "";
        }
        int l = r - 1;
        for (; l >= 0; --l) {
            if (!Character.isLetterOrDigit(text.charAt(l))) {
                ++l;
                break;
            }
        }

        if (l == -1) {
            l = 0;
        }
        return text.substring(l, r);
    }

    /**
     * Return word immediately after a position in text.
     *
     * @param text: The input text.
     * @param idx:  The specified position in text.
     * @return The word immediately after index.
     */
    public static String wordAfter(String text, int idx) {
        if (idx == text.length()) {
            return "";
        }
        int l = idx;
        for (; l < text.length(); ++l) {
            if (Character.isLetterOrDigit(text.charAt(l))) {
                break;
            }
        }
        if (l == text.length() - 1) {
            return "";
        }
        int r = l + 1;
        for (; r < text.length(); ++r) {
            if (!Character.isLetterOrDigit(text.charAt(r))) {
                break;
            }
        }

        if (l == text.length()) {
            return "";
        }

        return text.substring(l, r);
    }

    public static Set<String> getWordSet(String s, TokenizerStream tokenizer) {
        Set<String> words = new HashSet<String>();
        tokenizer.setText(s);
        String word = null;
        while ((word = tokenizer.next()) != null) {
            words.add(word);
        }
        return words;
    }
}
