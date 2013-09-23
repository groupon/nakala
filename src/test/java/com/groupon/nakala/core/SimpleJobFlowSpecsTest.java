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

import com.groupon.util.io.IoUtil;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author npendar@groupon.com
 */
public class SimpleJobFlowSpecsTest extends TestCase {

    @Test
    public void testParser1() throws Exception {
        String corpus = "\ttext content 1\n\ttext content2\n";
        String fileName = IoUtil.createTempFile(corpus);

        String yml = "# This is an example of a simple job flow specification\n" +
                "# The entries collection_reader, collection_analyzer, and data_stores are required.\n" +
                "# parameters are a list of attribute and values. Values could be parameters themselves.\n" +
                "\n" +
                "collection_reader:\n" +
                "  class_name: com.groupon.nakala.db.SimpleTextCollectionReader\n" +
                "  parameters:\n" +
                "    separator: \\t\n" +
                "    text_field: 1 # 0-based index. In case of database fields, you can use field name.\n" +
                "    file_name: " + fileName + "\n" +
                "\n" +
                "collection_analyzer:\n" +
                "  class_name: com.groupon.nakala.analysis.IdfCollectionAnalyzer\n" +
                "  parameters:\n" +
                "    tokenizer: \n" +
                "      class_name: com.groupon.nakala.core.RegexpTokenizerStream\n" +
                "    normalizers:\n" +
                "      - class_name: com.groupon.nakala.normalization.CaseNormalizer\n" +
                "\n" +
                "data_stores:\n" +
                "    - class_name: com.groupon.nakala.db.FlatFileStore\n" +
                "      parameters:\n" +
                "        file_name: idfs.tsv\n";

        SimpleJobFlowSpecs specs = new SimpleJobFlowSpecs();
        InputStream inputStream = new ByteArrayInputStream(yml.getBytes());
        specs.initialize(inputStream);

        assertEquals("com.groupon.nakala.db.SimpleTextCollectionReader", specs.getCollectionReader().getClass().getName());
        assertEquals("com.groupon.nakala.analysis.IdfCollectionAnalyzer", specs.getCollectionAnalyzer().getClass().getName());
        assertEquals(1, specs.getDataStores().length);
        assertEquals("com.groupon.nakala.db.FlatFileStore", specs.getDataStores()[0].getClass().getName());
    }
}
