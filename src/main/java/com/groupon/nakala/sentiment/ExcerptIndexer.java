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

import com.groupon.nakala.core.SimpleTitledTextContent;
import com.groupon.nakala.core.TitledContentArray;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LimitTokenCountAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author npendar@groupon.com
 */
public final class ExcerptIndexer {

    public static final String CONTENTS = "c";
    public static final String ORIGINAL = "o";
    public static final String DESC_ID = "d";
    public static final String TITLE = "t";
    public static final String TITLE_ORIGINAL = "u";
    public static final String PUNCT = "UTPUNCT";

    private static final Logger log = Logger.getLogger(ExcerptIndexer.class);
    private static final Pattern punctPat = Pattern.compile("[.?!,();:]\\p{Punct}*");
    private static final int MAX_TOKENS = 5000;

    private Directory indexDir;
    private Analyzer analyzer;
    private PrePostProcessor pp;
    private Matcher punctMatcher = punctPat.matcher("");

    public ExcerptIndexer(Analyzer a, PrePostProcessor pp) {
        this.analyzer = new LimitTokenCountAnalyzer(a, MAX_TOKENS);
        this.pp = pp;
    }

    public Directory getIndex() {
        return indexDir;
    }

    public IndexSearcher getIndexSearcher() throws IOException {
        IndexSearcher is = null;
        try {
            is = new IndexSearcher(IndexReader.open(indexDir));
        } catch (Exception e) {
            log.error("Problem reading index.", e);
        }
        return is;
    }

    public void index(TitledContentArray titledContentArray)
            throws Exception, IOException {
        indexDir = new RAMDirectory();
        try {
            IndexWriter indexWriter = new IndexWriter(indexDir, new IndexWriterConfig(Version.LUCENE_36, analyzer));
            for (SimpleTitledTextContent content : titledContentArray) {
                Document doc = new Document();
                String title = pp.preprocess(content.getTitle()).replace(
                        PrePostProcessor.SENTENCE_BOUNDARY,
                        "").trim();

                String contentsOriginal = pp.preprocess(content.getText());
                String contents = contentsOriginal.toLowerCase();
                punctMatcher.reset(contents);
                contents = punctMatcher.replaceAll(PUNCT).trim();

                doc.add(new Field(DESC_ID,
                        content.getId().toString(),
                        Field.Store.YES,
                        Field.Index.NOT_ANALYZED));
                doc.add(new Field(TITLE,
                        title.toLowerCase().trim(),
                        Field.Store.YES,
                        Field.Index.ANALYZED));
                doc.add(new Field(TITLE_ORIGINAL,
                        title,
                        Field.Store.YES,
                        Field.Index.NOT_ANALYZED));
                doc.add(new Field(CONTENTS,
                        contents,
                        Field.Store.YES,
                        Field.Index.ANALYZED));
                doc.add(new Field(ORIGINAL,
                        contentsOriginal,
                        Field.Store.YES,
                        Field.Index.ANALYZED));
                indexWriter.addDocument(doc);
            }
            indexWriter.close();
        } catch (Exception e) {
            log.error("Indexing failed.", e);
        }
    }
}
