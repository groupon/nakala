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

import junit.framework.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author npendar@groupon.com
 */
public class ShinglesTokenizerStreamTest {
    String s = "this is a test";
    String[] shingles = {"this", "his ", "is i", "s is", " is ", "is a", "s a ", " a t", "a te", " tes", "test"};

    @Test
    public void testNext() throws Exception {
        TokenizerStream tokenizer = new ShinglesTokenizerStream();
        tokenizer.setText(s);
        for (int i = 0; i < shingles.length; ++i) {
            Assert.assertEquals(shingles[i], tokenizer.next());
        }
        Assert.assertNull(tokenizer.next());
    }

    @Test
    public void testGetUniqueTokens() throws Exception {
        Set<String> expected = new HashSet<String>();
        for (String shingle : shingles) {
            expected.add(shingle);
        }

        TokenizerStream tokenizer = new ShinglesTokenizerStream();
        Set<String> actual = tokenizer.getUniqueTokens(s);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetTokens() throws Exception {
        TokenizerStream tokenizer = new ShinglesTokenizerStream();
        List<String> tokens = tokenizer.getTokens(s);
        Assert.assertEquals(shingles.length, tokens.size());
        for (int i = 0; i < shingles.length; ++i) {
            Assert.assertEquals(shingles[i], tokens.get(i));
        }
    }
}
