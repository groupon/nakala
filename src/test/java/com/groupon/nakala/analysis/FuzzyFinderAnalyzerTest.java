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
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author npendar@groupon.com
 */
public class FuzzyFinderAnalyzerTest {
    @Test
    public void testClean() throws Exception {
        FuzzyFinderAnalyzer analyzer = new FuzzyFinderAnalyzer();
        analyzer.initialize(null);

        String[][] pairs = {
                {"This ** is ** a name!!!", "this is a name"},
                {"This ** is ** not (a) name.", "this is not a name"},
                {"This is a phone number:  (+1) 123 456-7890.", "this is a phone number 11234567890"}
        };

        for (int i = 0; i < pairs.length; ++i) {
            Assert.assertEquals(pairs[i][1], analyzer.clean(pairs[i][0]));
        }
    }

    @Test
    public void testCleanPlace() throws Exception {

        TitledContentArray tca = new TitledContentArray();
        tca.add(new SimpleTitledTextContent(new Id("http://www.hyatt.com/index.html"),
                "Hyatt Hotel San Diego", "Welcome to the Hyatt website. Our address is 123 Main Street.\n\n" +
                "San Diego CA, 92101\nPhone: (123) 456-7890. Copyright Hyatt."));

        String cleanTitle = "hyatt hotel san diego";
        String cleanText = "welcome to the hyatt website our address is 123 main " +
                "street san diego ca 92101 phone 1234567890 copyright hyatt";

        Place place = new Place().setId(new Id("1"))
                .setName("** dupe ** Hyatt Hotel LLC San Diego")
                .setAddress("123 Main St.")
                .setLocality("San Diego")
                .setRegion("California")
                .setPostalCode("92101")
                .setPhone("+1 123 456-7890")
                .setUrl("http://www.hyatt.com")
                .setDescriptions(tca);
        FuzzyFinderAnalyzer analyzer = new FuzzyFinderAnalyzer();
        analyzer.initialize(null);

        Place cleanPlace = analyzer.cleanPlace(place);
        Assert.assertEquals(new Id("1"), cleanPlace.getId());
        Assert.assertEquals("hyatt hotel", cleanPlace.getName());
        Assert.assertEquals("123 main st", cleanPlace.getAddress());
        Assert.assertEquals("san diego", cleanPlace.getLocality());
        Assert.assertEquals("california", cleanPlace.getRegion());
        Assert.assertEquals("92101", cleanPlace.getPostalCode());
        Assert.assertEquals("11234567890", cleanPlace.getPhone());
        Assert.assertEquals("http://www.hyatt.com", cleanPlace.getUrl());
        Assert.assertEquals(cleanTitle, cleanPlace.getDescriptions().get(0).getTitle());
        Assert.assertEquals(cleanText, cleanPlace.getDescriptions().get(0).getText());
    }

    @Test
    public void testAnalyze() throws Exception {
        FuzzyFinderAnalyzer analyzer = new FuzzyFinderAnalyzer();
        analyzer.initialize(null);

        TitledContentArray tca = new TitledContentArray();
        tca.add(new SimpleTitledTextContent(new Id("http://www.hyatt.com/index.html"),
                "Hyatt Hotel San Diego", "Welcome to the Hyatt website. Our address is 123 Main Street.\n\n" +
                "San Diego CA, 92101\nPhone: (123) 456-7890. Copyright Hyatt."));

        Place place = new Place().setId(new Id("1"))
                .setName("** dupe ** Hyatt San Diego")
                .setAddress("123 Main St.")
                .setLocality("San Diego")
                .setRegion("California")
                .setPostalCode("92101")
                .setPhone("+1 123 456-7890")
                .setUrl("http://www.hyatt.com")
                .setDescriptions(tca);

        MultiScoreAnalysis analysis = (MultiScoreAnalysis) analyzer.analyze(place);
        Assert.assertEquals(1d, analysis.get(FuzzyFinderAnalyzer.DOMAIN_NAME_SIMILARITY));
        Assert.assertEquals(1d, analysis.get(FuzzyFinderAnalyzer.LOCALITY));
        Assert.assertEquals(0.875, analysis.get(FuzzyFinderAnalyzer.PHONE));
        Assert.assertEquals(1d, analysis.get(FuzzyFinderAnalyzer.ADDRESS));
        Assert.assertEquals(1d, analysis.get(FuzzyFinderAnalyzer.NAME));
        Assert.assertEquals(1d, analysis.get(FuzzyFinderAnalyzer.POSTAL_CODE));
    }

    @Test
    public void testShinglesFound() throws Exception {
        FuzzyFinderAnalyzer analyzer = new FuzzyFinderAnalyzer();
        analyzer.initialize(null);

        ShinglesTokenizerStream shinglesTokenizer = new ShinglesTokenizerStream();
        shinglesTokenizer.setShingleSize(4);

        Assert.assertEquals(1.0, analyzer.shinglesFound(shinglesTokenizer.getUniqueTokens("abcdefg"),
                shinglesTokenizer.getUniqueTokens("abcdefg")));
        Assert.assertEquals(1.0, analyzer.shinglesFound(shinglesTokenizer.getUniqueTokens("bcde"),
                shinglesTokenizer.getUniqueTokens("abcdef")));
        Assert.assertEquals(0.5, analyzer.shinglesFound(shinglesTokenizer.getUniqueTokens("abcdefghi"),
                shinglesTokenizer.getUniqueTokens("abcdef")));
        Assert.assertEquals(0.0, analyzer.shinglesFound(shinglesTokenizer.getUniqueTokens("ghijkl"),
                shinglesTokenizer.getUniqueTokens("abcdef")));
    }
}
