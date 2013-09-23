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

/**
 *
 */
package com.groupon.util.io;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Handy little writer class that allows you to format output
 * with indentations.
 *
 * @author alasdair@groupon.com
 */
public class IndentWriter extends FilterWriter {
    private int fnIndent = 0;
    private String fIndentString = "  ";
    private boolean fbAtStart = true;
    private boolean fbAutoIndent = true;

    public IndentWriter(Writer w) {
        super(w);
    }

    public void setAutoIndent(boolean indent) {
        fbAutoIndent = indent;
    }

    public boolean getAutoIndent() {
        return fbAutoIndent;
    }

    public static IndentWriter cast(Writer w) {
        return (w instanceof IndentWriter) ? (IndentWriter) w : new IndentWriter(w);
    }

    public int getIndent() {
        return fnIndent;
    }

    public void setIndent(int indent) {
        fnIndent = indent;
    }

    public String getIndentString() {
        return fIndentString;
    }

    public void setIndentString(String indentString) {
        fIndentString = indentString;
    }

    public int push() {
        return ++fnIndent;
    }

    public int pop() {
        if (fnIndent > 0)
            --fnIndent;
        return fnIndent;
    }

    private void indent() throws IOException {
        if (!fbAtStart) return;
        fbAtStart = false;
        for (int i = 0; i < fnIndent; i++) {
            super.write(fIndentString);
        }
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (!fbAutoIndent) {
            super.write(cbuf, off, len);
            if (len > 0)
                fbAtStart = cbuf[off + len - 1] == '\n';
            return;
        }
        int i = 0;
        for (; i < len; i++) {
            if (cbuf[i] == '\n') {
                indent();
                if (i > 0) {
                    super.write(cbuf, off, i - off);
                }
                newline();
                off = i + 1;
            }
        }
        if (off < len) {
            indent();
            super.write(cbuf, off, len - off);
            len = 0;
        }
    }

    @Override
    public void write(int c) throws IOException {
        if (!fbAutoIndent) {
            super.write(c);
        } else {
            if ((char) c == '\n') {
                newline();
            } else {
                indent();
                super.write(c);
            }
        }
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        if (!fbAutoIndent) {
            super.write(str, off, len);
            if (len > 0)
                fbAtStart = str.charAt(off + len - 1) == '\n';
            return;
        }
        int i = 0;
        for (; i < len; i++) {
            if (str.charAt(i) == '\n') {
                indent();
                if (i > 0) {
                    super.write(str, off, i - off);
                }
                newline();
                off = i + 1;
            }
        }
        if (off < len) {
            indent();
            super.write(str, off, len - off);
            len = 0;
        }
    }

    private void newline() throws IOException {
        super.write('\n');
        fbAtStart = true;
    }
}