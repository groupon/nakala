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

import com.groupon.nakala.analysis.AnalysisCollector;
import com.groupon.nakala.analysis.CollectionAnalyzer;
import com.groupon.nakala.analysis.CollectionFeatureCounter;
import com.groupon.nakala.core.Parameters;
import com.groupon.nakala.core.RegexpTokenizerStream;
import com.groupon.nakala.core.StemmingNormalizer;
import com.groupon.nakala.core.StringSet;
import com.groupon.nakala.db.*;
import com.groupon.nakala.normalization.CaseNormalizer;
import com.groupon.nakala.normalization.NumberNormalizer;
import com.groupon.nakala.normalization.SNormalizer;
import com.groupon.nakala.normalization.StringNormalizer;
import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author npendar@groupon.com
 */
public class SimpleTextTokenCounter {
    public static void printUsage(Options opts) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(SimpleTextTokenCounter.class.getName(), opts);
    }

    public static void main(String[] args) {
        Options opts = new Options();
        opts.addOption("h", "help", false, "Displays this message.");
        opts.addOption("i", "input", true, "Input file; - for stdin (Default).");
        opts.addOption("o", "output", true, "Output file; - for stdout (Default).");
        opts.addOption("s", "separator", true, "Field separator pattern.");
        opts.addOption("x", "index", true, "Index of text content in case of delimited input (starting from 1).");
        opts.addOption("m", "stem", false, "Stem incoming tokens.");
        opts.addOption("t", "stopwords", false, "Use stop words list.");
        opts.addOption(null, "stopwords-file", true, "If specified, use the stop words list provided; otherwise, use the default stop words.");

        CommandLineParser parser = new PosixParser();

        CommandLine cli = null;
        try {
            cli = parser.parse(opts, args);
        } catch (ParseException e) {
            System.err.println("Cannot parse command line arguments.");
            System.exit(1);
        }

        if (cli.hasOption("h")) {
            printUsage(opts);
            System.exit(10);
        }

        String infileName = cli.getOptionValue("i", "-");
        CollectionParameters inputParams = new CollectionParameters();
        inputParams.set(CollectionParameters.FILE_NAME, infileName);

        if (cli.hasOption("s")) {
            inputParams.set(CollectionParameters.SEPARATOR, cli.getOptionValue("s", "\t"));
        }

        if (cli.hasOption("x")) {
            inputParams.set(CollectionParameters.TEXT_FIELD, Integer.parseInt(cli.getOptionValue("x")) - 1);
            if (!inputParams.contains(CollectionParameters.SEPARATOR)) {
                inputParams.set(CollectionParameters.SEPARATOR, "\t");
            }
        }

        String outfileName = cli.getOptionValue("o", "-");
        CollectionParameters outputParams = new CollectionParameters();
        outputParams.set(CollectionParameters.FILE_NAME, outfileName);

        CollectionReader input = new SimpleTextCollectionReader();
        input.initialize(inputParams);

        DataStore output = new FlatFileStore();
        output.initialize(outputParams);

        Parameters analyzerParams = new Parameters();
        analyzerParams.set("tokenizer", new RegexpTokenizerStream());

        List<StringNormalizer> normalizers = new ArrayList<StringNormalizer>(2);
        normalizers.add(new NumberNormalizer());
        normalizers.add(new CaseNormalizer());
        normalizers.add(new SNormalizer());

        if (cli.hasOption("stem")) {
            normalizers.add(new StemmingNormalizer());
        }

        analyzerParams.set("normalizers", normalizers);

        if (cli.hasOption("stopwords")) {
            StringSet stopwords = new StringSet();
            String filename = cli.getOptionValue("stopwords-file");
            if (filename == null) {
                if (cli.hasOption("stem")) {
                    stopwords.initialize(SimpleTextTokenCounter.class, "/nakala/stopwords_stemmed.txt");
                } else {
                    stopwords.initialize(SimpleTextTokenCounter.class, "/nakala/stopwords.txt");
                }
            } else {
                stopwords.initialize(filename);
            }
            analyzerParams.set("stopwords", stopwords);
        }

        CollectionAnalyzer analyzer = new CollectionFeatureCounter();
        analyzer.initialize(analyzerParams);

        AnalysisCollector counts = analyzer.analyze(input);
        counts.store(output);

        output.close();
    }
}