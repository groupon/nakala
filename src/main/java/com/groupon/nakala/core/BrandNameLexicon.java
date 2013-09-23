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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

/**
 * @author npendar@groupon.com
 */
public class BrandNameLexicon {
    private FinderIndex word2phrase;

    private BrandNameLexicon() {
    }

    public static BrandNameLexicon getInstance() throws IOException {
        BrandNameLexicon b = new BrandNameLexicon();
        b.word2phrase = GeneralFinder.makeIndex("/nakala/brand_names.txt");
        return b;
    }

    public boolean isBrandName(String s) {
        return GeneralFinder.isFound(s, word2phrase);
    }

    public Set<String> getBrandNames(String s) {
        return GeneralFinder.getExtractions(s, word2phrase);
    }

    public List<Span> findBrandNames(String s) {
        return GeneralFinder.findSpans(s, word2phrase);
    }

    public static void main(String[] args) throws IOException {
        BrandNameLexicon bnl = BrandNameLexicon.getInstance();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line = null;
        while ((line = in.readLine()) != null) {
            System.out.println(StringUtils.join(bnl.getBrandNames(line), ','));
        }
    }
}
