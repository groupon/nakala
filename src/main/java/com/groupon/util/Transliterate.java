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

import com.groupon.util.collections.Mapper;
import com.groupon.util.io.IoUtil;
import com.groupon.util.io.WorkerProcess;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author alasdair@groupon.com
 */
public class Transliterate implements Mapper<String, String> {
    private String fFromCharSet;
    private WorkerProcess fWorker;
    private static Map<String, WorkerProcess> gWorker = new HashMap<String, WorkerProcess>();

    public Transliterate(String fromCharSet) {
        //fFromCharSet = fromCharSet == null ? "UTF-8" : fromCharSet;
        /*--- we'll always be sending down UTF-8, right? */
        fFromCharSet = "UTF-8";
    }

    public Transliterate() {
        this("UTF-8");
    }

    @Override
    public String map(String key) {
        if (key == null)
            return null;
        try {
            if (fWorker == null) {
                synchronized (gWorker) {
                    fWorker = gWorker.get(fFromCharSet);
                    if (fWorker == null) {
                        File script = File.createTempFile("txl", ".rb");
                        script.deleteOnExit();
                        IoUtil.copy(IoUtil.read(getClass(), "transliterate.rb"), IoUtil.write(script)).close();
                        gWorker.put(fFromCharSet, fWorker = new WorkerProcess(
                                new ProcessBuilder("ruby", script.getAbsolutePath(), "-e", fFromCharSet).start()));
                    }
                }
            }
            return fWorker.send(key);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
