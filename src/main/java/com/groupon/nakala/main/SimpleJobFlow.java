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
import com.groupon.nakala.core.SimpleJobFlowSpecs;
import com.groupon.nakala.db.CollectionReader;
import com.groupon.nakala.db.DataStore;
import com.groupon.util.io.IoUtil;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

/**
 * @author npendar@groupon.com
 */
public final class SimpleJobFlow {
    private static final Logger logger = Logger.getLogger(SimpleJobFlow.class);

    private static void printUsage(Options opts) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(SimpleJobFlow.class.getName(), opts);
    }

    public static void main(String[] args) {
        Options opts = new Options();
        opts.addOption("h", "help", false, "Displays this message.");
        opts.addOption("c", "config", true, "Job flow configuration file (required).");

        Parser parser = new PosixParser();

        CommandLine cli = null;
        try {
            cli = parser.parse(opts, args);
        } catch (ParseException e) {
            logger.fatal("Failed to parse command line options. " + e.getMessage());
            System.exit(1);
        }

        if (cli.hasOption("h")) {
            printUsage(opts);
            System.exit(10);
        }

        if (!cli.hasOption("c")) {
            logger.fatal("Config file not specified.");
            System.exit(20);
        }

        SimpleJobFlowSpecs specs = new SimpleJobFlowSpecs();

        try {
            specs.initialize(IoUtil.input(cli.getOptionValue("c")));
        } catch (Exception e) {
            logger.fatal("Failed to load configuration file " + cli.getOptionValue("c") + ". " + e.getMessage());
            e.printStackTrace();
            System.exit(30);
        }
        CollectionReader input = specs.getCollectionReader();
        CollectionAnalyzer collectionAnalyzer = specs.getCollectionAnalyzer();
        DataStore[] outputs = specs.getDataStores();

        try {
            AnalysisCollector analysisCollector = collectionAnalyzer.analyze(input);
            input.close();
            collectionAnalyzer.shutdown();

            for (DataStore output : outputs) {
                analysisCollector.store(output);
                output.close();
            }
        } catch (Exception e) {
            collectionAnalyzer.shutdown();
            logger.fatal("Job flow failed.", e);
            e.printStackTrace();
        }
    }
}
