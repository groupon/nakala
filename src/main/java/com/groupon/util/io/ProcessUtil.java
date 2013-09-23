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

import com.groupon.util.QuotedStringTokenizer;

import java.io.IOException;

/**
 * @author alasdair@groupon.com
 */
public class ProcessUtil {

    public static int run(String command, ProcessHandler... handlers) {
        QuotedStringTokenizer st = new QuotedStringTokenizer(command);
        return run(st.toArray(), handlers);
    }

    public static int run(String[] command, ProcessHandler... handlers) {
        return run(new ProcessBuilder(command), handlers);
    }

    public static int run(ProcessBuilder pb, ProcessHandler... handlers) {
        try {
            return exec(pb, handlers);
        } catch (IOException e) {
            return 1;
        }
    }

    public static int exec(String command, ProcessHandler... handlers) throws IOException {
        QuotedStringTokenizer st = new QuotedStringTokenizer(command);
        return exec(st.toArray(), handlers);
    }

    public static int exec(String[] command, ProcessHandler... handlers) throws IOException {
        return exec(new ProcessBuilder(command), handlers);
    }

    public static int exec(ProcessBuilder pb) throws IOException {
        return exec(pb, new CaptureProcessHandler(new DontCloseWriter(System.out), new DontCloseWriter(System.err)));
    }

    public static int exec(ProcessBuilder pb, ProcessHandler... handlers) throws IOException {
        return exec(pb.start(), handlers);
    }

    public static int exec(Process p, ProcessHandler... handlers) throws IOException {
        try {
            for (ProcessHandler ph : handlers)
                ph.processStarted(p);
            return p.waitFor();
        } catch (InterruptedException e) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
            }
            throw new IOException(e);
        } finally {
            for (ProcessHandler ph : handlers)
                ph.processFinished(p);
        }
    }
}
