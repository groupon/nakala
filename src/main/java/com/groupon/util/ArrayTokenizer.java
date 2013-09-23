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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author alasdair@groupon.com
 */
public class ArrayTokenizer implements Iterator<String> {
    private String fSep;
    private String fString;
    private int fiStart;
    private int fiToken;
    private boolean fbTrim;

    public ArrayTokenizer(String s) {
        this(s, "#||#");
    }

    public ArrayTokenizer(String s, String sep) {
        fString = s;
        fSep = sep;
        fiToken = -sep.length();
        if (s != null)
            advance();
    }

    public void setTrim(boolean b) {
        fbTrim = b;
    }

    public boolean getTrim() {
        return fbTrim;
    }

    private void advance() {
        fiStart = fiToken + fSep.length();
        for (; fiStart < fString.length(); fiStart = fiToken + fSep.length()) {
            fiToken = fString.indexOf(fSep, fiStart);
            if (fiToken < 0)
                fiToken = fString.length();
            int i = fiStart;
            for (; i < fiToken; i++)
                if (!Character.isWhitespace(fString.charAt(i))) break;
            if (i < fiToken)
                return;
        }
    }

    public String[] toArray() {
        if (fString == null)
            return null;
        List<String> r = new LinkedList<String>();
        ArrayTokenizer at = new ArrayTokenizer(fString, fSep);
        at.setTrim(getTrim());
        for (; at.hasNext(); ) {
            r.add(at.next());
        }
        return r.toArray(new String[r.size()]);
    }

    @Override
    public boolean hasNext() {
        return fString != null && fiStart < fString.length();
    }

    @Override
    public String next() {
        String r;
        if (fiToken < 0) {
            r = fString.substring(fiStart);
        } else {
            r = fString.substring(fiStart, fiToken);
        }
        advance();
        if (fbTrim)
            r = r.trim();
        return r;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public int size() {
        ArrayTokenizer at = new ArrayTokenizer(fString, fSep);
        int i = 0;
        for (; at.hasNext(); i++) at.next();
        return i;
    }
}