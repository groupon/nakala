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

import java.util.LinkedList;
import java.util.List;

/**
 * @author npendar@groupon.com
 */
public class RegExpBlockFilterTest extends TestCase {
    @Test
    public void testIsBlocked() throws Exception {
        List<String> pats = new LinkedList<String>();
        pats.add("mexican");
        pats.add("taco");

        TextContent[] shouldBlock = new TextContent[]{
                new IdentifiableTextContent(new Id(0), "This is a Mexican restaurant."),
                new IdentifiableTextContent(new Id(0), "We serve taco.")
        };

        TextContent[] shouldNotBlock = new TextContent[]{
                new IdentifiableTextContent(new Id(0), "We are the premier seafood place in the world."),
                new IdentifiableTextContent(new Id(0), "We serve fish!")
        };

        BlockFilter blockFilter = new RegexpBlockFilter();
        blockFilter.initialize(pats);

        for (TextContent textContent : shouldBlock) {
            assertTrue(blockFilter.blocks(textContent));
        }

        for (TextContent textContent : shouldNotBlock) {
            assertFalse(blockFilter.blocks(textContent));
        }
    }
}
