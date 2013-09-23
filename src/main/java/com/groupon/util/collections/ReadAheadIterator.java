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

package com.groupon.util.collections;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Support class for iteration that requires scanning some external
 * iteration source to determine what's next. This class takes care
 * of maintaining and refilling a buffer of data that we've retrieved
 * ahead of time and serves that data via the usual iteration interface.
 *
 * @param <T>
 * @author alasdair@groupon.com
 */
public class ReadAheadIterator<T> implements Iterator<T>, Runnable {

    //private static final Logger log = Logger.getLogger(ReadAheadIterator.class);

    /*--- iteration elements are dumped into here */
    private List<T> fBuffer;

    /*--- have we been told we're done */
    private boolean fbDone;

    /*--- filler used to get more data when we need it */
    private ReadAheadFiller<T> fFiller;

    /*--- the filler background thread */
    private Thread fThread;

    /*--- is our thread running */
    private boolean fbRunning;

    /*--- signals that we want more data from the background thread */
    private boolean fbNeedData;

    public ReadAheadIterator(ReadAheadFiller<T> filler) {
        fFiller = filler;
    }

    protected ReadAheadIterator() {
    }

    protected <F extends ReadAheadFiller<T>> F setFiller(F filler) {
        fFiller = filler;
        return filler;
    }

    public ReadAheadIterator<T> setThreaded(boolean b) {
        fThread = b == false ? null : new Thread(this, "ReadAheadIterator::" + fFiller);
        return this;
    }

    public boolean getThreaded() {
        return fThread != null;
    }

    public void run() {
        List<T> next = new LinkedList<T>();
        for (; fbRunning; ) {
            boolean bDone = fFiller.fill(next);
            synchronized (this) {
                /*--- wait to be told we need more data */
                while (fbRunning && !fbNeedData) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
                if (!fbRunning) break;

                /*--- swap the current buffer out for our new one and pass on the done flag */
                List<T> t = fBuffer;
                fBuffer = next;
                fbDone = bDone;
                next = t;
                notifyAll();
                fbRunning = !bDone;
                fbNeedData = false;
            }
        }
    }

    protected void start() {
        fBuffer = new LinkedList<T>();
        if (fThread != null) {
            fbRunning = true;
            fThread.setDaemon(true);
            fThread.start();
        }
        fill();
    }

    protected void fill() {
        if (fThread == null) {
            fbDone = fFiller.fill(fBuffer);
        } else {
            synchronized (this) {
                /*--- indicate that we want data */
                fbNeedData = true;
                notifyAll();

                /*--- wait for the data to come back to us */
                while (fbNeedData) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    @Override
    public final boolean hasNext() {
        if (fBuffer == null) start();
        if (fBuffer.size() > 0)
            return true;
        if (fbNeedData) {
            synchronized (this) {
                while (fbNeedData) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        return fBuffer.size() > 0;
    }

    @Override
    public final T next() {
        if (fBuffer == null) start();
        try {
            return fBuffer.remove(0);
        } finally {
            if ((fBuffer.size() == 0) && !fbDone) {
                fill();
            }
        }
    }

    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }

}
