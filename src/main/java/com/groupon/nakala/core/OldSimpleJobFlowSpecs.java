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

import com.groupon.ml.svm.ValueScaler;
import com.groupon.nakala.analysis.Analyzer;
import com.groupon.nakala.analysis.CollectionAnalyzer;
import com.groupon.nakala.db.CollectionParameters;
import com.groupon.nakala.db.CollectionReader;
import com.groupon.nakala.db.DataStore;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import com.groupon.nakala.normalization.StringNormalizer;
import com.groupon.util.io.IoUtil;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.Reader;
import java.util.*;

/**
 * @author npendar@groupon.com
 */
public class OldSimpleJobFlowSpecs implements JobFlowSpecs {
    protected static final Set<String> REQUIRED_ANALYZER_KEYS = new HashSet<String>();
    protected static final Set<String> REQUIRED_COLLECTION_ANALYZER_KEYS = new HashSet<String>();
    protected static final Set<String> REQUIRED_FEATURES_KEYS = new HashSet<String>();
    protected static final Set<String> REQUIRED_FILTER_KEYS = new HashSet<String>();
    protected static final Set<String> REQUIRED_LABELS_KEYS = new HashSet<String>();
    protected static final Set<String> REQUIRED_MODEL_KEYS = new HashSet<String>();
    protected static final Set<String> REQUIRED_NORMALIZER_KEYS = new HashSet<String>();
    protected static final Set<String> REQUIRED_READER_KEYS = new HashSet<String>();
    protected static final Set<String> REQUIRED_REPRESENTER_KEYS = new HashSet<String>();
    protected static final Set<String> REQUIRED_SCALER_KEYS = new HashSet<String>();
    protected static final Set<String> REQUIRED_STOPWORDS_KEYS = new HashSet<String>();
    protected static final Set<String> REQUIRED_STORE_KEYS = new HashSet<String>();
    protected static final Set<String> REQUIRED_TOKENIZER_KEYS = new HashSet<String>();
    protected static final Set<String> REQUIRED_TOP_LEVEL_KEYS = new HashSet<String>();

    protected static final Set<String> OPTIONAL_ANALYZER_KEYS = new HashSet<String>();
    protected static final Set<String> OPTIONAL_COLLECTION_ANALYZER_KEYS = new HashSet<String>();
    protected static final Set<String> OPTIONAL_FEATURES_KEYS = new HashSet<String>();
    protected static final Set<String> OPTIONAL_FILTER_KEYS = new HashSet<String>();
    protected static final Set<String> OPTIONAL_LABELS_KEYS = new HashSet<String>();
    protected static final Set<String> OPTIONAL_MODEL_KEYS = new HashSet<String>();
    protected static final Set<String> OPTIONAL_NORMALIZER_KEYS = new HashSet<String>();
    protected static final Set<String> OPTIONAL_READER_KEYS = new HashSet<String>();
    protected static final Set<String> OPTIONAL_REPRESENTER_KEYS = new HashSet<String>();
    protected static final Set<String> OPTIONAL_SCALER_KEYS = new HashSet<String>();
    protected static final Set<String> OPTIONAL_STOPWORDS_KEYS = new HashSet<String>();
    protected static final Set<String> OPTIONAL_STORE_KEYS = new HashSet<String>();
    protected static final Set<String> OPTIONAL_TOKENIZER_KEYS = new HashSet<String>();
    protected static final Set<String> OPTIONAL_TOP_LEVEL_KEYS = new HashSet<String>();

    protected static final Map<String, PARAM_TYPE> KNOWN_ANALYZER_PARAMS = new HashMap<String, PARAM_TYPE>();
    protected static final Map<String, PARAM_TYPE> KNOWN_COLLECTION_ANALYZER_PARAMS = new HashMap<String, PARAM_TYPE>();
    protected static final Map<String, PARAM_TYPE> KNOWN_FILTER_PARAMS = new HashMap<String, PARAM_TYPE>();
    protected static final Map<String, PARAM_TYPE> KNOWN_READER_PARAMS = new HashMap<String, PARAM_TYPE>();
    protected static final Map<String, PARAM_TYPE> KNOW_REPRESENTER_PARAMS = new HashMap<String, PARAM_TYPE>();
    protected static final Map<String, PARAM_TYPE> KNOWN_STORE_PARAMS = new HashMap<String, PARAM_TYPE>();

