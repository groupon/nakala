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

import com.groupon.nakala.core.Id;
import com.groupon.nakala.core.Review;
import com.groupon.nakala.core.TitledContentArray;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.Version;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author npendar@groupon.com
 */
public class ExcerptIndexerTest {

    @Test
    public void testIndex() throws Exception {
        String SB = PrePostProcessor.SENTENCE_BOUNDARY;
        String sb = SB.toLowerCase();
        String PUNCT = ExcerptIndexer.PUNCT;
        ExcerptIndexer indexer = new ExcerptIndexer(new WhitespaceAnalyzer(Version.LUCENE_36), PrePostProcessor.newInstance());
        TitledContentArray ses = new TitledContentArray();
        ses.add(new Review(new Id(1), "First Title", "First contents are here. This is the second sentence in first content.", 1.0));
        ses.add(new Review(new Id(3), "Second Title", "Second contents are here. This is the second sentence in second content.", 0.75));
        indexer.index(ses);
        IndexSearcher is = indexer.getIndexSearcher();
        assertEquals(2, is.maxDoc());
        Document doc = is.doc(0);
        assertEquals("1", doc.get(ExcerptIndexer.DESC_ID));
        System.out.println("Contents: " + doc.get(ExcerptIndexer.CONTENTS));
        System.out.println("Original: " + doc.get(ExcerptIndexer.ORIGINAL));
        assertEquals("first contents are here " + PUNCT + ' ' + sb + " this is the second sentence in first content " + PUNCT + ' ' + sb, doc.get(ExcerptIndexer.CONTENTS));
        assertEquals("First contents are here . " + SB + " This is the second sentence in first content . " + SB, doc.get(ExcerptIndexer.ORIGINAL));
        assertEquals("first title", doc.get(ExcerptIndexer.TITLE));
        assertEquals("First Title", doc.get(ExcerptIndexer.TITLE_ORIGINAL));

        doc = is.doc(1);
        assertEquals("3", doc.get(ExcerptIndexer.DESC_ID));
        System.out.println("Contents: " + doc.get(ExcerptIndexer.CONTENTS));
        System.out.println("Original: " + doc.get(ExcerptIndexer.ORIGINAL));
        assertEquals("second contents are here " + PUNCT + ' ' + sb + " this is the second sentence in second content " + PUNCT + ' ' + sb, doc.get(ExcerptIndexer.CONTENTS));
        assertEquals("Second contents are here . " + SB + " This is the second sentence in second content . " + SB, doc.get(ExcerptIndexer.ORIGINAL));
        assertEquals("second title", doc.get(ExcerptIndexer.TITLE));
        assertEquals("Second Title", doc.get(ExcerptIndexer.TITLE_ORIGINAL));
    }
}
