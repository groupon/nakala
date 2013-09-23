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

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * @author alasdair@groupon.com
 */
public class CaptureProcessHandler extends ProcessIOHandler {
    private PrintWriter fOutput;
    private PrintWriter fError;

    public CaptureProcessHandler() {
        this(new OutputStreamWriter(System.out), new OutputStreamWriter(System.err));
    }

    public CaptureProcessHandler(Writer out, Writer err) {
        fOutput = out instanceof PrintWriter ? (PrintWriter) out : new PrintWriter(out);
        fError = err instanceof PrintWriter ? (PrintWriter) err : new PrintWriter(err);
    }

    @Override
    protected void processError(String line) {
        fError.println(line);
    }

    @Override
    protected void processOutput(String line) {
        fOutput.println(line);
    }
}
