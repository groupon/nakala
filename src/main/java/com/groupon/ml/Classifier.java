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

package com.groupon.ml;

import com.groupon.nakala.analysis.Analyzer;
import com.groupon.nakala.core.Constants;
import com.groupon.nakala.core.Parameters;
import com.groupon.nakala.core.ResourceReader;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author npendar@groupon.com
 */

public abstract class Classifier implements Analyzer {
    protected static final Logger logger = Logger.getLogger(Classifier.class);

    protected List<String> labels;
    protected double threshold = 0d;

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        if (params.contains(Constants.LABELS)) {
            labels = loadLabels(((ResourceReader) params.get(Constants.LABELS)).getReader());
        } else {
            throw new ResourceInitializationException("No labels provided.");
        }
        if (params.contains(Constants.THRESHOLD)) {
            threshold = params.getDouble(Constants.THRESHOLD);
        }
    }

    @Override
    public void shutdown() {

    }

    private List<String> loadLabels(BufferedReader reader) throws ResourceInitializationException {
        List<String> labels = new LinkedList<String>();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) labels.add(line);
            }
        } catch (IOException e) {
            throw new ResourceInitializationException("Failed to load labels.", e);
        }
        return labels;
    }

    public List<String> getLabels() {
        return labels;
    }

    public double getThreshold() {
        return threshold;
    }
}
