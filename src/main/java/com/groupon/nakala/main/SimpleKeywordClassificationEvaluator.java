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

package com.groupon.nakala.main;

import com.groupon.ml.ClassificationAnalysis;
import com.groupon.ml.ClassificationAnalysisCollector;
import com.groupon.ml.ClassificationMetrics;
import com.groupon.ml.ClassificationReportWriter;
import com.groupon.nakala.core.*;
import com.groupon.nakala.db.CollectionParameters;
import com.groupon.nakala.db.CollectionReader;
import com.groupon.nakala.db.TsvCategorizedTextCollectionReader;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author npendar@groupon.com
 */
public class SimpleKeywordClassificationEvaluator {
    private static final Logger logger = Logger.getLogger(SimpleKeywordClassificationEvaluator.class);

    private static void printUsage() {
        System.out.println("USAGE: SimpleKeywordClassificationEvaluator <CORPUS> <KEYWORDS> <OUTPUT_FILE>");
    }


    private static void output(final String outputFileName,
                               final List<String> categories,
                               final Map<Id, Set<String>> id2truecategories,
                               final ClassificationAnalysisCollector id2predictions) throws IOException {
        Workbook workbook = new HSSFWorkbook();
        double threshold = 1d;
        ClassificationMetrics metrics = new ClassificationMetrics(threshold, categories, id2truecategories, id2predictions);
        ClassificationReportWriter.writeStats(workbook.createSheet("stats" + threshold), metrics);
        ClassificationReportWriter.writeDetails(workbook.createSheet("details"), categories, id2truecategories, id2predictions);
        FileOutputStream outFile = new FileOutputStream(outputFileName);
        workbook.write(outFile);
        outFile.close();
    }

    private static Map<String, PassFilter> parseKeywords(StringSet stringSet) {
        Map<String, PassFilter> map = new HashMap<String, PassFilter>();
        for (String s : stringSet) {
            String[] pieces = s.split(":");
            String category = pieces[0];

            Set<String> keywords = new HashSet<String>();
            for (String k : pieces[1].split(","))
                keywords.add(k);

            PassFilter passFilter = new RegexpPassFilter();
            passFilter.initialize(keywords);

            map.put(category, passFilter);
        }
        return map;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            printUsage();
            System.exit(1);
        }

        String corpusFileName = args[0];
        String keywordsFileName = args[1];
        String outputFileName = args[2];

        if (!outputFileName.endsWith(".xls")) {
            outputFileName += ".xls";
        }

        CollectionParameters inputParams = new CollectionParameters();
        inputParams.set(CollectionParameters.FILE_NAME, corpusFileName);
        inputParams.set(CollectionParameters.ID_FIELD, 0);
        inputParams.set(CollectionParameters.LABEL_FIELD, 1);
        inputParams.set(CollectionParameters.TEXT_FIELD, 2);

        CollectionReader input = new TsvCategorizedTextCollectionReader();
        input.initialize(inputParams);

        StringSet keywordSet = new StringSet();
        keywordSet.initialize(keywordsFileName);

        Map<String, PassFilter> cat2passfilter = parseKeywords(keywordSet);

        Map<Id, Set<String>> id2truecategories = new HashMap<Id, Set<String>>();
        ClassificationAnalysisCollector id2predictions = new ClassificationAnalysisCollector();

        int c = 0;
        int total = input.getSize();

        for (Analyzable a : input) {
            logger.info("Processing " + ++c + " of " + total);

            CategorizedTextContent ctc = (CategorizedTextContent) a;
            id2truecategories.put(ctc.getId(), ctc.getCategories());

            ClassificationAnalysis predictions = new ClassificationAnalysis(ctc.getId());
            for (Map.Entry<String, PassFilter> e : cat2passfilter.entrySet()) {
                String category = e.getKey();
                PassFilter passFilter = e.getValue();
                predictions.addClassification(category, passFilter.passes(ctc) ? 1d : 0d);
            }
            id2predictions.put(ctc.getId(), predictions);
        }


        logger.info("Writing output to " + outputFileName);

        List<String> categories = new ArrayList<String>(cat2passfilter.keySet());
        Collections.sort(categories);

        output(outputFileName, categories, id2truecategories, id2predictions);
        logger.info("done.");
    }
}
