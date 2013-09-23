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

package com.groupon.nakala.core;

import com.groupon.nakala.analysis.Analyzer;
import com.groupon.nakala.db.CollectionReader;
import com.groupon.nakala.db.DataStore;
import com.groupon.nakala.exceptions.TextminingException;
import com.groupon.util.Progress;
import com.groupon.util.TextProgressListener;
import com.groupon.util.io.LogWriter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author npendar@groupon.com
 */
public class MultiThreadedJobFlow {
    protected static final Logger logger = Logger.getLogger(MultiThreadedJobFlow.class);

    protected CollectionReader collectionReader;
    protected DataStore[] dataStores;
    protected Analyzer[] analyzers;

    protected int numberOfThreads = 1;
    protected Progress progress;

    public MultiThreadedJobFlow(CollectionReader collectionReader,
                                Analyzer[] analyzers,
                                DataStore[] dataStores) {


        this.collectionReader = collectionReader;
        this.analyzers = analyzers;
        this.dataStores = dataStores;
        this.numberOfThreads = analyzers.length;
    }

    public void process() throws TextminingException {
        progress = new Progress("Performing analysis", collectionReader.getSize());
        Thread[] threads = new Thread[numberOfThreads];
        TextProgressListener tpl = new TextProgressListener(new LogWriter(logger, Level.INFO));
        tpl.setWholeNumbers(true);
        tpl.setUpdateFrequency(1000L);
        progress.setAutoStart();

        for (int i = 0; i < numberOfThreads; ++i) {
            Analyzer a = analyzers[i];
            threads[i] = new Thread(new RunnableTextMiner(a, collectionReader, dataStores));
            threads[i].start();
        }

        int nAlive = threads.length;
        while (nAlive > 0) {
            for (int i = 0; i < threads.length; ++i) {
                if (threads[i] != null) {
                    try {
                        threads[i].join(1000L);
                        tpl.progressUpdated(progress);
                        if (!threads[i].isAlive()) {
                            threads[i] = null;
                            logger.debug("Thread finished...");
                            --nAlive;
                        }

                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        progress.stop();
        tpl.progressUpdated(progress);
        logger.debug("All threads finished.");
        logger.debug("Shutting down.");
        for (DataStore ds : dataStores) {
            ds.close();
        }
        logger.debug("Done!");
    }

    public void setLogLevel(Level l) {
        logger.setLevel(l);
    }
}
