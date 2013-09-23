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

package com.groupon.util;

import com.groupon.util.collections.CollectionUtil;
import com.groupon.util.io.IoUtil;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

/**
 * @author alasdair@groupon.com
 */
public class AddressSynonyms {
    private Synonyms fSynonyms = new Synonyms().setExpandNumbers(true);
    private Pattern fComma = Pattern.compile("\\s*,\\s*");

    private AddressSynonyms() {
    }

    public static AddressSynonyms open(Reader r) throws IOException {
        AddressSynonyms an = new AddressSynonyms();
        an.getSynonyms().read(r);
        return an;
    }

    public static AddressSynonyms open(String resource) throws IOException {
        return open(IoUtil.read(AddressSynonyms.class, resource));
    }

    public static AddressSynonyms open() {
        try {
            return open("/address-synonyms.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Synonyms getSynonyms() {
        return fSynonyms;
    }

    public String normalize(String input) {
        if (input == null)
            return null;
        String[] bits = fComma.split(input.trim().toLowerCase());
        for (int i = 0; i < bits.length; i++) {
            bits[i] = fSynonyms.replaceSynonyms(bits[i]);
        }
        return CollectionUtil.join(", ", bits);
    }
}
