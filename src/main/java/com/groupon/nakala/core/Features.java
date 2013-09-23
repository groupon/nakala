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

import com.groupon.nakala.analysis.Initializable;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import com.groupon.util.io.IoUtil;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author npendar@groupon.com
 */
public class Features implements Serializable, Initializable {
    Map<String, Feature> word2feature;
    String[] strings; // For reverse lookup

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        if (params.contains(Constants.FILE_NAME)) {
            initialize(params.getString(Constants.FILE_NAME));
        } else if (params.contains(Constants.RESOURCE)) {
            initialize(getClass(), params.getString(params.getString(Constants.RESOURCE)));
        } else if (params.contains(Constants.RESOURCE_STREAM)) {
            Object streamObj = params.get(Constants.RESOURCE_STREAM);
            if (!(streamObj instanceof InputStream)) {
                throw new ResourceInitializationException("Expected RESOURCE_STREAM to be instance of InputStream");
            }
            initialize(new InputStreamReader((InputStream) streamObj));
        } else {
            throw new ResourceInitializationException("No file name or resource specified.");
        }
    }

    public void initialize(Class cls, String resource) throws ResourceInitializationException {
        try {
            initialize(IoUtil.read(cls, resource));
        } catch (IOException e) {
            throw new ResourceInitializationException("Failed to load resource " + resource, e);
        }
    }

    public void initialize(String fileName) throws ResourceInitializationException {
        try {
            initialize(IoUtil.read(fileName));
        } catch (IOException e) {
            throw new ResourceInitializationException("Failed to load file " + fileName, e);
        }
    }

    public void initialize(Reader reader) {
        word2feature = new HashMap<String, Feature>();
        try {
            int index = 0;
            for (String line : IoUtil.readLines(reader)) {
                //Trim new line but not white space as space may be part of feature
                while (line.length() > 0 && (line.endsWith("\n") || line.endsWith("\r"))) {
                    line = line.substring(0, line.length() - 1);
                }
                String[] pieces = line.split("\t");
                double weight = 1d;
                if (pieces.length > 1) {
                    weight = Double.parseDouble(pieces[1]);
                }
                word2feature.put(pieces[0], new Feature(index++, weight));
            }
        } catch (IOException e) {
            throw new ResourceInitializationException("Loading features failed.", e);
        }

        strings = new String[word2feature.size()];
        for (Map.Entry<String, Feature> entry : word2feature.entrySet()) {
            strings[entry.getValue().getIndex()] = entry.getKey();
        }
    }

    public int getIndex(String s) {
        Feature f = word2feature.get(s);
        return f == null ? -1 : f.getIndex();
    }

    public double getWeight(String s) {
        Feature f = word2feature.get(s);
        return f == null ? 0d : f.getWeight();
    }

    public double getWeight(int i) {
        String text = getFeatureText(i);
        return text == null ? 0d : getWeight(text);
    }

    public Feature getFeature(String s) {
        return word2feature.get(s);
    }

    public String getFeatureText(int i) {
        if (i < 0 || i >= strings.length)
            return null;
        return strings[i];
    }

    public int size() {
        return strings.length;
    }

    @Override
    public String toString() {
        return word2feature.toString();
    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Features)) return false;
        Features other = (Features) o;
        return word2feature.equals(other.word2feature) && Arrays.equals(strings, other.strings);
    }
}
