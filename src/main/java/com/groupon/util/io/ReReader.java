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

import java.io.File;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * @author alasdair@groupon.com
 */
public class ReReader extends FilterReader {

    private File fTmpFile;
    private Reader fReRead;
    private boolean fbRead;

    public ReReader(Reader r) {
        super(r);
        try {
            fTmpFile = File.createTempFile("reread", "txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reopen() throws IOException {
        if (fbRead) {
            close();
            fReRead = IoUtil.read(fTmpFile.getAbsolutePath());
        }
    }

    @Override
    public void close() throws IOException {
        if (fReRead == null) {
            super.close();
        } else {
            fReRead.close();
        }
        fbRead = false;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        fbRead = true;
        if (fReRead == null) {
            return super.read(cbuf, off, len);
        } else {
            return fReRead.read(cbuf, off, len);
        }
    }

}
