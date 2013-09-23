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

import java.io.*;

/**
 * @author alasdair@groupon.com
 */
public class WorkerProcess {
    private Process fProcess;
    private PrintWriter fSend;
    private BufferedReader fRecv;
    private StringWriter fErrors;

    private static final Logger log = Logger.getLogger(WorkerProcess.class);

    public WorkerProcess(Process process) {
        fProcess = process;
        try {
            fSend = new PrintWriter(new OutputStreamWriter(process.getOutputStream(), "utf8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        fRecv = new BufferedReader(new InputStreamReader(fProcess.getInputStream()));
        fErrors = new StringWriter();
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (String line : IoUtil.readLines(new InputStreamReader(fProcess.getErrorStream()))) {
                        log.error(line);
                        fErrors.append(line).append("\n");
                    }
                } catch (IOException e) {
                    log.error("errors", e);
                }
            }
        });
        th.setDaemon(true);
        th.start();
    }

    public Process getProcess() {
        return fProcess;
    }

    private String recvLine() throws IOException {
        String line = fRecv.readLine();
        if (line == null) {
            /*--- unexpected end of file */
            throw new IOException("Unexpected process termination: " + fErrors.toString());
        }
        return line;
    }

    public String send(String line) throws IOException {
        int off = line.indexOf("\n");
        if (off >= 0) {
            int nLines = 0;
            for (; off >= 0; ) {
                fSend.println(line.substring(0, off));
                nLines++;
                line = line.substring(off + 1);
                off = line.indexOf("\n");
            }
            if (line.length() > 0) {
                fSend.println(line);
                nLines++;
            }
            fSend.flush();
            StringBuilder sb = new StringBuilder();
            for (int iLine = 0; iLine < nLines; iLine++) {
                if (iLine > 0) {
                    sb.append("\n");
                }
                sb.append(recvLine());
            }
            return sb.toString();
        } else {
            fSend.println(line);
            fSend.flush();
            return recvLine();
        }
    }
}
