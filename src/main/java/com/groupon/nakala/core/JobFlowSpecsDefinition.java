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

import com.groupon.nakala.exceptions.ResourceInitializationException;
import com.groupon.util.collections.CollectionUtil;
import com.groupon.util.io.IoUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author npendar@groupon.com
 */
public class JobFlowSpecsDefinition {
    public static final String TOP = "top";
    public static final String CLASS_NAME = "class_name";
    public static final String VALUE_TYPE = "value_type";
    public static final String REQUIRED = "required";
    public static final String BOOLEAN = "boolean";
    public static final String STRING = "string";
    public static final String INTEGER = "integer";
    public static final String DOUBLE = "double";
    public static final String STRING_OR_INTEGER = "string_or_integer";
    public static final String MAP = "map";
    public static final String LIST = "list";
    public static final String LIST_TYPE = "list_type";
    public static final String PARAMETERS = "parameters";
    public static final Set<String> VALUE_TYPES = new HashSet<String>();

    static {
        VALUE_TYPES.add(BOOLEAN);
        VALUE_TYPES.add(STRING);
        VALUE_TYPES.add(INTEGER);
        VALUE_TYPES.add(DOUBLE);
        VALUE_TYPES.add(STRING_OR_INTEGER);
        VALUE_TYPES.add(MAP);
        VALUE_TYPES.add(LIST);
    }

    private static final Logger logger = Logger.getLogger(JobFlowSpecsDefinition.class);

    private Map<String, SpecItem> specs;

    public void initialize(String definitionFile) throws ResourceInitializationException {
        InputStream inputStream = null;
        try {
            inputStream = IoUtil.input(JobFlowSpecsDefinition.class, definitionFile);
        } catch (IOException e) {
            throw new ResourceInitializationException("Failed to load " + definitionFile, e);
        }

        initialize(inputStream);
    }

    public void initialize(InputStream inputStream) throws ResourceInitializationException {
        Yaml yaml = new Yaml();
        specs = new HashMap<String, SpecItem>();
        Map<String, Object> definition = null;
        definition = (Map<String, Object>) yaml.load(inputStream);
        Set<String> usedKeys = new HashSet<String>();
        logger.debug("Validating job flow definition.");
        validate(definition, TOP, usedKeys);

        Set<String> unusedKeys = CollectionUtil.intersect(definition.keySet(), usedKeys);
        if (!unusedKeys.isEmpty()) {
            //throw new ResourceInitializationException("Key(s) " + StringUtils.join(unusedKeys, ", ") + " unused.");
            logger.warn("Key(s) " + StringUtils.join(unusedKeys, ", ") + " unused.");
        }

        addSpec(definition, specs);
    }

    private void addSpec(Map<String, Object> definition, Map<String, SpecItem> specs) {

        for (String key : definition.keySet()) {
            Map<String, Object> spec = (Map<String, Object>) definition.get(key);

            SpecItem specItem = new SpecItem();
            specItem.valueType = (String) spec.get(VALUE_TYPE);

            if (specItem.valueType.equals(JobFlowSpecsDefinition.LIST)) {
                specItem.listType = (String) spec.get(LIST_TYPE);
            }

            Object valList = spec.get(REQUIRED);
            if (valList != null) {
                specItem.required = new HashSet<String>((List<String>) valList);
            } else {
                specItem.required = new HashSet<String>();
            }

            valList = spec.get(PARAMETERS);
            if (valList != null) {
                specItem.parameters = new HashSet<String>((List<String>) valList);
            } else {
                specItem.parameters = new HashSet<String>();
            }

            specs.put(key, specItem);
        }
    }

    private void validate(Map<String, Object> definition, String key, Set<String> usedKeys) throws ResourceInitializationException {
        if (!definition.containsKey(key)) {
            throw new ResourceInitializationException("Key '" + key + "' not found in definition file.");
        }

        usedKeys.add(key);

        Map<String, Object> val = (Map<String, Object>) definition.get(key);
        if (!val.containsKey(VALUE_TYPE)) {
            throw new ResourceInitializationException("value_type not provided for " + key);
        }

        if (!(val.get(VALUE_TYPE) instanceof String)) {
            throw new ResourceInitializationException("value of value_type must be atomic in " + key);
        }

        String valueType = (String) val.get(VALUE_TYPE);
        if (!VALUE_TYPES.contains(valueType)) {
            throw new ResourceInitializationException("Invalid value_type: " + valueType + " in " + key);
        }

        if (valueType.equals(LIST)) {
            if (!val.containsKey(LIST_TYPE)) {
                throw new ResourceInitializationException("List type not specified in " + key);
            }

            if (!(val.get(LIST_TYPE) instanceof String)) {
                throw new ResourceInitializationException("List type must be a string in " + key);
            }

            String listType = (String) val.get(LIST_TYPE);
            if (definition.containsKey(listType)) {
                validate(definition, listType, usedKeys);
            } else if (!listType.equals(STRING)) {
                throw new ResourceInitializationException("List type must either be an existing label or 'string' in " + key);
            }
        }

        if (val.containsKey(REQUIRED)) {
            if (!valueType.equals(MAP)) {
                throw new ResourceInitializationException("Required parameter valid for map value_types only. In " + key);
            }

            for (String key1 : (List<String>) val.get(REQUIRED)) {
                validate(definition, key1, usedKeys);
            }
        }

        if (val.containsKey(PARAMETERS)) {
            Object paramList = val.get(PARAMETERS);
            if (!(paramList instanceof List)) {
                throw new ResourceInitializationException("Parameters requires a list of labels. In " + key);
            }
            for (String key1 : (List<String>) paramList) {
                if (!usedKeys.contains(key1))
                    validate(definition, key1, usedKeys);
            }
        }
    }

    public String getValueType(String key) {
        try {
            return specs.get(key).valueType;
        } catch (NullPointerException e) {
            throw new ResourceInitializationException("Key " + key + " undefined.");
        }
    }

    public String getListType(String key) {
        try {
            return specs.get(key).listType;
        } catch (NullPointerException e) {
            throw new ResourceInitializationException("Key " + key + " undefined.");
        }
    }

    public Set<String> getRequired(String key) {
        try {
            return specs.get(key).required;
        } catch (NullPointerException e) {
            throw new ResourceInitializationException("Key " + key + " undefined.");
        }
    }

    public Set<String> getParameters(String key) {
        try {
            return specs.get(key).parameters;
        } catch (NullPointerException e) {
            throw new ResourceInitializationException("Key " + key + " undefined.");
        }
    }
}

class SpecItem {
    String valueType;
    String listType;
    Set<String> required;
    Set<String> parameters;
}
