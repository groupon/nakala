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

import com.groupon.nakala.core.Id;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.*;

/**
 * @author npendar@groupon.com
 */
public class ClassificationMetricsTest extends TestCase {

    @Test
    public void testMetrics() throws Exception {
        double threshold = 0.5;
        List<String> categories = new LinkedList<String>();
        Map<Id, Set<String>> id2truecategories = new HashMap<Id, Set<String>>();
        ClassificationAnalysisCollector id2predictions = new ClassificationAnalysisCollector();

        categories.add("hotel");
        categories.add("restaurant");
        categories.add("golf_course");

        Id id1 = new Id(1);
        Id id2 = new Id(2);
        Id id3 = new Id(3);

        Set<String> cats = new HashSet<String>();
        cats.add("hotel");
        id2truecategories.put(id1, cats);

        cats = new HashSet<String>();
        cats.add("restaurant");
        id2truecategories.put(id2, cats);

        cats = new HashSet<String>();
        cats.add("golf_course");
        id2truecategories.put(id3, cats);


        Map<String, Double> classification = new HashMap<String, Double>();
        classification.put("hotel", 0.6);
        classification.put("restaurant", 0.4);
        classification.put("golf_course", 0.2);
        id2predictions.put(id1, new ClassificationAnalysis(id1, classification));

        classification = new HashMap<String, Double>();
        classification.put("hotel", 0.6);
        classification.put("restaurant", 0.8);
        classification.put("golf_course", 0.2);
        id2predictions.put(id2, new ClassificationAnalysis(id1, classification));

        classification = new HashMap<String, Double>();
        classification.put("hotel", 0.2);
        classification.put("restaurant", 0.1);
        classification.put("golf_course", 0.45);
        id2predictions.put(id3, new ClassificationAnalysis(id3, classification));

        ClassificationMetrics metrics = new ClassificationMetrics(threshold, categories, id2truecategories, id2predictions);

        final double TWO_THIRDS = 2d / 3d;
        final double EPSILON = 0.000001;

        assertEquals(1, metrics.getTp("hotel"));
        assertEquals(1, metrics.getFp("hotel"));
        assertEquals(0, metrics.getFn("hotel"));
        assertEquals(0.5, metrics.getP("hotel"));
        assertEquals(1d, metrics.getR("hotel"));
        assertEquals(TWO_THIRDS, metrics.getF1("hotel"), EPSILON);

        assertEquals(1, metrics.getTp("restaurant"));
        assertEquals(0, metrics.getFp("restaurant"));
        assertEquals(0, metrics.getFn("restaurant"));
        assertEquals(1d, metrics.getP("restaurant"));
        assertEquals(1d, metrics.getR("restaurant"));
        assertEquals(1d, metrics.getF1("restaurant"));


        assertEquals(0, metrics.getTp("golf_course"));
        assertEquals(0, metrics.getFp("golf_course"));
        assertEquals(1, metrics.getFn("golf_course"));
        assertEquals(0d, metrics.getP("golf_course"));
        assertEquals(0d, metrics.getR("golf_course"));
        assertEquals(0d, metrics.getF1("golf_course"));


        assertEquals(0.5, metrics.getMacroAvgP());
        assertEquals(TWO_THIRDS, metrics.getMacroAvgR(), EPSILON);
        assertEquals(0.5555555555555555, metrics.getMarcoAvgF1(), EPSILON);

        assertEquals(TWO_THIRDS, metrics.getMicroAvgP(), EPSILON);
        assertEquals(TWO_THIRDS, metrics.getMicroAvgR(), EPSILON);
        assertEquals(TWO_THIRDS, metrics.getMicroAvgF1(), EPSILON);
        assertEquals(0.5, metrics.getAvgAccuracy(), EPSILON);
    }
}