    protected static enum PARAM_TYPE {
        BOOLEAN,
        STRING,
        INTEGER,
        DOUBLE,
        STRING_OR_INTEGER,
        FILTER_PARAMS,
        TOKENIZER,
        NORMALIZERS,
        LABELS,
        STOPWORDS,
        FEATURES,
        REPRESENTER,
        ANALYZER,
        MODEL,
        SCALER
    }

    static {
        REQUIRED_TOP_LEVEL_KEYS.add(Constants.COLLECTION_READER);
        REQUIRED_TOP_LEVEL_KEYS.add(Constants.COLLECTION_ANALYZER);
        REQUIRED_TOP_LEVEL_KEYS.add(Constants.DATA_STORES);

        REQUIRED_READER_KEYS.add(Constants.CLASS_NAME);
        OPTIONAL_READER_KEYS.add(Constants.PARAMETERS);

        REQUIRED_COLLECTION_ANALYZER_KEYS.add(Constants.CLASS_NAME);
        OPTIONAL_COLLECTION_ANALYZER_KEYS.add(Constants.PARAMETERS);

        REQUIRED_ANALYZER_KEYS.add(Constants.CLASS_NAME);
        OPTIONAL_ANALYZER_KEYS.add(Constants.PARAMETERS);

        REQUIRED_STORE_KEYS.add(Constants.CLASS_NAME);
        OPTIONAL_STORE_KEYS.add(Constants.PARAMETERS);

        REQUIRED_FILTER_KEYS.add(Constants.CLASS_NAME);
        OPTIONAL_FILTER_KEYS.add(Constants.FILE_NAME);
        OPTIONAL_FILTER_KEYS.add(Constants.RESOURCE);

        REQUIRED_TOKENIZER_KEYS.add(Constants.CLASS_NAME);

        REQUIRED_NORMALIZER_KEYS.add(Constants.CLASS_NAME);

        REQUIRED_REPRESENTER_KEYS.add(Constants.CLASS_NAME);
        OPTIONAL_REPRESENTER_KEYS.add(Constants.PARAMETERS);
        OPTIONAL_REPRESENTER_KEYS.add(Constants.NORMALIZE_BY_LENGTH);

        OPTIONAL_FEATURES_KEYS.add(Constants.FILE_NAME);
        OPTIONAL_FEATURES_KEYS.add(Constants.RESOURCE);

        OPTIONAL_LABELS_KEYS.add(Constants.FILE_NAME);
        OPTIONAL_LABELS_KEYS.add(Constants.RESOURCE);

        OPTIONAL_MODEL_KEYS.add(Constants.FILE_NAME);
        OPTIONAL_MODEL_KEYS.add(Constants.RESOURCE);

        OPTIONAL_SCALER_KEYS.add(Constants.FILE_NAME);
        OPTIONAL_SCALER_KEYS.add(Constants.RESOURCE);

        OPTIONAL_STOPWORDS_KEYS.add(Constants.FILE_NAME);
        OPTIONAL_STOPWORDS_KEYS.add(Constants.RESOURCE);

        KNOWN_READER_PARAMS.put(CollectionParameters.COLLECTION_NAME, PARAM_TYPE.STRING);
        KNOWN_READER_PARAMS.put(CollectionParameters.DB_NAME, PARAM_TYPE.STRING);
        KNOWN_READER_PARAMS.put(CollectionParameters.FILE_NAME, PARAM_TYPE.STRING);
        KNOWN_READER_PARAMS.put(CollectionParameters.HOST, PARAM_TYPE.STRING);
        KNOWN_READER_PARAMS.put(CollectionParameters.ID_FIELD, PARAM_TYPE.STRING_OR_INTEGER);
        KNOWN_READER_PARAMS.put(CollectionParameters.LABEL_FIELD, PARAM_TYPE.STRING_OR_INTEGER);
        KNOWN_READER_PARAMS.put(CollectionParameters.PORT, PARAM_TYPE.INTEGER);
        KNOWN_READER_PARAMS.put(CollectionParameters.SEPARATOR, PARAM_TYPE.STRING);
        KNOWN_READER_PARAMS.put(CollectionParameters.TABLE_NAME, PARAM_TYPE.STRING);
        KNOWN_READER_PARAMS.put(CollectionParameters.TEXT_FIELD, PARAM_TYPE.STRING_OR_INTEGER);
        KNOWN_READER_PARAMS.put(CollectionParameters.TITLE_FIELD, PARAM_TYPE.STRING_OR_INTEGER);

        KNOWN_ANALYZER_PARAMS.put(Constants.LABELS, PARAM_TYPE.LABELS);
        KNOWN_ANALYZER_PARAMS.put(Constants.MODEL, PARAM_TYPE.MODEL);
        KNOWN_ANALYZER_PARAMS.put(Constants.REPRESENTER, PARAM_TYPE.REPRESENTER);
        KNOWN_ANALYZER_PARAMS.put(Constants.THRESHOLD, PARAM_TYPE.DOUBLE);

        KNOWN_COLLECTION_ANALYZER_PARAMS.put(Constants.ANALYZER, PARAM_TYPE.ANALYZER);
        KNOWN_COLLECTION_ANALYZER_PARAMS.put(Constants.BLOCK_FILTER, PARAM_TYPE.FILTER_PARAMS);
        KNOWN_COLLECTION_ANALYZER_PARAMS.put(Constants.FIND_BEST_PARAMETERS, PARAM_TYPE.BOOLEAN);
        KNOWN_COLLECTION_ANALYZER_PARAMS.put(Constants.MIN_DF, PARAM_TYPE.INTEGER);
        KNOWN_COLLECTION_ANALYZER_PARAMS.put(Constants.NORMALIZERS, PARAM_TYPE.NORMALIZERS);
        KNOWN_COLLECTION_ANALYZER_PARAMS.put(Constants.PASS_FILTER, PARAM_TYPE.FILTER_PARAMS);
        KNOWN_COLLECTION_ANALYZER_PARAMS.put(Constants.REPRESENTER, PARAM_TYPE.REPRESENTER);
        KNOWN_COLLECTION_ANALYZER_PARAMS.put(Constants.SAMPLE, PARAM_TYPE.DOUBLE);
        KNOWN_COLLECTION_ANALYZER_PARAMS.put(Constants.STOPWORDS, PARAM_TYPE.STOPWORDS);
        KNOWN_COLLECTION_ANALYZER_PARAMS.put(Constants.TOKENIZER, PARAM_TYPE.TOKENIZER);
        KNOWN_COLLECTION_ANALYZER_PARAMS.put(Constants.MAX_THRESHOLD, PARAM_TYPE.DOUBLE);
        KNOWN_COLLECTION_ANALYZER_PARAMS.put(Constants.MIN_THRESHOLD, PARAM_TYPE.DOUBLE);
        KNOWN_COLLECTION_ANALYZER_PARAMS.put(Constants.THRESHOLD_STEP, PARAM_TYPE.DOUBLE);

        KNOWN_FILTER_PARAMS.put(Constants.CLASS_NAME, PARAM_TYPE.STRING);
        KNOWN_FILTER_PARAMS.put(Constants.FILE_NAME, PARAM_TYPE.STRING);
        KNOWN_FILTER_PARAMS.put(Constants.RESOURCE, PARAM_TYPE.STRING);

        KNOWN_STORE_PARAMS.put(CollectionParameters.COLLECTION_NAME, PARAM_TYPE.STRING);
        KNOWN_STORE_PARAMS.put(CollectionParameters.DB_NAME, PARAM_TYPE.STRING);
        KNOWN_STORE_PARAMS.put(CollectionParameters.FILE_NAME, PARAM_TYPE.STRING);
        KNOWN_STORE_PARAMS.put(CollectionParameters.HOST, PARAM_TYPE.STRING);
        KNOWN_STORE_PARAMS.put(CollectionParameters.ID_FIELD, PARAM_TYPE.STRING);
        KNOWN_STORE_PARAMS.put(CollectionParameters.LABEL_FIELD, PARAM_TYPE.STRING);
        KNOWN_STORE_PARAMS.put(CollectionParameters.PORT, PARAM_TYPE.INTEGER);
        KNOWN_STORE_PARAMS.put(CollectionParameters.TABLE_NAME, PARAM_TYPE.STRING);
        KNOWN_STORE_PARAMS.put(CollectionParameters.TEXT_FIELD, PARAM_TYPE.STRING);
        KNOWN_STORE_PARAMS.put(CollectionParameters.TITLE_FIELD, PARAM_TYPE.STRING);

        KNOW_REPRESENTER_PARAMS.put(Constants.FEATURES, PARAM_TYPE.FEATURES);
        KNOW_REPRESENTER_PARAMS.put(Constants.NORMALIZE_BY_LENGTH, PARAM_TYPE.BOOLEAN);
        KNOW_REPRESENTER_PARAMS.put(Constants.NORMALIZERS, PARAM_TYPE.NORMALIZERS);
        KNOW_REPRESENTER_PARAMS.put(Constants.TOKENIZER, PARAM_TYPE.TOKENIZER);
        KNOW_REPRESENTER_PARAMS.put(Constants.SCALER, PARAM_TYPE.SCALER);
    }

