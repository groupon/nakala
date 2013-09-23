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

import org.junit.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author npendar@groupon.com
 */
public class PrePostProcessorTest {

    public PrePostProcessorTest() {
    }

    private static final String[] original = {
            "This is a sentence!? You,<br/> \"my friend\" <br>call this a sentence...",
            "I can't wait to see your paragraph! :-) -- (cool parentheses)",
            "The hotel offers a 4-person jacuzzi and children under the age of two are charged $2,000 per day.",
            "John&#39;s new &lt;car&gt; is here.",
            "2-for-1 Spa Passes",
            "SomeCamelCase.Words",
            "You'll need to either take a taxi ($$$) or a very long walk to see anything else of value. "
                    + "In all, I'd say it was a relatively pleasant stay but I don't think it was worth the price.",
            "AND not to mention the romantic view from the master bedroom at night.",
            "Mold in bathroom added to the lack of charm of this Morey-abismal motel.",
            "we went there in june. we really liked the place! it was awesome.",
            "So, if you want luxury, pampering, daily housekeeping, etc. you should probably choose somewhere else.",
            "While not a luxury hotel, it is a good, clean, safe, friendly place to stay... and we'll stay there again in June 2006 on our way North."};

    private static final String[] preprocessed = {
            "This is a sentence !? UTSB You , my friend call this a sentence ... UTSB",
            "I can 't_wait to see your paragraph ! UTSB :- ) -- ( cool parentheses ) UTSB",
            "The hotel offers a UTNUM4 - person jacuzzi and children under the age of UTNUMtwo are charged $ UTNUM2,UTNUM000 per day . UTSB",
            "John 's new < car > is here . UTSB",
            "UTNUM2 - for - UTNUM1 Spa Passes UTSB",
            "Some Camel Case . UTSB Words UTSB",
            "You 'll need to either take a taxi ( $ $ $ ) or a very long walk to see anything else of value . UTSB "
                    + "In all , I 'd say it was a relatively pleasant stay but I don 't think it was worth the price . UTSB",
            "AND not_to_mention the romantic view from the master bedroom at night . UTSB",
            "Mold in bathroom added to the lack of charm of this Morey - abismal motel . UTSB",
            "we went there in june . UTSB We really liked the place ! UTSB It was awesome . UTSB",
            "So , if you want luxury , pampering , daily housekeeping , etc . you should probably choose somewhere else . UTSB",
            "While not a luxury hotel , it is a good , clean , safe , friendly place to stay ... UTSB And we 'll stay there again in June UTNUM2006 on our way North . UTSB"};

    private static final String[] postprocessed = {
            "This is a sentence!? You, my friend call this a sentence...",
            "I can't wait to see your paragraph!:-)--(cool parentheses)",
            "The hotel offers a 4-person jacuzzi and children under the age of two are charged $2,000 per day.",
            "John's new &lt;car&gt; is here.",
            "2-for-1 Spa Passes",
            "Some Camel Case. Words",
            "You'll need to either take a taxi ($$$) or a very long walk to see anything else of value. "
                    + "In all, I'd say it was a relatively pleasant stay but I don't think it was worth the price.",
            "AND not to mention the romantic view from the master bedroom at night.",
            "Mold in bathroom added to the lack of charm of this Morey-abismal motel.",
            "we went there in june. We really liked the place! It was awesome.",
            "So, if you want luxury, pampering, daily housekeeping, etc. you should probably choose somewhere else.",
            "While not a luxury hotel, it is a good, clean, safe, friendly place to stay... And we'll stay there again in June 2006 on our way North."};

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.setProperty("RVW_HOME", System.getenv("HOME") + "/projects/uptake-java/trunk/rvw");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of preprocess method, of class PrePostProcessor.
     *
     * @throws Exception
     */
    @Test
    public void testPreprocess() throws Exception {
        System.out.println("preprocess");
        PrePostProcessor instance = null;
        instance = PrePostProcessor.newInstance();

        for (int i = 0; i < original.length; ++i) {
            String result = instance.preprocess(original[i]);
            System.out.println("Original:     " + original[i]);
            System.out.println("Preprocessed: " + result);
            System.out.println(preprocessed[i].equals(result) ? "Success!\n" : "Failure!\n");
            assertEquals(preprocessed[i], result);
        }
    }

    /**
     * Test of postprocessRow method, of class PrePostProcessor.
     *
     * @throws Exception
     */
    @Test
    public void testPostprocessRow() throws Exception {
        System.out.println("postprocessRow");
        PrePostProcessor instance = null;
        instance = PrePostProcessor.newInstance();

        for (int i = 0; i < preprocessed.length; ++i) {
            ArrayList<String> words = new ArrayList<String>();
            for (String word : preprocessed[i].split("\\s+")) {
                words.add(word);
            }
            ExtractedReviewRecord row = new ExtractedReviewRecord(0, 0, 15, 0, "test", words, "title", ExtractedReviewRecord.TYPE_TITLE);
            instance.postprocessRow(row, 256, 256, 100);
            System.out.println("Preprocessed:  " + preprocessed[i]);
            System.out.println("Postprocessed: " + row.getQuote());
            System.out.println(postprocessed[i].equals(row.getQuote()) ? "Success!\n" : "Failure!\n");
            assertEquals(postprocessed[i], row.getQuote());
        }
    }

    @Test
    public void testHighlighting() throws Exception {
        String pre = "This is a " + PrePostProcessor.HIGHLIGHTSTART + "test" + PrePostProcessor.HIGHLIGHTEND +
                " plus another " + PrePostProcessor.HIGHLIGHTSTART + "test" + PrePostProcessor.HIGHLIGHTEND +
                " and the last " + PrePostProcessor.HIGHLIGHTSTART + "test" + PrePostProcessor.HIGHLIGHTEND;
        PrePostProcessor ppp = PrePostProcessor.newInstance();
        List<String> words = new ArrayList<String>();
        for (String w : pre.split(" ")) {
            words.add(w);
        }
        ExtractedReviewRecord row = new ExtractedReviewRecord(0, 0, 15, 0, "test", words, "title", ExtractedReviewRecord.TYPE_CONTENT);
        ppp.postprocessRow(row, 512, 512, 512);
        Assert.assertEquals("This is a test plus another test and the last test", row.getQuote());
        Assert.assertEquals("This is a <B>test</B> plus another <B>test</B> and the last <B>test</B>", row.getAnnotatedQuote());
        for (HitSpan span : row.getHighlightSpans()) {
            Assert.assertEquals("test", row.getQuote().substring(span.getStart(), span.getEnd()));
        }
    }
}
