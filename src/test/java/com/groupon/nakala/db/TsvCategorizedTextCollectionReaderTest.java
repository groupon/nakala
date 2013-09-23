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
import com.groupon.nakala.core.CategorizedTextContent;
import com.groupon.util.io.IoUtil;
import junit.framework.TestCase;
import org.apache.commons.lang3.StringUtils;

/**
 * @author npendar@groupon.com
 */
public class TsvCategorizedTextCollectionReaderTest extends TestCase {
    String[] s = {"restaurant\t1\tthis is an awesome restaurant",
            "hotel\t2\thotel intercontinental is the best hotel in the world",
            "spa\t3\tcome to bliss spa for a day of soothing massage",
            "restaurant,hotel\t2\thotel awesomely is the best hotel in the world with a restaurant"};

    public void testReader() throws Exception {
        CollectionParameters cp = new CollectionParameters();
        cp.set(CollectionParameters.LABEL_FIELD, 0);
        cp.set(CollectionParameters.ID_FIELD, 1);
        cp.set(CollectionParameters.TEXT_FIELD, 2);
        cp.set(CollectionParameters.FILE_NAME, IoUtil.createTempFile(StringUtils.join(s, '\n')));

        TsvCategorizedTextCollectionReader r = new TsvCategorizedTextCollectionReader();
        r.initialize(cp);
        assertEquals(s.length, r.getSize());

        int i = 0;
        for (Analyzable a : r) {
            CategorizedTextContent ctc = (CategorizedTextContent) a;
            String[] pieces = s[i++].split("\t");
            for (String cat : pieces[0].split(",")) {
                assertTrue(ctc.hasCategory(cat));
            }
            assertEquals(pieces[1], ctc.getId().toString());
            assertEquals(pieces[2], ctc.getText());
        }
    }
}
