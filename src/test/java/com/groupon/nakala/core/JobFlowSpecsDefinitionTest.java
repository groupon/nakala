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

import java.io.ByteArrayInputStream;

/**
 * @author npendar@groupon.com
 */
public class JobFlowSpecsDefinitionTest {

    private static final String SPECS =
            "top:\n" +
                    "  value_type: map\n" +
                    "  required:\n" +
                    "    - collection_reader\n" +
                    "    - collection_analyzer\n" +
                    "    - data_stores\n" +
                    "collection_reader:\n" +
                    "  value_type: map\n" +
                    "  required:\n" +
                    "    - class_name\n" +
                    "  parameters:\n" +
                    "    - collection_name\n" +
                    "    - db_name\n" +
                    "    - file_name\n" +
                    "    - host\n" +
                    "    - id_field\n" +
                    "    - label_field\n" +
                    "    - port\n" +
                    "    - separator\n" +
                    "    - table_name\n" +
                    "    - text_field\n" +
                    "    - title_field\n" +
                    "collection_analyzer:\n" +
                    "  value_type: map\n" +
                    "  required:\n" +
                    "    - class_name\n" +
                    "  parameters:\n" +
                    "    - analyzer\n" +
                    "    - block_filter\n" +
                    "    - find_best_parameters\n" +
                    "    - min_df\n" +
                    "    - normalizers\n" +
                    "    - pass_filter\n" +
                    "    - representer\n" +
                    "    - stopwords\n" +
                    "    - tokenizer\n" +
                    "    - max_threshold\n" +
                    "    - min_threshold\n" +
                    "    - threshold_step\n" +
                    "data_stores:\n" +
                    "  value_type: list\n" +
                    "  list_type: data_store\n" +
                    "data_store:\n" +
                    "  value_type: map\n" +
                    "  required:\n" +
                    "    - class_name\n" +
                    "  parameters:\n" +
                    "    - collection_name\n" +
                    "    - db_name\n" +
                    "    - file_name\n" +
                    "    - host\n" +
                    "    - port\n" +
                    "    - table_name\n" +
                    "normalizer:\n" +
                    "  value_type: map\n" +
                    "  required:\n" +
                    "    - class_name\n" +
                    "parameters:\n" +
                    "  value_type: map\n" +
                    "class_name:\n" +
                    "  value_type: string\n" +
                    "db_name:\n" +
                    "  value_type: string\n" +
                    "host:\n" +
                    "  value_type: string\n" +
                    "port:\n" +
                    "  value_type: string\n" +
                    "table_name:\n" +
                    "  value_type: string\n" +
                    "collection_name:\n" +
                    "  value_type: string\n" +
                    "file_name:\n" +
                    "  value_type: string\n" +
                    "separator:\n" +
                    "  value_type: string\n" +
                    "id_field:\n" +
                    "  value_type: string\n" +
                    "label_field:\n" +
                    "  value_type: string\n" +
                    "text_field:\n" +
                    "  value_type: string\n" +
                    "title_field:\n" +
                    "  value_type: string\n" +
                    "analyzer:\n" +
                    "  value_type: map\n" +
                    "block_filter:\n" +
                    "  value_type: map\n" +
                    "find_best_parameters:\n" +
                    "  value_type: boolean\n" +
                    "min_df:\n" +
                    "  value_type: integer\n" +
                    "normalizers:\n" +
                    "  value_type: list\n" +
                    "  list_type: normalizer\n" +
                    "pass_filter:\n" +
                    "  value_type: map\n" +
                    "representer:\n" +
                    "  value_type: map\n" +
                    "stopwords:\n" +
                    "  value_type: map\n" +
                    "tokenizer:\n" +
                    "  value_type: map\n" +
                    "max_threshold:\n" +
                    "  value_type: double\n" +
                    "min_threshold:\n" +
                    "  value_type: double\n" +
                    "threshold_step:\n" +
                    "  value_type: double\n" +
                    "domains:\n" +
                    "  value_type: list\n" +
                    "  list_type: string\n";

    @Test
    public void testJobFlowSpecsDefinition() throws Exception {
        JobFlowSpecsDefinition definition = new JobFlowSpecsDefinition();
        definition.initialize(new ByteArrayInputStream(SPECS.getBytes()));

        Assert.assertEquals(JobFlowSpecsDefinition.MAP, definition.getValueType("top"));
        Assert.assertEquals(3, definition.getRequired("top").size());
        Assert.assertTrue(definition.getRequired("top").contains(SimpleJobFlowSpecs.COLLECTION_READER));
        Assert.assertTrue(definition.getRequired("top").contains(SimpleJobFlowSpecs.COLLECTION_ANALYZER));
        Assert.assertTrue(definition.getRequired("top").contains(SimpleJobFlowSpecs.DATA_STORES));

        Assert.assertEquals(JobFlowSpecsDefinition.MAP, definition.getValueType("collection_reader"));
        Assert.assertEquals(1, definition.getRequired("collection_reader").size());
        Assert.assertTrue(definition.getRequired("collection_reader").contains(JobFlowSpecsDefinition.CLASS_NAME));
        Assert.assertEquals(11, definition.getParameters("collection_reader").size());

        Assert.assertEquals(JobFlowSpecsDefinition.MAP, definition.getValueType("collection_analyzer"));
        Assert.assertEquals(JobFlowSpecsDefinition.LIST, definition.getValueType("data_stores"));
        Assert.assertEquals(JobFlowSpecsDefinition.STRING, definition.getValueType("class_name"));
        Assert.assertEquals(JobFlowSpecsDefinition.DOUBLE, definition.getValueType("max_threshold"));
        Assert.assertEquals(JobFlowSpecsDefinition.LIST, definition.getValueType("normalizers"));
        Assert.assertEquals(JobFlowSpecsDefinition.LIST, definition.getValueType("domains"));

        Assert.assertEquals(JobFlowSpecsDefinition.STRING, definition.getListType("domains"));
        Assert.assertEquals("normalizer", definition.getListType("normalizers"));
    }
}
