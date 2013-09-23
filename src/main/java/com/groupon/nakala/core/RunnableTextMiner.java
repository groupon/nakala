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

import com.groupon.nakala.analysis.Analysis;
import com.groupon.nakala.analysis.Analyzer;
import com.groupon.nakala.db.CollectionReader;
import com.groupon.nakala.db.DataStore;
import org.apache.log4j.Logger;

/**
 * @author npendar@groupon.com
 */
public class RunnableTextMiner implements Runnable {
    protected static final Logger logger = Logger.getLogger(RunnableTextMiner.class);
    protected Analyzer analyzer;
    protected Parameters analyzerParameters;
    protected CollectionReader dataSource;
    protected DataStore[] dataStores;

    public RunnableTextMiner(Analyzer analyzer,
                             CollectionReader dataSource,
                             DataStore[] dataStores) {
        this.analyzer = analyzer;
        this.dataSource = dataSource;
        this.dataStores = dataStores;
    }

    @Override
    public void run() {
        for (Analyzable a : dataSource) {
            Analysis analysis = analyzer.analyze(a);
            if (analysis == null) {
                continue;
            }

            for (DataStore ds : dataStores) {
                analysis.store(ds);
            }
        }
    }
}
