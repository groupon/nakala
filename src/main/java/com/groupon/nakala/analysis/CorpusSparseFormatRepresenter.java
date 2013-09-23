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

package com.groupon.nakala.analysis;

import com.groupon.nakala.core.*;
import com.groupon.nakala.db.CollectionReader;
import com.groupon.nakala.exceptions.AnalyzerFailureException;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author npendar@groupon.com
 */
public class CorpusSparseFormatRepresenter extends AbstractCollectionAnalyzer {

    TextRepresenter representer;

    @Override
    public void initialize(Parameters params) {
        super.initialize(params);

        if (params.contains(Constants.REPRESENTER)) {
            representer = (TextRepresenter) params.get(Constants.REPRESENTER);
        } else {
            throw new ResourceInitializationException("No representer specified.");
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public AnalysisCollector analyze(CollectionReader cr) throws AnalyzerFailureException {

        logger.debug("Getting a list of categories...");
        Set<String> categories = new HashSet<String>();
        for (Analyzable a : cr) {
            CategorizedTextContent ctc = (CategorizedTextContent) a;
            categories.addAll(ctc.getCategories());
        }
        List<String> categoryList = new ArrayList<String>(categories);
        Collections.sort(categoryList);

        logger.debug("Got " + categoryList.size() + " categories.");
        logger.debug("Category list: " + StringUtils.join(categoryList, ", "));

        cr.reset();

        ListAnalysisCollector analysisCollector = new ListAnalysisCollector();

        for (Analyzable a : cr) {
            CategorizedTextContent ctc = (CategorizedTextContent) a;
            SparseRepresentation rep = representer.represent(ctc.getText());
            String repStr = rep.toSparseFormat();

            for (String category : ctc.getCategories()) {
                StringBuilder sb = new StringBuilder();
                sb.append(categoryList.indexOf(category)).append(' ').append(repStr);
                analysisCollector.addAnalysis(new PlainTextAnalysis(sb.toString()));
            }
        }

        return analysisCollector;
    }
}
