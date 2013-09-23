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

import com.groupon.nakala.analysis.CorpusSparseFormatRepresenter;
import com.groupon.nakala.db.CollectionParameters;
import com.groupon.nakala.db.FlatFileStore;
import com.groupon.nakala.db.TsvCategorizedTextCollectionReader;
import com.groupon.nakala.normalization.CaseNormalizer;
import com.groupon.nakala.normalization.MarkupRemover;
import com.groupon.nakala.normalization.NumberNormalizer;
import com.groupon.nakala.normalization.StringNormalizer;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * @author npendar@groupon.com
 */
public class OldSimpleJobFlowSpecsTest extends TestCase {

    @Test
    public void testParser1() throws Exception {
        String yml = "# This is an example of a simple job flow specification\n" +
                "# The entries collection_reader, collection_analyzer, and data_stores are required.\n" +
                "# parameters are a list of attribute and values. Values could be parameters themselves.\n" +
                "\n" +
                "collection_reader:\n" +
                "  class_name: com.groupon.nakala.db.SimpleTextCollectionReader\n" +
                "  parameters:\n" +
                "    separator: \\t\n" +
                "    text_field: 1 # 0-based index. In case of database fields, you can use field name.\n" +
                "    file_name: corpus.tsv\n" +
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

        OldSimpleJobFlowSpecs specs = new OldSimpleJobFlowSpecs();
        InputStream inputStream = new ByteArrayInputStream(yml.getBytes());
        specs.initialize(inputStream);

        assertEquals("com.groupon.nakala.db.SimpleTextCollectionReader", specs.getCollectionReader().getClass().getName());
        assertEquals("com.groupon.nakala.analysis.IdfCollectionAnalyzer", specs.getCollectionAnalyzer().getClass().getName());
        assertEquals(1, specs.getDataStores().length);
        assertEquals("com.groupon.nakala.db.FlatFileStore", specs.getDataStores()[0].getClass().getName());
        assertTrue(specs.getCollectionReaderParameters() instanceof CollectionParameters);
        assertTrue(specs.getDataStoreParameters() instanceof CollectionParameters);
        assertTrue(specs.getCollectionAnalyzerParameters() instanceof Parameters);
        assertEquals(1, (int) specs.getCollectionReaderParameters().getInt(CollectionParameters.TEXT_FIELD));
        assertEquals("corpus.tsv", specs.getCollectionReaderParameters().getString(CollectionParameters.FILE_NAME));
        assertEquals("\t", specs.getCollectionReaderParameters().getString(CollectionParameters.SEPARATOR));
        assertEquals("com.groupon.nakala.core.RegexpTokenizerStream", specs.getCollectionAnalyzerParameters().get("tokenizer").getClass().getName());
        assertEquals(1, ((List<StringNormalizer>) specs.getCollectionAnalyzerParameters().get("normalizers")).size());
        assertEquals("com.groupon.nakala.normalization.CaseNormalizer", ((List<StringNormalizer>) specs.getCollectionAnalyzerParameters().get("normalizers")).get(0).getClass().getName());
    }

