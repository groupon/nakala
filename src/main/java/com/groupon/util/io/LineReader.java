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

package com.groupon.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * @author alasdair@groupon.com
 */
public class LineReader extends Reader {

    private BufferedReader fReader;
    private String fSource;
    private int fnLine;
    private String fLine;

    public LineReader(Reader r) {
        fReader = r instanceof BufferedReader ? (BufferedReader) r : new BufferedReader(r);
    }

    public LineReader setSource(String source) {
        fSource = source;
        return this;
    }

    public String getSource() {
        return fSource;
    }

    public int getLine() {
        return fnLine;
    }

    @Override
    public void close() throws IOException {
        fReader.close();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (fLine == null) {
            fnLine++;
            fLine = fReader.readLine();
            if (fLine == null) {
                return -1;
            }
            fLine += "\n";
        }
        if (fLine.length() <= len) {
            len = fLine.length();
            fLine.getChars(0, len, cbuf, off);
            fLine = null;
        } else {
            fLine.getChars(0, len, cbuf, off);
            fLine = fLine.substring(len);
        }
        return len;
    }
}
