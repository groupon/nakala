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

import com.groupon.nakala.normalization.CaseNormalizer;
import com.groupon.nakala.normalization.StringNormalizer;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author npendar@groupon.com
 */
public class RegexpTokenizerStreamTest extends TestCase {
    @Test
    public void testTokens() throws Exception {
        TokenizerStream tokenizer = new RegexpTokenizerStream();
        tokenizer.setText("This, my friend, is a test.");
        assertEquals("This", tokenizer.next());
        assertEquals("my", tokenizer.next());
        assertEquals("friend", tokenizer.next());
        assertEquals("is", tokenizer.next());
        assertEquals("a", tokenizer.next());
        assertEquals("test", tokenizer.next());
        assertNull(tokenizer.next());

        tokenizer.setText("How is my tokenizer?");
        assertEquals("How", tokenizer.next());
        assertEquals("is", tokenizer.next());
        assertEquals("my", tokenizer.next());
        assertEquals("tokenizer", tokenizer.next());
        assertNull(tokenizer.next());
    }

    @Test
    public void testgetUniqueTokens() throws Exception {
        String text = "This, is A test (sentencE)!! Sentence test this IS.";
        List<StringNormalizer> normalizers = new LinkedList<StringNormalizer>();
        normalizers.add(new CaseNormalizer());

        TokenizerStream tokenizer = new RegexpTokenizerStream();

        Set<String> uniques = tokenizer.getUniqueTokens(text, normalizers);
        assertEquals(5, uniques.size());
        assertTrue(uniques.contains("this"));
        assertTrue(uniques.contains("is"));
        assertTrue(uniques.contains("a"));
        assertTrue(uniques.contains("test"));
        assertTrue(uniques.contains("sentence"));

        uniques = tokenizer.getUniqueTokens(text);
        assertEquals(8, uniques.size());
        assertTrue(uniques.contains("This"));
        assertTrue(uniques.contains("is"));
        assertTrue(uniques.contains("A"));
        assertTrue(uniques.contains("test"));
        assertTrue(uniques.contains("sentencE"));
        assertTrue(uniques.contains("Sentence"));
        assertTrue(uniques.contains("this"));
        assertTrue(uniques.contains("IS"));
    }

    @Test
    public void testGetTokens() throws Exception {
        String text = "This, is A test (sentencE)!! Sentence test this IS.";
        List<StringNormalizer> normalizers = new LinkedList<StringNormalizer>();
        normalizers.add(new CaseNormalizer());

        TokenizerStream tokenizer = new RegexpTokenizerStream();

        String[] tokens1 = {"this", "is", "a", "test", "sentence", "sentence", "test", "this", "is"};
        List<String> tokens = tokenizer.getTokens(text, normalizers);

        assertEquals(tokens1.length, tokens.size());
        for (int i = 0; i < tokens1.length; ++i) {
            assertEquals(tokens1[i], tokens.get(i));
        }

        String[] tokens2 = {"This", "is", "A", "test", "sentencE", "Sentence", "test", "this", "IS"};
        tokens = tokenizer.getTokens(text);

        assertEquals(tokens2.length, tokens.size());
        for (int i = 0; i < tokens2.length; ++i) {
            assertEquals(tokens2[i], tokens.get(i));
        }
    }
}
