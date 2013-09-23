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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Utility to suck data out of an input source and write it to a log continuously.
 * Useful to attach to a process, for example, to log all error output.
 *
 * @author alasdair@groupon.com
 */
public class ReadLogger implements Runnable {
    private Reader fReader;
    private Logger fLog;
    private Level fLevel;
    private Thread fThread;

    public ReadLogger(Reader r, Logger log, Level level) {
        fReader = r;
        fLog = log;
        fLevel = level;
    }

    public Thread start() {
        getThread();
        if (!fThread.isAlive())
            fThread.start();
        return fThread;
    }

    public Thread getThread() {
        if (fThread == null) {
            fThread = new Thread(this);
        }
        return fThread;
    }

    public void join() {
        try {
            fThread.join();
        } catch (InterruptedException e) {
        }
    }

    public void run() {
        BufferedReader br = (fReader instanceof BufferedReader) ? (BufferedReader) fReader : new BufferedReader(fReader);
        try {
            for (String line; (line = br.readLine()) != null; ) {
                fLog.log(fLevel, line);
            }
        } catch (IOException e) {
            fLog.error("Error reading from input source.", e);
        }
    }
}
