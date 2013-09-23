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

import com.groupon.nakala.normalization.StringNormalizer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author npendar@groupon.com
 */
public class ShinglesTokenizerStream implements TokenizerStream {
    private int shingleSize = 4;
    private String text;
    private int index;

    public void setShingleSize(int shingleSize) {
        this.shingleSize = shingleSize;
    }

    public int getShingleSize() {
        return shingleSize;
    }

    @Override
    public void setText(String s) {
        text = s;
        index = 0;
    }

    @Override
    public String next() {
        String token = null;
        if (index <= text.length() - shingleSize) {
            token = text.substring(index, index + shingleSize);
            ++index;
        }
        return token;
    }

    @Override
    public Set<String> getUniqueTokens(String s) {
        Set<String> wordTypes = new HashSet<String>();
        setText(s);
        String token = null;
        while ((token = next()) != null) {
            wordTypes.add(token);
        }
        return wordTypes;
    }

    @Override
    public List<String> getTokens(String s) {
        List<String> tokens = new LinkedList<String>();
        setText(s);
        String token = null;
        while ((token = next()) != null) {
            tokens.add(token);
        }
        return tokens;
    }

    @Override
    public Set<String> getUniqueTokens(String s, List<StringNormalizer> normalizers) {
        setText(s);
        Set<String> uniques = new HashSet<String>();
        String tok = null;
        while ((tok = next()) != null) {
            if (normalizers != null) {
                for (StringNormalizer normalizer : normalizers) {
                    tok = normalizer.normalize(tok);
                }
            }
            uniques.add(tok);
        }
        return uniques;
    }

    @Override
    public List<String> getTokens(String s, List<StringNormalizer> normalizers) {
        setText(s);
        List<String> tokens = new LinkedList<String>();
        String tok = null;
        while ((tok = next()) != null) {
            if (normalizers != null) {
                for (StringNormalizer normalizer : normalizers) {
                    tok = normalizer.normalize(tok);
                }
            }
            tokens.add(tok);
        }
        return tokens;
    }
}
