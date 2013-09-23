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

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * @author alasdair@groupon.com
 */
public class QuotedStringTokenizer implements Enumeration<String> {

    private String fInput;
    private int fiStart;
    private int fiEnd;
    private String fSeps;
    private String fOpenQuotes = "\"'";
    private String fCloseQuotes = fOpenQuotes;
    private boolean fbReturnEmpty;
    private boolean fbStripQuotes;

    public QuotedStringTokenizer(String input) {
        this(input, " \t\n", false);
    }

    public QuotedStringTokenizer(String input, String seps) {
        this(input, seps, false);
    }

    public QuotedStringTokenizer(String input, String seps, boolean bReturnEmpty) {
        fInput = input;
        fSeps = seps;
        fbReturnEmpty = bReturnEmpty;
    }

    public QuotedStringTokenizer setStripQuotes(boolean stripQuotes) {
        fbStripQuotes = stripQuotes;
        return this;
    }

    public boolean getStripQuotes() {
        return fbStripQuotes;
    }

    public void setQuotes(String quotes) {
        fOpenQuotes = fCloseQuotes = quotes;
    }

    public void setOpenQuotes(String quotes) {
        fOpenQuotes = quotes;
        fiStart = fiEnd = 0;
    }

    public String getOpenQuotes() {
        return fOpenQuotes;
    }

    public void setCloseQuotes(String quotes) {
        fCloseQuotes = quotes;
    }

    public String getCloseQuotes() {
        return fCloseQuotes;
    }

    private void advance() {
        for (; fiStart < fInput.length(); ) {
            List<Character> quoteStack = new LinkedList<Character>();
            fiStart = fiEnd;
            for (; fiStart < fInput.length(); fiStart++) {
                if (fSeps.indexOf(fInput.charAt(fiStart)) < 0)
                    break;
            }
            for (fiEnd = fiStart; fiEnd < fInput.length(); fiEnd++) {
                char c = fInput.charAt(fiEnd);
                if (quoteStack == null || quoteStack.size() == 0) {
                    if (fSeps.indexOf(c) >= 0)
                        break;
                }
                int iq = fOpenQuotes.indexOf(c);
                if (iq >= 0) {
                    char cc = fCloseQuotes.charAt(iq);
                    if (quoteStack == null) {
                        quoteStack = new LinkedList<Character>();
                    }
                    /*--- check if it's the same open/closeWriter character and closeWriter if we're in it */
                    if ((c == cc) && (quoteStack.size() > 0) && (quoteStack.get(0) == c)) {
                        quoteStack.remove(0);
                    } else {
                        /*--- must be an open */
                        quoteStack.add(0, c);
                    }
                } else if ((iq = fCloseQuotes.indexOf(c)) >= 0) {
                    char oc = fOpenQuotes.charAt(iq);
                    if ((quoteStack != null) && (quoteStack.size() > 0) && (quoteStack.get(0) == oc)) {
                        /*--- pop! */
                        quoteStack.remove(0);
                    }
                }
            }
            if ((fiEnd - fiStart > 0) || fbReturnEmpty)
                return;
        }
    }

    public String[] toArray() {
        QuotedStringTokenizer qt = new QuotedStringTokenizer(fInput, fSeps).setStripQuotes(fbStripQuotes);
        qt.fbReturnEmpty = fbReturnEmpty;
        qt.fCloseQuotes = fCloseQuotes;
        qt.fOpenQuotes = fOpenQuotes;
        qt.fSeps = fSeps;
        List<String> l = new LinkedList<String>();
        for (; qt.hasMoreElements(); ) {
            l.add(qt.nextElement());
        }
        return l.toArray(new String[l.size()]);
    }

    @Override
    public boolean hasMoreElements() {
        if (fiStart == fiEnd && fiStart == 0)
            advance();
        return fiStart < fInput.length();
    }

    @Override
    public String nextElement() {
        if (fiStart == fiEnd && fiStart == 0)
            advance();
        try {
            String result = fInput.substring(fiStart, fiEnd);
            if (fbStripQuotes) {
                result = Strings.stripQuotes(result);
            }
            return result;
        } finally {
            advance();
        }
    }

}
