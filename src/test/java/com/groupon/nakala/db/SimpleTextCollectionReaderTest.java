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

package com.groupon.nakala.db;

import com.groupon.nakala.core.Analyzable;
import com.groupon.nakala.core.TextContent;
import com.groupon.util.io.IoUtil;
import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;

/**
 * @author npendar@groupon.com
 */
public class SimpleTextCollectionReaderTest extends TestCase {

    String[] lines = {
            "1\tThis is line 1.",
            "2\tThis is line 2.",
            "3\tAnd here's line 3."
    };

    public void testReader() throws Exception {
        CollectionParameters cp = new CollectionParameters();
        cp.set(CollectionParameters.SEPARATOR, "\t");
        cp.set(CollectionParameters.TEXT_FIELD, 1);

        cp.set(CollectionParameters.FILE_NAME, IoUtil.createTempFile(StringUtils.join(lines, "\n")));

        SimpleTextCollectionReader cr = new SimpleTextCollectionReader();
        cr.initialize(cp);

        assertEquals(lines.length, cr.getSize());

        TextContent[] tcs = new TextContent[lines.length];
        int i = 0;
        for (Analyzable a : cr) {
            tcs[i++] = (TextContent) a;
        }
        for (i = 0; i < lines.length; ++i) {
            assertEquals(lines[i].substring(lines[i].indexOf('\t') + 1), tcs[i].getText());
        }

        // Test reset
        cr.reset();
        tcs = new TextContent[lines.length];
        i = 0;
        for (Analyzable a : cr) {
            tcs[i++] = (TextContent) a;
        }
        for (i = 0; i < lines.length; ++i) {
            assertEquals(lines[i].substring(lines[i].indexOf('\t') + 1), tcs[i].getText());
        }
    }
}
