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

import junit.framework.TestCase;
import org.junit.Test;

import java.io.StringReader;

/**
 * @author npendar@groupon.com
 */
public class FeaturesTest extends TestCase {
    @Test
    public void testInitializeWithResource() throws Exception {
        Features features = new Features();
        features.initialize(getClass(), "/test_features.txt");

        assertEquals(0, (int) features.getIndex("this"));
        assertEquals(1, (int) features.getIndex("is"));
        assertEquals(2, (int) features.getIndex("a"));
        assertEquals(3, (int) features.getIndex("set"));
        assertEquals(4, (int) features.getIndex("of"));
        assertEquals(5, (int) features.getIndex("test"));
        assertEquals(6, (int) features.getIndex("features"));
    }

    @Test
    public void testInitializeWithReader() throws Exception {
        String feats = "this\n" +
                "is\n" +
                "a\n" +
                "set\n" +
                "of\n" +
                "test\n" +
                "features\n";
        Features features = new Features();
        features.initialize(new StringReader(feats));

        assertEquals(0, (int) features.getIndex("this"));
        assertEquals(1, (int) features.getIndex("is"));
        assertEquals(2, (int) features.getIndex("a"));
        assertEquals(3, (int) features.getIndex("set"));
        assertEquals(4, (int) features.getIndex("of"));
        assertEquals(5, (int) features.getIndex("test"));
        assertEquals(6, (int) features.getIndex("features"));
    }

    @Test
    public void testGetFeature() throws Exception {
        String feats = "this\n" +
                "is\n" +
                "a\n" +
                "set\n" +
                "of\n" +
                "test\n" +
                "features\n";
        Features features = new Features();
        features.initialize(new StringReader(feats));

        assertEquals("this", features.getFeatureText(0));
        assertEquals("is", features.getFeatureText(1));
        assertEquals("a", features.getFeatureText(2));
        assertEquals("set", features.getFeatureText(3));
        assertEquals("of", features.getFeatureText(4));
        assertEquals("test", features.getFeatureText(5));
        assertEquals("features", features.getFeatureText(6));
    }


    @Test
    public void testGetWeight() throws Exception {
        String feats = "this\n" +
                "is\n" +
                "a\n" +
                "set\n" +
                "of\n" +
                "test\n" +
                "features\n";
        Features features = new Features();
        features.initialize(new StringReader(feats));

        assertEquals(1d, features.getWeight("this"));
        assertEquals(1d, features.getWeight("is"));
        assertEquals(1d, features.getWeight("a"));

        feats = "this\t0.1\n" +
                "is\t0.2\n" +
                "a\t0.3\n" +
                "set\t0.4\n";

        features = new Features();
        features.initialize(new StringReader(feats));

        assertEquals(0.1, features.getWeight("this"));
        assertEquals(0.1, features.getWeight(0));
        assertEquals(0.2, features.getWeight("is"));
        assertEquals(0.2, features.getWeight(1));
        assertEquals(0.3, features.getWeight("a"));
        assertEquals(0.3, features.getWeight(2));
        assertEquals(0.4, features.getWeight("set"));
        assertEquals(0.4, features.getWeight(3));
    }
}
