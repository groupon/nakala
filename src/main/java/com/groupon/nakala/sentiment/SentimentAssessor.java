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
import com.groupon.nakala.analysis.MultiScoreAnalysis;
import com.groupon.nakala.core.Analyzable;
import com.groupon.nakala.core.Constants;
import com.groupon.nakala.core.Parameters;
import com.groupon.nakala.core.Place;
import com.groupon.nakala.exceptions.AnalyzerFailureException;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Set;

/**
 * @author npendar@groupon.com
 */
public class SentimentAssessor implements Analyzer {
    protected static final Logger logger = Logger.getLogger(SentimentAssessor.class);
    private static int MIN_EXTRACTION_COUNT = 3;
    private SentimentAnalyzer sentimentAnalyzer;
    private String[] domains;

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        if (params.contains(Constants.DOMAINS)) {
            domains = ((List<String>) params.get(Constants.DOMAINS)).toArray(new String[1]);
            logger.debug("Domains to load: " + StringUtils.join(domains, ", "));
        } else {
            throw new ResourceInitializationException("Domains must be specified for SentimentAssessor.");
        }

        sentimentAnalyzer = new SentimentAnalyzer();
        sentimentAnalyzer.initialize(params);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public Analysis analyze(Analyzable a) throws AnalyzerFailureException {
        if (!(a instanceof Place)) {
            throw new AnalyzerFailureException("Analyzable must be an instance of Place. Received " + a.getClass().getName());
        }

        ExtractedRecordCollector erc = (ExtractedRecordCollector) sentimentAnalyzer.analyze(a);
        Set<ExtractedReviewRecord> records = erc.getTableToOutput();
        MultiScoreAnalysis analysis = new MultiScoreAnalysis(((Place) a).getId());

        for (String domain : domains) {
            double score = 0d;
            int count = 0;
            int total = 0;

            for (ExtractedReviewRecord rec : records) {
                if (!rec.getDomain().equals(domain)) {
                    continue;
                }
                ++count;

                if (rec.getScore() > 0.5) {
                    ++total;
                } else if (rec.getScore() < 0.5) {
                    --total;
                }
            }

            if (count >= MIN_EXTRACTION_COUNT) {
                if (total >= 0) {
                    score = (double) Math.min(100, total);
                } else {
                    score = (double) Math.max(-100, total);
                }
            }

            analysis.put(domain, score);
        }

        return analysis;
    }
}
