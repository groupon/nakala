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

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author alasdair@groupon.com
 */
public abstract class ProcessIOHandler implements ProcessHandler {
    private Thread fStdout;
    private Thread fStderr;

    private static final Logger log = Logger.getLogger(ProcessIOHandler.class);

    protected abstract void processOutput(String line);

    protected void processError(String line) {
        processOutput(line);
    }

    @Override
    public void processStarted(final Process p) {
        Runnable out = new Runnable() {
            public void run() {
                try {
                    for (String line : IoUtil.readLines(new InputStreamReader(p.getInputStream()))) {
                        processOutput(line);
                    }
                } catch (IOException e) {
                    log.error(e);
                }
            }
        };
        Runnable err = new Runnable() {
            public void run() {
                try {
                    for (String line : IoUtil.readLines(new InputStreamReader(p.getErrorStream()))) {
                        processError(line);
                    }
                } catch (IOException e) {
                    log.error(e);
                }
            }
        };
        fStdout = new Thread(out);
        fStderr = new Thread(err);
        fStdout.start();
        fStderr.start();
    }

    @Override
    public void processFinished(final Process p) {
        try {
            fStdout.join(1000);
        } catch (InterruptedException e) {
            log.error(e);
        }
        try {
            fStderr.join(1000);
        } catch (InterruptedException e) {
            log.error(e);
        }
    }
}
