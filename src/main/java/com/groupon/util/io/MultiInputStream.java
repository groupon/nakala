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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author alasdair@groupon.com
 */
public class MultiInputStream extends InputStream {

    private Iterator<InputStream> fInputStreams;
    private InputStream fCurrent;

    public MultiInputStream(InputStream... il) {
        if (il.length == 0)
            return;
        List<InputStream> l = new LinkedList<InputStream>();
        for (InputStream in : il)
            l.add(in);
        fInputStreams = l.iterator();
        fCurrent = fInputStreams.next();
    }

    @Override
    public void close() throws IOException {
        fCurrent.close();
        for (; fInputStreams.hasNext(); )
            fInputStreams.next().close();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        for (; ; ) {
            int n = fCurrent.read(b, off, len);
            if ((n > 0) || !fInputStreams.hasNext())
                return n;
            fCurrent = fInputStreams.next();
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        for (; ; ) {
            int n = fCurrent.read(b);
            if ((n > 0) || !fInputStreams.hasNext())
                return n;
            fCurrent = fInputStreams.next();
        }
    }

    @Override
    public int read() throws IOException {
        for (; ; ) {
            int n = fCurrent.read();
            if ((n >= 0) || !fInputStreams.hasNext())
                return n;
            fCurrent = fInputStreams.next();
        }
    }

}