    protected CollectionReader collectionReader;
    protected CollectionAnalyzer collectionAnalyzer;
    protected DataStore[] dataStores;

    protected CollectionParameters collectionReaderParameters;
    protected CollectionParameters dataStoreParameters;
    protected Parameters collectionAnalyzerParameters;

    @Override
    public void initialize(InputStream inputStream) throws ResourceInitializationException {
        Yaml yaml = new Yaml();
        try {
            Map<String, Object> specs = (Map<String, Object>) yaml.load(inputStream);
            checkKeys(REQUIRED_TOP_LEVEL_KEYS, OPTIONAL_TOP_LEVEL_KEYS, specs.keySet());
            parseReaderSpecs((Map<String, Object>) specs.get(Constants.COLLECTION_READER));
            parseCollectionAnalyzerSpecs((Map<String, Object>) specs.get(Constants.COLLECTION_ANALYZER));
            parseDataStoresSpecs((List<Map<String, Object>>) specs.get(Constants.DATA_STORES));
        } catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    private void checkKeys(Set<String> required, Set<String> optional, Set<String> parsed) throws ResourceInitializationException {
        List<String> missing = new LinkedList<String>();
        for (String e : required) {
            if (!parsed.contains(e))
                missing.add(e);
        }
        List<String> unexpected = new LinkedList<String>();
        for (String p : parsed) {
            if (!(required.contains(p) || optional.contains(p)))
                unexpected.add(p);
        }
        if (!missing.isEmpty() || !unexpected.isEmpty())
            throw new ResourceInitializationException("Missing parameters: " + StringUtils.join(missing, ", ") +
                    "  Unexpected parameters: " + StringUtils.join(unexpected, ", "));
    }

    private void parseReaderSpecs(Map<String, Object> specs) throws ResourceInitializationException {
        checkKeys(REQUIRED_READER_KEYS, OPTIONAL_READER_KEYS, specs.keySet());

        try {
            collectionReader = (CollectionReader) Class.forName((String) specs.get(Constants.CLASS_NAME)).newInstance();
        } catch (ClassNotFoundException e) {
            throw new ResourceInitializationException("Collection reader class not found.", e);
        } catch (IllegalAccessException e) {
            throw new ResourceInitializationException("Collection reader class not accessible.", e);
        } catch (InstantiationException e) {
            throw new ResourceInitializationException("Failed to instantiate collection reader.", e);
        }

        collectionReaderParameters = new CollectionParameters();
        parseParameters((Map<String, Object>) specs.get(Constants.PARAMETERS), KNOWN_READER_PARAMS, collectionReaderParameters);
    }

    private void parseCollectionAnalyzerSpecs(Map<String, Object> specs) throws ResourceInitializationException {
        checkKeys(REQUIRED_COLLECTION_ANALYZER_KEYS, OPTIONAL_COLLECTION_ANALYZER_KEYS, specs.keySet());

        try {
            collectionAnalyzer = (CollectionAnalyzer) Class.forName((String) specs.get(Constants.CLASS_NAME)).newInstance();
        } catch (ClassNotFoundException e) {
            throw new ResourceInitializationException("Analyzer class not found.", e);
        } catch (IllegalAccessException e) {
            throw new ResourceInitializationException("Analyzer class not accessible.", e);
        } catch (InstantiationException e) {
            throw new ResourceInitializationException("Failed to instantiate analyzer.", e);
        }

        collectionAnalyzerParameters = new Parameters();
        parseParameters((Map<String, Object>) specs.get(Constants.PARAMETERS), KNOWN_COLLECTION_ANALYZER_PARAMS, collectionAnalyzerParameters);
    }

    private void parseDataStoresSpecs(List<Map<String, Object>> specs) throws Exception {
        if (specs.isEmpty())
            throw new ResourceInitializationException("No data store specified.");

        dataStores = new DataStore[specs.size()];
        for (int i = 0; i < specs.size(); ++i) {
            dataStores[i] = parseDataStoreSpecs(specs.get(i));
        }
    }

    private DataStore parseDataStoreSpecs(Map<String, Object> specs) throws ResourceInitializationException {
        checkKeys(REQUIRED_STORE_KEYS, OPTIONAL_STORE_KEYS, specs.keySet());

        DataStore dataStore = null;
        try {
            dataStore = (DataStore) Class.forName((String) specs.get(Constants.CLASS_NAME)).newInstance();
        } catch (ClassNotFoundException e) {
            throw new ResourceInitializationException("Data store class " + specs.get(Constants.CLASS_NAME) + " not found.", e);
        } catch (IllegalAccessException e) {
            throw new ResourceInitializationException("Data store class " + specs.get(Constants.CLASS_NAME) + " not accessible.", e);
        } catch (InstantiationException e) {
            throw new ResourceInitializationException("Failed to instantiate data store " + specs.get(Constants.CLASS_NAME), e);
        }

        dataStoreParameters = new CollectionParameters();
        parseParameters((Map<String, Object>) specs.get(Constants.PARAMETERS), KNOWN_STORE_PARAMS, dataStoreParameters);
        return dataStore;
    }

    private void parseFilterSpecs(String filterType, Map<String, String> specs, Parameters parameters) throws ResourceInitializationException {
        checkKeys(REQUIRED_FILTER_KEYS, OPTIONAL_FILTER_KEYS, specs.keySet());

        if (filterType.equals(Constants.BLOCK_FILTER)) {
            BlockFilter blockFilter = null;
            try {
                blockFilter = (BlockFilter) Class.forName(specs.get(Constants.CLASS_NAME)).newInstance();
            } catch (ClassNotFoundException e) {
                throw new ResourceInitializationException("Filter class " + specs.get(Constants.CLASS_NAME) + " not found.", e);
            } catch (IllegalAccessException e) {
                throw new ResourceInitializationException("Filter class " + specs.get(Constants.CLASS_NAME) + " not accessible.", e);
            } catch (InstantiationException e) {
                throw new ResourceInitializationException("Failed to instantiate filter " + specs.get(Constants.CLASS_NAME), e);
            }
            if (specs.containsKey(Constants.FILE_NAME)) {
                blockFilter.initialize(specs.get(Constants.FILE_NAME));
            } else if (specs.containsKey(Constants.RESOURCE)) {
                blockFilter.initialize(BlockFilter.class, specs.get(Constants.RESOURCE));
            } else {
                throw new ResourceInitializationException("No filename or resource specified for block filter.");
            }
            parameters.set(Constants.BLOCK_FILTER, blockFilter);
        } else {
            PassFilter passFilter = null;
            try {
                passFilter = (PassFilter) Class.forName(specs.get(Constants.CLASS_NAME)).newInstance();
            } catch (ClassNotFoundException e) {
                throw new ResourceInitializationException("Filter class " + specs.get(Constants.CLASS_NAME) + " not found.", e);
            } catch (IllegalAccessException e) {
                throw new ResourceInitializationException("Filter class " + specs.get(Constants.CLASS_NAME) + " not accessible.", e);
            } catch (InstantiationException e) {
                throw new ResourceInitializationException("Failed to instantiate filter " + specs.get(Constants.CLASS_NAME), e);
            }
            if (specs.containsKey(Constants.FILE_NAME)) {
                passFilter.initialize(specs.get(Constants.FILE_NAME));
            } else if (specs.containsKey(Constants.RESOURCE)) {
                passFilter.initialize(PassFilter.class, specs.get(Constants.RESOURCE));
            } else {
                throw new ResourceInitializationException("No filename or resource specified for pass filter.");
            }
            parameters.set(Constants.PASS_FILTER, passFilter);
        }
    }


    private void parseParameters(Map<String, Object> specs, Map<String, PARAM_TYPE> knownParams, Parameters parameters) throws ResourceInitializationException {
        if (specs == null)
            return;

        for (Map.Entry<String, Object> e : specs.entrySet()) {
            PARAM_TYPE type = knownParams.get(e.getKey());
            if (type != null) {
                switch (type) {
                    case BOOLEAN:
                        parameters.set(e.getKey(), e.getValue());
                        break;
                    case STRING:
                        parameters.set(e.getKey(), e.getValue().toString().replace("\\t", "\t"));
                        break;
                    case INTEGER:
                        parameters.set(e.getKey(), e.getValue());
                        break;
                    case DOUBLE:
                        parameters.set(e.getKey(), e.getValue());
                        break;
                    case STRING_OR_INTEGER:
                        parameters.set(e.getKey(), e.getValue());
                        break;
                    case FILTER_PARAMS:
                        parseFilterSpecs(e.getKey(), (Map<String, String>) e.getValue(), parameters);
                        break;
                    case TOKENIZER:
                        parseTokenizerSpecs((Map<String, String>) e.getValue(), parameters);
                        break;
                    case NORMALIZERS:
                        parseNormalizersSpecs((List<Map<String, String>>) e.getValue(), parameters);
                        break;
                    case STOPWORDS:
                        parseStopwordsSpecs((Map<String, String>) e.getValue(), parameters);
                        break;
                    case REPRESENTER:
                        parseRepresenterSpecs((Map<String, Object>) e.getValue(), parameters);
                        break;
                    case FEATURES:
                        parseFeaturesSpecs((Map<String, String>) e.getValue(), parameters);
                        break;
                    case LABELS:
                        parseLabelsSpecs((Map<String, String>) e.getValue(), parameters);
                        break;
                    case MODEL:
                        parseModelSpecs((Map<String, String>) e.getValue(), parameters);
                        break;
                    case SCALER:
                        parseScalerSpecs((Map<String, String>) e.getValue(), parameters);
                        break;
                    case ANALYZER:
                        parseUnitAnalyzerSpecs((Map<String, Object>) e.getValue(), parameters);
                        break;
                    default:
                        throw new ResourceInitializationException("You just hit a bug. I don't know what to do with type " + type);
                }
            } else {
                throw new ResourceInitializationException("Unknown parameter " + e.getKey());
            }
        }
    }

    private void parseUnitAnalyzerSpecs(Map<String, Object> specs, Parameters parameters) throws ResourceInitializationException {
        checkKeys(REQUIRED_ANALYZER_KEYS, OPTIONAL_ANALYZER_KEYS, specs.keySet());

        Analyzer analyzer = null;
        try {
            analyzer = (Analyzer) Class.forName((String) specs.get(Constants.CLASS_NAME)).newInstance();
        } catch (ClassNotFoundException e) {
            throw new ResourceInitializationException("Analyzer class " + specs.get(Constants.CLASS_NAME) + "not found.", e);
        } catch (IllegalAccessException e) {
            throw new ResourceInitializationException("Analyzer class " + specs.get(Constants.CLASS_NAME) + " not accessible.", e);
        } catch (InstantiationException e) {
            throw new ResourceInitializationException("Failed to instantiate analyzer " + specs.get(Constants.CLASS_NAME), e);
        }

        Parameters analyzerParameters = new Parameters();
        if (specs.containsKey(Constants.PARAMETERS)) {
            parseParameters((Map<String, Object>) specs.get(Constants.PARAMETERS), KNOWN_ANALYZER_PARAMS, analyzerParameters);
        }
        analyzer.initialize(analyzerParameters);
        parameters.set(Constants.ANALYZER, analyzer);
    }

    private void parseScalerSpecs(Map<String, String> specs, Parameters parameters) throws ResourceInitializationException {
        checkKeys(REQUIRED_SCALER_KEYS, OPTIONAL_SCALER_KEYS, specs.keySet());

        ValueScaler scaler = new ValueScaler();
        if (specs.containsKey(Constants.RESOURCE)) {
            scaler.load(getClass(), specs.get(Constants.RESOURCE));
        } else if (specs.containsKey(Constants.FILE_NAME)) {
            scaler.load(specs.get(Constants.FILE_NAME));
        }

        parameters.set(Constants.SCALER, scaler);
    }

    private void parseModelSpecs(Map<String, String> specs, Parameters parameters) throws ResourceInitializationException {
        checkKeys(REQUIRED_MODEL_KEYS, OPTIONAL_MODEL_KEYS, specs.keySet());

        Parameters modelParams = new Parameters();
        if (specs.containsKey(Constants.RESOURCE)) {
            modelParams.set(Constants.RESOURCE, specs.get(Constants.RESOURCE));
        } else if (specs.containsKey(Constants.FILE_NAME)) {
            modelParams.set(Constants.FILE_NAME, specs.get(Constants.FILE_NAME));
        } else {
            throw new ResourceInitializationException("No file name or resource specified for model.");
        }
        parameters.set(Constants.MODEL, modelParams);
    }

    private void parseLabelsSpecs(Map<String, String> specs, Parameters parameters) throws ResourceInitializationException {
        checkKeys(REQUIRED_LABELS_KEYS, OPTIONAL_LABELS_KEYS, specs.keySet());

        String filename = null;

        try {
            Reader reader = null;
            if (specs.containsKey(Constants.RESOURCE)) {
                reader = IoUtil.read(getClass(), specs.get(Constants.RESOURCE));
            } else if (specs.containsKey(Constants.FILE_NAME)) {
                reader = IoUtil.read(specs.get(Constants.FILE_NAME));
            } else {
                throw new ResourceInitializationException("No file name or resource specified for labels.");
            }
            List<String> labels = new LinkedList<String>();
            IoUtil.readCollection(reader, labels);
            parameters.set(Constants.LABELS, labels);
        } catch (Exception e) {
            throw new ResourceInitializationException("Failed to load labels file " + filename, e);
        }
    }

    private void parseFeaturesSpecs(Map<String, String> specs, Parameters parameters) throws ResourceInitializationException {
        checkKeys(REQUIRED_FEATURES_KEYS, OPTIONAL_FEATURES_KEYS, specs.keySet());

        Features features = new Features();
        if (specs.containsKey(Constants.RESOURCE)) {
            features.initialize(Features.class, specs.get(Constants.RESOURCE));
        } else if (specs.containsKey(Constants.FILE_NAME)) {
            features.initialize(specs.get(Constants.FILE_NAME));
        } else {
            throw new ResourceInitializationException("No file name or resource specified for features.");
        }

        parameters.set(Constants.FEATURES, features);
    }

    private void parseStopwordsSpecs(Map<String, String> specs, Parameters parameters) throws ResourceInitializationException {
        checkKeys(REQUIRED_STOPWORDS_KEYS, OPTIONAL_STOPWORDS_KEYS, specs.keySet());

        StringSet stopwords = new StringSet();
        if (specs.containsKey(Constants.RESOURCE)) {
            stopwords.initialize(StringSet.class, specs.get(Constants.RESOURCE));
        } else if (specs.containsKey(Constants.FILE_NAME)) {
            stopwords.initialize(specs.get(Constants.FILE_NAME));
        } else {
            throw new ResourceInitializationException("No file name or resource name specified for stop words.");
        }
        parameters.set(Constants.STOPWORDS, stopwords);
    }

    private void parseRepresenterSpecs(Map<String, Object> specs, Parameters parameters) throws ResourceInitializationException {
        checkKeys(REQUIRED_REPRESENTER_KEYS, OPTIONAL_REPRESENTER_KEYS, specs.keySet());

        String className = (String) specs.get(Constants.CLASS_NAME);
        TextRepresenter representer;
        try {
            representer = (TextRepresenter) Class.forName(className).newInstance();
        } catch (ClassNotFoundException e) {
            throw new ResourceInitializationException("Tokenizer class " + className + " not found.", e);
        } catch (IllegalAccessException e) {
            throw new ResourceInitializationException("Tokenizer class " + className + " not accessible.", e);
        } catch (InstantiationException e) {
            throw new ResourceInitializationException("Failed to instantiate tokenizer " + className, e);
        }

        Parameters representerParams = new Parameters();

        if (specs.containsKey(Constants.PARAMETERS)) {
            parseParameters((Map<String, Object>) specs.get(Constants.PARAMETERS), KNOW_REPRESENTER_PARAMS, representerParams);
        }

        representer.initialize(representerParams);
        parameters.set(Constants.REPRESENTER, representer);

    }

    private void parseTokenizerSpecs(Map<String, String> specs, Parameters parameters) throws ResourceInitializationException {
        checkKeys(REQUIRED_TOKENIZER_KEYS, OPTIONAL_TOKENIZER_KEYS, specs.keySet());

        String className = specs.get(Constants.CLASS_NAME);
        try {
            parameters.set(Constants.TOKENIZER, Class.forName(className).newInstance());
        } catch (ClassNotFoundException e) {
            throw new ResourceInitializationException("Tokenizer class " + className + " not found.", e);
        } catch (IllegalAccessException e) {
            throw new ResourceInitializationException("Tokenizer class " + className + " not accessible.", e);
        } catch (InstantiationException e) {
            throw new ResourceInitializationException("Failed to instantiate tokenizer " + className, e);
        }
    }

    private void parseNormalizersSpecs(List<Map<String, String>> specs, Parameters parameters) throws ResourceInitializationException {
        List<StringNormalizer> normalizers = new ArrayList<StringNormalizer>(specs.size());
        for (Map<String, String> spec : specs) {
            checkKeys(REQUIRED_NORMALIZER_KEYS, OPTIONAL_NORMALIZER_KEYS, spec.keySet());
            String className = spec.get(Constants.CLASS_NAME);
            try {
                normalizers.add((StringNormalizer) Class.forName(className).newInstance());
            } catch (ClassNotFoundException e) {
                throw new ResourceInitializationException("Normalizer class " + className + " not found.", e);
            } catch (IllegalAccessException e) {
                throw new ResourceInitializationException("Normalizer class " + className + " not accessible.", e);
            } catch (InstantiationException e) {
                throw new ResourceInitializationException("Failed to instantiate normalizer " + className, e);
            }
        }
        parameters.set(Constants.NORMALIZERS, normalizers);
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

    public CollectionParameters getCollectionReaderParameters() {
        return collectionReaderParameters;
    }

    public CollectionParameters getDataStoreParameters() {
        return dataStoreParameters;
    }

    public Parameters getCollectionAnalyzerParameters() {
        return collectionAnalyzerParameters;
    }
}
