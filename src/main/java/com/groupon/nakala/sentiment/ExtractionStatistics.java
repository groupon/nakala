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

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author npendar@groupon.com
 */
public class ExtractionStatistics {
    private static Logger log = Logger.getLogger(ExtractionStatistics.class);
    private Map<String, Integer> distribPos, distribNeg;
    private int size;
    private String[] myFeatures = null;

    public ExtractionStatistics(String[] features) {
        size = features.length;
        distribPos = Collections.synchronizedMap(new HashMap<String, Integer>());
        distribNeg = Collections.synchronizedMap(new HashMap<String, Integer>());
        myFeatures = features.clone();
        for (String f : myFeatures) {
            distribPos.put(f, 0);
            distribNeg.put(f, 0);
        }
    }

    public void addExtractionResults(ExtractedRecordCollector erc) {
        if (erc == null || erc.isEmpty())
            return;
        for (ExtractedReviewRecord er : erc.values()) {
            if (er.getScore() > 0.5) {
                Integer c = distribPos.get(er.getDomain());
                distribPos.put(er.getDomain(), ++c);
            } else {
                Integer c = distribNeg.get(er.getDomain());
                distribNeg.put(er.getDomain(), ++c);
            }
        }
    }

    public void printStatistics() {
        StringBuffer sb = new StringBuffer("\n");
        for (int f = 0; f < size; f++) {
            sb.append(myFeatures[f] + " ");
        }
        sb.append("\n== p o s i t i v e s ==========================================================================\n");
        for (int f = 0; f < size; f++) {
            sb.append(distribPos.get(myFeatures[f]) + "      ");
        }
        sb.append("\n== n e g a t i v e s ==========================================================================\n");
        for (int f = 0; f < size; f++) {
            sb.append(distribNeg.get(myFeatures[f]) + "      ");
        }
        log.info(sb.toString());
    }
}