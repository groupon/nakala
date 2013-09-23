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

import com.groupon.util.io.IoUtil;
import org.apache.lucene.search.spans.SpanQuery;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author npendar@groupon.com
 */
public abstract class AbstractVocabulary {
    protected Map<String, Set<SpanQuery>> queryMapContents; // label -> queries
    protected Map<String, Set<SpanQuery>> queryMapTitle;    // label -> queries
    protected Map<String, Set<Pattern>> patMap; // label -> patterns
    // used for highlighting

    protected static final Pattern INCLUDE = Pattern.compile(
            "INCLUDE\\s+(\\*|(?:(?:\\w+)(?:,\\w+)*))\\s+FROM\\s+(\\w+)",
            Pattern.CASE_INSENSITIVE);
    protected static final Pattern COMMA = Pattern.compile("\\s*,\\s*");
    protected static final Pattern SPACE = Pattern.compile("\\s+");
    protected static final Pattern INDENT = Pattern.compile("^\\s+");
    protected static final Pattern COMMANOSP = Pattern.compile(",");

    protected static final String OVERRIDE = "OVERRIDE";

    protected String vocabPath = "/nakala/sentiment/";
    protected String domain;
    protected Set<String> labels;
    protected Set<Pattern> wordsOfInterest;

    public AbstractVocabulary(String vocabName) {
        domain = vocabName.toLowerCase();
    }

    public String getDomain() {
        return domain;
    }

    public Set<Pattern> getWordsOfInterest() {
        return wordsOfInterest;
    }

    protected void readVocabFile(String vocabName, Set<String> fields,
                                 String parent, String iField, Map<String, Set<SpanQuery>> queryMap) throws IOException {
        String f = vocabPath + vocabName + "_vocab.txt";

        BufferedReader in = new BufferedReader(IoUtil.read(AbstractVocabulary.class, f));

        String line;
        int lineNo = 0;
        String label = "";

        while ((line = in.readLine()) != null) {
            ++lineNo;
            line = VocabUtils.COMMENT.matcher(line).replaceFirst("");

            if (line.equals("")) {
                continue;
            }

            if (INDENT.matcher(line).find()) {
                // Indented line contains a vocab item
                if (label.equals("")) {
                    throwException(
                            "Word " + line.trim() + " without label in file "
                                    + f + " line " + lineNo, parent);
                }

                line = line.trim();
                if (line.endsWith(",")) {
                    throwException("Expected term after comma in '" + line
                            + "'.", parent);
                }

                // Add word unless you're required to ignore words under label
                if (fields.contains(label)) {
                    try {
                        processLine(label, line, iField, queryMap);
                    } catch (VocabularyException e) {
                        throwException("Error processing line " + lineNo, parent);
                    }
                }
                continue;
            }

            // Line is a directive or label
            Matcher matcher = INCLUDE.matcher(line);
            if (matcher.find()) {
                // Include a file
                String fieldToInclude = matcher.group(1).toUpperCase();
                String vocabToInclude = matcher.group(2);

                Set<String> fieldsToInclude;
                if (fieldToInclude.equals("*")) {
                    fieldsToInclude = Collections.synchronizedSet(new HashSet<String>(fields));
                } else {
                    fieldsToInclude = Collections.synchronizedSet(new HashSet<String>());
                    for (String splitField : COMMA.split(fieldToInclude)) {
                        if (labels.contains(splitField)) {
                            fieldsToInclude.add(splitField);
                        } else {
                            throwException("Invalid label name " + splitField
                                    + " in file " + f + " line " + lineNo,
                                    parent);
                        }
                    }
                }

                String callingFile = domain;
                // Took it out because include chains aren't followed anymore.
                // if (parent != null){
                // callingFile = parent + " > " + vocabName;
                // //Check for circular INCLUDE
                // if (callingFile.contains(vocabToInclude)){
                // throwException("Circular INCLUDE in " + f + " line " +
                // lineNo,
                // parent);
                // }
                // }
                // Recurse; INCLUDE file, but this only happens at the top level
                readVocabFile(vocabToInclude, fieldsToInclude, callingFile, iField, queryMap);
                continue;
            }

            String[] tokens = SPACE.split(line);
            String t0 = tokens[0].toUpperCase();

            if (queryMap.containsKey(t0)) {
                // It's a valid label
                label = t0;
                if (tokens.length == 2) {
                    // Check for override. Override only works at top level.
                    // Includes must ignore override to avoid inadvertently
                    // deleting previously included data.
                    if (tokens[1].toUpperCase().equals(OVERRIDE)) {
                        if (parent == null) {
                            queryMap.get(label).clear();
                        }
                    } else {
                        throwException("Unknown directive '" + tokens[1]
                                + "' in file " + f + " line " + lineNo, parent);
                    }
                }
            } else {
                throwException("Unknown label or directive '" + line
                        + "' in file " + f + " line " + lineNo, parent);
            }
        }
        in.close();
    }

    private void processLine(String label, String line, String iField, Map<String, Set<SpanQuery>> queryMap)
            throws VocabularyException {
        // get rid of accidental spaces before or after commas
        Matcher matcher = COMMA.matcher(line);
        if (matcher.find()) {
            line = matcher.replaceAll(",");
        }

        SpanQuery sq = VocabUtils.processQuery(line, iField, wordsOfInterest);
        queryMap.get(label).add(sq);
    }

    private void throwException(String msg, String parent)
            throws VocabularyException {
        if (parent != null) {
            msg += " referred to by " + parent;
        }
        throw new VocabularyException(msg);
    }
}