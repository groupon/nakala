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

package com.groupon.nakala.sentiment;

import com.groupon.nakala.analysis.Analysis;
import com.groupon.nakala.analysis.Analyzer;
import com.groupon.nakala.core.*;
import com.groupon.nakala.exceptions.AnalyzerFailureException;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.List;

/**
 * @author npendar@groupon.com
 */
public class SentimentAnalyzer implements Analyzer {
    protected static final Logger logger = Logger.getLogger(SentimentAnalyzer.class);

    private ReviewExtractor reviewExtractor;
    private ExcerptIndexer indexer;
    private PrePostProcessor prePostProcessor;

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        String[] domainsToLoad = null;
        if (params.contains(Constants.DOMAINS)) {
            domainsToLoad = ((List<String>) params.get(Constants.DOMAINS)).toArray(new String[1]);
            logger.debug("Domains to load: " + StringUtils.join(domainsToLoad, ", "));
        }

        boolean bPolite = false;
        if (params.contains(Constants.POLITE)) {
            bPolite = (Boolean) params.get(Constants.POLITE);
            logger.debug("Polite: " + bPolite);
        }

        try {
            reviewExtractor = ReviewExtractor.newInstance(domainsToLoad, bPolite);
        } catch (IOException e) {
            throw new ResourceInitializationException("Failed to initialize internal analyzer.", e);
        }

        try {
            prePostProcessor = PrePostProcessor.newInstance();
        } catch (IOException e) {
            throw new ResourceInitializationException("Failed to initialize pre-/post-processor.", e);
        }

        indexer = new ExcerptIndexer(new WhitespaceAnalyzer(Version.LUCENE_36), prePostProcessor);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public Analysis analyze(Analyzable a) throws AnalyzerFailureException {
        TitledContentArray titledContentArray = null;

        if (a instanceof Place) {
            titledContentArray = ((Place) a).getReviews();
        } else if (a instanceof IdentifiableTextContent) {
            IdentifiableTextContent itc = (IdentifiableTextContent) a;
            titledContentArray = new TitledContentArray();
            titledContentArray.add(new SimpleTitledTextContent(itc.getId(), "", itc.getText()));
        } else if (a instanceof TitledContentArray) {
            titledContentArray = (TitledContentArray) a;
        } else {
            throw new AnalyzerFailureException("Analyzable must be an instance of Place, TitledContentArray or IdentifiableTextContent. Received " + a.getClass().getName());
        }

        try {
            indexer.index(titledContentArray);
        } catch (Exception e) {
            throw new AnalyzerFailureException("Failed to index reviews.", e);
        }

        ExtractedRecordCollector erc = null;
        try {
            erc = reviewExtractor.extract(indexer.getIndexSearcher(), prePostProcessor);
        } catch (Exception e) {
            logger.error("Failed to get index searcher.");
        }

        return erc;
    }
}