    @Test
    public void testParser2() throws Exception {
        String yml = "collection_reader:\n" +
                "  # fully specified name of a CollectionReader\n" +
                "  class_name: com.groupon.nakala.db.TsvCategorizedTextCollectionReader\n" +
                "  parameters:\n" +
                "    file_name: ut_descriptions_training.tsv\n" +
                "    separator: \\t\n" +
                "    id_field: 0 # 0-based index. In case of database fields, you can use field name.\n" +
                "    label_field: 1 # 0-based index of comma-separated labels. In case of database fields, you can field name.\n" +
                "    text_field: 2 # 0-based index. In case of database fields, you can use field name.\n" +
                "\n" +
                "collection_analyzer:\n" +
                "  class_name: com.groupon.nakala.analysis.CorpusSparseFormatRepresenter\n" +
                "  parameters:\n" +
                "    representer:\n" +
                "      class_name: com.groupon.nakala.core.FeatureWeightTextRepresenter\n" +
                "      parameters:\n" +
                "        features:\n" +
                "          resource: /test_features.txt\n" +
                "        tokenizer:\n" +
                "          class_name: com.groupon.nakala.core.RegexpTokenizerStream\n" +
                "        normalizers:\n" +
                "          - class_name: com.groupon.nakala.normalization.MarkupRemover\n" +
                "          - class_name: com.groupon.nakala.normalization.CaseNormalizer\n" +
                "          - class_name: com.groupon.nakala.normalization.NumberNormalizer\n" +
                "\n" +
                "data_stores:\n" +
                "  - class_name: com.groupon.nakala.db.FlatFileStore\n" +
                "    parameters:\n" +
                "      file_name: ut_descriptions_training.dat\n";

        OldSimpleJobFlowSpecs specs = new OldSimpleJobFlowSpecs();
        InputStream inputStream = new ByteArrayInputStream(yml.getBytes());
        specs.initialize(inputStream);

        assertTrue(specs.getCollectionReader() instanceof TsvCategorizedTextCollectionReader);
        assertTrue(specs.getCollectionAnalyzer() instanceof CorpusSparseFormatRepresenter);
        assertTrue(specs.dataStores.length == 1);
        assertTrue(specs.dataStores[0] instanceof FlatFileStore);

        assertTrue(specs.getCollectionAnalyzerParameters().get("representer") instanceof FeatureWeightTextRepresenter);
        TextRepresenter representer = (TextRepresenter) specs.getCollectionAnalyzerParameters().get("representer");
        assertTrue(representer.getTokenizer() instanceof RegexpTokenizerStream);
        List<StringNormalizer> normalizers = representer.getNormalizers();
        assertNotNull(normalizers);
        assertEquals(3, normalizers.size());
        assertTrue(normalizers.get(0) instanceof MarkupRemover);
        assertTrue(normalizers.get(1) instanceof CaseNormalizer);
        assertTrue(normalizers.get(2) instanceof NumberNormalizer);
    }

    @Test
    public void testSvmTrainerSpecs() throws Exception {
        String yml = "collection_reader:\n" +
                "  class_name: com.groupon.nakala.db.TsvCategorizedTextCollectionReader\n" +
                "  parameters:\n" +
                "    file_name: keyword_training_data.tsv\n" +
                "    separator: \\t\n" +
                "    id_field: 0 # 0-based index. In case of database fields, you can use field name.\n" +
                "    label_field: 1 # 0-based index.\n" +
                "    text_field: 2 # 0-based index. In case of database fields, you can use field name.\n" +
                "\n" +
                "collection_analyzer:\n" +
                "  class_name: com.groupon.ml.svm.LibSvmTrainer\n" +
                "  parameters:\n" +
                "    find_best_parameters: true\n" +
                "    sample: 0.3\n" +
                "    # c: 1.0 # default: 1 -- can be used when find_best_parameters is false\n" +
                "    # gamma: 0.0001 # default: 1/number of features -- can be used when find_best_parameter is false\n" +
                "    representer:\n" +
                "      class_name: com.groupon.nakala.core.TfFeatureWeightTextRepresenter\n" +
                "      parameters:\n" +
                "        normalize_by_length: true\n" +
                "        features:\n" +
                "          resource: /test_features.txt\n" +
                "        tokenizer:\n" +
                "          class_name: com.groupon.nakala.core.RegexpTokenizerStream\n" +
                "        normalizers:\n" +
                "          - class_name: com.groupon.nakala.normalization.MarkupRemover\n" +
                "          - class_name: com.groupon.nakala.normalization.CaseNormalizer\n" +
                "          - class_name: com.groupon.nakala.normalization.NumberNormalizer\n" +
                "\n" +
                "data_stores:\n" +
                "  - class_name: com.groupon.nakala.db.FlatFileStore\n" +
                "    parameters:\n" +
                "      # file stem: This collection analyzer produces three files:\n" +
                "      #    filestem.model  : the svm model\n" +
                "      #    filestem.range  : value range file for scaling new data\n" +
                "      #    filestem.labels : list of labels in the training data for labeling new data\n" +
                "      file_name: keyword_training_svm_output\n";

        OldSimpleJobFlowSpecs specs = new OldSimpleJobFlowSpecs();
        InputStream inputStream = new ByteArrayInputStream(yml.getBytes());
        specs.initialize(inputStream);

        assertTrue(specs.getCollectionAnalyzerParameters().contains(Constants.FIND_BEST_PARAMETERS));
        assertTrue(specs.getCollectionAnalyzerParameters().getBoolean(Constants.FIND_BEST_PARAMETERS));
        assertEquals(specs.getCollectionAnalyzerParameters().getDouble(Constants.SAMPLE), 0.3);
    }

}
