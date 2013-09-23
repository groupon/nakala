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

package com.groupon.nakala.normalization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author npendar@groupon.com
 */
public class MarkupRemover implements StringNormalizer {
    private static final Pattern[] PATS = {
            Pattern.compile("<!DOCTYPE.+?>", Pattern.DOTALL),
            Pattern.compile("<!--.+?-->", Pattern.DOTALL),
//            Pattern.compile("<head.*?>.*?</head>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script.*?>.*?</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
//            Pattern.compile("<title.*?>.*?</title>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
//            Pattern.compile("<description.*?>.*?</description>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
//            Pattern.compile("<keywords.*?>.*?</keywords>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
            Pattern.compile("<meta.*?>.*?</meta>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
            Pattern.compile("<form.*?>.*?</form>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
            Pattern.compile("<input.*?>.*?</input>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
            Pattern.compile("<button.*?>.*?</button>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
            Pattern.compile("<.*?>")
    };

    @Override
    public String normalize(String s) {
        if (s == null)
            return null;

        for (Pattern pat : PATS) {
            Matcher matcher = pat.matcher(s);
            if (matcher.find())
                s = matcher.replaceAll(" ");
        }
        return s;
    }
}
