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

import com.groupon.nakala.analysis.CollectionAnalyzer;
import com.groupon.nakala.analysis.Initializable;
import com.groupon.nakala.db.CollectionParameters;
import com.groupon.nakala.db.CollectionReader;
import com.groupon.nakala.db.DataStore;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author npendar@groupon.com
 */
public class SimpleJobFlowSpecs implements JobFlowSpecs {
    protected static final Logger logger = Logger.getLogger(MultiThreadedJobFlowSpecs.class);

    protected static final String COLLECTION_READER = "collection_reader";
    protected static final String COLLECTION_ANALYZER = "collection_analyzer";
    protected static final String DATA_STORES = "data_stores";

    private static final String definitionFile = "/nakala/jobFlowSpecsDefinition.yml";

    protected CollectionReader collectionReader;
    protected DataStore[] dataStores;

    private CollectionAnalyzer collectionAnalyzer;

    @Override
    public void initialize(InputStream inputStream) throws ResourceInitializationException {
        JobFlowSpecsDefinition specsDefinition = new JobFlowSpecsDefinition();
        specsDefinition.initialize(definitionFile);

        Yaml yaml = new Yaml();
        try {
            Map<String, Object> specs = (Map<String, Object>) yaml.load(inputStream);
            List<String> errors = new LinkedList<String>();
            validate(specsDefinition, specs, JobFlowSpecsDefinition.TOP, errors);

            if (!errors.isEmpty()) {
                throw new ResourceInitializationException("Job flow specification validation failed.\n" +
                        StringUtils.join(errors, "\n"));
            }
            collectionReader = (CollectionReader) parseSpecs(
                    specsDefinition,
                    specs.get(SimpleJobFlowSpecs.COLLECTION_READER),
                    SimpleJobFlowSpecs.COLLECTION_READER);
            collectionAnalyzer = (CollectionAnalyzer) parseSpecs(
                    specsDefinition,
                    specs.get(SimpleJobFlowSpecs.COLLECTION_ANALYZER),
                    SimpleJobFlowSpecs.COLLECTION_ANALYZER);
            List<DataStore> dataStoreList = (List<DataStore>) parseSpecs(
                    specsDefinition,
                    specs.get(SimpleJobFlowSpecs.DATA_STORES),
                    SimpleJobFlowSpecs.DATA_STORES);
            dataStores = dataStoreList.toArray(new DataStore[1]);
        } catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    protected Object parseSpecs(JobFlowSpecsDefinition definition, Object specs, String key) {
        logger.trace("Parsing key " + key);
        Object ret = null;

        if (specs instanceof List) {
            ret = parseList(definition, (List<Object>) specs, key);
        } else if (specs instanceof Map) {
            ret = parseMapValue(definition, (Map<String, Object>) specs);
        } else {
            ret = specs;
        }

        return ret;
    }

    protected List<Object> parseList(JobFlowSpecsDefinition definition, List<Object> specs, String key) {
        logger.trace("Parsing list " + specs);
        List<Object> items = new LinkedList<Object>();

        String listType = definition.getListType(key);

        if (listType.equals(JobFlowSpecsDefinition.STRING)) {
            items = specs;
        } else {
            for (Object item : specs) {
                items.add(parseSpecs(definition, item, key));
            }
        }

        return items;
    }

    protected Object parseMapValue(JobFlowSpecsDefinition definition, Map<String, Object> map) {
        logger.debug("Parsing map " + map);
        Object ret = null;

        // class_name is parsed here; other required attributes are handled in the initialize method above
        if (map.containsKey(JobFlowSpecsDefinition.CLASS_NAME)) {
            try {
                logger.debug("Instantiating class " + map.get(JobFlowSpecsDefinition.CLASS_NAME));
                ret = Class.forName((String) map.get(JobFlowSpecsDefinition.CLASS_NAME)).newInstance();
            } catch (Exception ex) {
                throw new ResourceInitializationException(ex);
            }
        } else {
            logger.debug("no class_name attribute in " + map);
        }

        if (ret instanceof Initializable || ret instanceof CollectionReader || ret instanceof DataStore) {
            logger.debug("Parsing parameters" + map);
            Parameters params = null;
            if (ret instanceof CollectionReader || ret instanceof DataStore) {
                params = new CollectionParameters();
            } else {
                params = new Parameters();
            }

            if (map.containsKey(JobFlowSpecsDefinition.PARAMETERS)) {
                for (Map.Entry<String, Object> e1 : ((Map<String, Object>) map.get(JobFlowSpecsDefinition.PARAMETERS)).entrySet()) {
                    String paramKey = e1.getKey();
                    Object paramVal = e1.getValue();
                    params.set(paramKey, parseSpecs(definition, paramVal, paramKey));
                }
            } else {
                throw new ResourceInitializationException("Parameters expected.");
            }

            logger.debug("Initializing " + ret.getClass().getName());
            if (ret instanceof CollectionReader) {
                ((CollectionReader) ret).initialize((CollectionParameters) params);
            } else if (ret instanceof DataStore) {
                ((DataStore) ret).initialize((CollectionParameters) params);
            } else {
                ((Initializable) ret).initialize(params);
            }
        }

        return ret;
    }

    protected void validate(JobFlowSpecsDefinition specsDefinition, Object specs, String key, List<String> errors) {
        String valueType = specsDefinition.getValueType(key);
        if (valueType == null) {
            errors.add("Key " + key + " not found in spec definition.");
        } else if (valueType.equals(JobFlowSpecsDefinition.BOOLEAN)) {
            if (!(specs instanceof Boolean)) {
                errors.add("Value of " + key + " must be a boolean.");
            }
        } else if (valueType.equals(JobFlowSpecsDefinition.INTEGER)) {
            if (!(specs instanceof Integer)) {
                errors.add("Value of " + key + " must be an integer.");
            }
        } else if (valueType.equals(JobFlowSpecsDefinition.DOUBLE)) {
            if (!(specs instanceof Double)) {
                errors.add("Value of " + key + " must be a double.");
            }
        } else if (valueType.equals(JobFlowSpecsDefinition.STRING)) {
            if (!(specs instanceof String)) {
                errors.add("Value of " + key + " must be a string.");
            }
        } else if (valueType.equals(JobFlowSpecsDefinition.STRING_OR_INTEGER)) {
            if (!(specs instanceof String || specs instanceof Integer)) {
                errors.add("Value of " + key + " must be either a string or an integer.");
            }
        } else if (valueType.equals(JobFlowSpecsDefinition.LIST)) {
            if (!(specs instanceof List)) {
                errors.add("Value of " + key + " must be a list.");
            }

            if (!specsDefinition.getListType(key).equals(JobFlowSpecsDefinition.STRING)) {
                // List of values is not an arbitrary list. Must be validated
                for (Object obj : (List) specs) {
                    if (obj instanceof String) {
                        validate(specsDefinition, obj, (String) obj, errors);
                    } else if (obj instanceof Map) {
                        for (Map.Entry<String, Object> e : ((Map<String, Object>) obj).entrySet()) {
                            validate(specsDefinition, e.getValue(), e.getKey(), errors);
                        }
                    } else {
                        errors.add("Only strings or maps allowed as list items in " + key);
                    }
                }
            }
        } else if (valueType.equals(JobFlowSpecsDefinition.MAP)) {
            if (!(specs instanceof Map)) {
                errors.add("Value of " + key + " must be a map.");
            } else {
                // Make sure all required keys are present.
                Set<String> required = specsDefinition.getRequired(key);
                for (String r : required) {
                    if (!((Map<String, Object>) specs).containsKey(r)) {
                        errors.add("Required key " + r + " is missing in " + key);
                    }
                }

                // Make sure all parameters are valid for the key.
                Set<String> validParameters = specsDefinition.getParameters(key);
                if (((Map<String, Object>) specs).containsKey(JobFlowSpecsDefinition.PARAMETERS)) {
                    Map<String, Object> parameters = (Map<String, Object>) ((Map<String, Object>) specs).get(JobFlowSpecsDefinition.PARAMETERS);
                    for (String param : parameters.keySet()) {
                        if (!validParameters.contains(param)) {
                            errors.add(param + " not a valid parameter for " + key);
                        }
                    }
                }

                // Validate entries.
                for (Map.Entry<String, Object> e : ((Map<String, Object>) specs).entrySet()) {
                    logger.debug("Validating " + e.getKey());
                    validate(specsDefinition, e.getValue(), e.getKey(), errors);
                }
            }
        } else {
            errors.add("Value type " + valueType + " not recognized.");
        }
    }

    public CollectionReader getCollectionReader() {
        return collectionReader;
    }

    public CollectionAnalyzer getCollectionAnalyzer() {
        return collectionAnalyzer;
    }

    public DataStore[] getDataStores() {
        return dataStores;
    }
}
