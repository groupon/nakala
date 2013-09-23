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

package com.groupon.nakala.analysis;

import com.groupon.nakala.core.*;
import com.groupon.nakala.exceptions.AnalyzerFailureException;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import com.groupon.nakala.normalization.DeaccentNormalizer;
import com.groupon.nakala.normalization.DomainNameNormalizer;
import com.groupon.nakala.normalization.RegexpBasedCleanerNormalizer;
import com.groupon.nakala.normalization.StringNormalizer;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author npendar@groupon.com
 */
public class FuzzyFinderAnalyzer implements Analyzer {

    public static final String NAME = "name";
    public static final String ADDRESS = "address";
    public static final String LOCALITY = "locality";
    public static final String REGION = "region";
    public static final String POSTAL_CODE = "postal_code";
    public static final String PHONE = "phone";
    public static final String DOMAIN_NAME_SIMILARITY = "domain_name_similarity";

    private static final int SHINGLE_SIZE = 4;
    private static final String[] TAILS = new String[]{"ltd", "llc", "limited", "ltda", "inc"};

    private ShinglesTokenizerStream shinglesTokenizer;
    private TokenizerStream wordTokenizer;
    private StringNormalizer deaccentNormalizer;
    private StringNormalizer parensRemover;
    private StringNormalizer orphanSRemover;
    private StringNormalizer domainNameNormalizer;

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        shinglesTokenizer = new ShinglesTokenizerStream();
        ((ShinglesTokenizerStream) shinglesTokenizer).setShingleSize(SHINGLE_SIZE);
        wordTokenizer = new RegexpTokenizerStream();
        deaccentNormalizer = new DeaccentNormalizer();
        // Remove text between parentheses and ** from name, e.g., Hyatt (San Diego), or Hyatt **dupe**
        parensRemover = new RegexpBasedCleanerNormalizer("(?:\\*\\*|\\().+(?:\\*\\*|\\))");
        // Remove orphan "s", e.g., john s pizza --> john pizza
        orphanSRemover = new RegexpBasedCleanerNormalizer(" s\\b");
        // Extract domain name (without www or top level name) from url, e.g., www.nbc.com --> nbc
        domainNameNormalizer = new DomainNameNormalizer();
    }

    public String clean(String s) {
        if (StringUtils.isEmpty(s)) return "";

        StringBuilder sb = new StringBuilder();
        boolean lastTokenEndsInDigit = false;
        wordTokenizer.setText(deaccentNormalizer.normalize(s.toLowerCase()));
        String token;
        while ((token = wordTokenizer.next()) != null) {
            if (token.isEmpty()) continue;
            boolean tokenBeginsWithDigit = Character.isDigit(token.charAt(0));
            if (lastTokenEndsInDigit && tokenBeginsWithDigit) {
                sb.append(token);
            } else {
                sb.append(' ').append(token);
            }
            lastTokenEndsInDigit = Character.isDigit(token.charAt(token.length() - 1));
        }
        return orphanSRemover.normalize(sb.toString()).trim();
    }

    public double shinglesFound(Set<String> shortShingles, Set<String> longShingles) {
        if (shortShingles.size() == 0) return 0d;
        Set<String> intersection = new HashSet<String>(shortShingles);
        intersection.retainAll(longShingles);
        return intersection.size() / (double) shortShingles.size();
    }

    public Place cleanPlace(Place place) {
        TitledContentArray tca = new TitledContentArray();
        for (SimpleTitledTextContent sttc : place.getDescriptions()) {
            tca.add(new SimpleTitledTextContent(sttc.getId(), clean(sttc.getTitle()), clean(sttc.getText())));
        }

        Place outPlace = new Place().setId(place.getId())
                .setUrl(place.getUrl())
                .setName(clean(parensRemover.normalize(place.getName())))
                .setAddress(clean(place.getAddress()))
                .setLocality(clean(place.getLocality()))
                .setRegion(clean(place.getRegion()))
                .setPostalCode(clean(place.getPostalCode()))
                .setPhone(clean(place.getPhone()))
                .setDescriptions(tca);

        // remove city name from the end of name if name larger than city name
        if (!outPlace.getName().isEmpty() && !outPlace.getLocality().isEmpty()) {
            if (outPlace.getName().endsWith(outPlace.getLocality()) &&
                    outPlace.getName().length() > outPlace.getLocality().length()) {
                outPlace.setName(outPlace.getName().replace(outPlace.getLocality(), "").trim());
            }
        }

        // remove ltd llc limited from end of name
        for (String tail : TAILS) {
            if (outPlace.getName().endsWith(tail)) {
                outPlace.setName(outPlace.getName().replace(tail, "").trim());
                break;
            }
        }

        return outPlace;
    }

    /**
     * For each attribute/value pair, if value is non-empty check to see if the length of the value is longer than or
     * equal to shingle size. If it is, store the set of shingles for that value for fuzzy matching, otherwise, store
     * the string value for regexp matching.
     *
     * @param key   attribute name, e.g., name, address, phone number, etc.
     * @param value string value of attribute
     * @param map   attribute -> {string value | set of shingles of value}
     */
    private void addAttrib(String key, String value, Map<String, Object> map) {
        if (!value.isEmpty()) {
            if (value.length() >= SHINGLE_SIZE) {
                map.put(key, new ShinglesSizePair(shinglesTokenizer.getUniqueTokens(value), value.length()));
            } else {
                map.put(key, Pattern.compile(String.format("\\b%s\\b", value)));
            }
        }
    }

    private Map<String, Object> getPlaceAttributes(Place place) {
        Map<String, Object> attribs = new HashMap<String, Object>();
        addAttrib(NAME, place.getName(), attribs);
        addAttrib(ADDRESS, place.getAddress(), attribs);
        addAttrib(LOCALITY, place.getLocality(), attribs);
        addAttrib(REGION, place.getRegion(), attribs);
        addAttrib(POSTAL_CODE, place.getPostalCode(), attribs);
        addAttrib(PHONE, place.getPhone(), attribs);
        return attribs;
    }

    @Override
    public Analysis analyze(Analyzable a) throws AnalyzerFailureException {
        if (a == null) return null;

        if (!(a instanceof Place)) {
            throw new AnalyzerFailureException("Analyzable must be an instance of Place.");
        }
        Place place = cleanPlace((Place) a);
        MultiScoreAnalysis analysis = new MultiScoreAnalysis(place.getId());
        Map<String, String> bestText = new HashMap<String, String>();

        //NOTE: I'm not including the check for known aggregators here. That should happen before
        //      this analyzer is called.

        Map<String, Object> placeAttributes = getPlaceAttributes(place);
        for (SimpleTitledTextContent sttc : place.getDescriptions()) {
            String text = clean(sttc.getTitle() + ' ' + sttc.getText());
            Set<String> found = new HashSet<String>();
            for (Map.Entry<String, Object> attributeValue : placeAttributes.entrySet()) {

                String attribute = attributeValue.getKey();
                Object value = attributeValue.getValue();
                double score = 0d;
                boolean addBestText = false;

                if (value instanceof ShinglesSizePair) {
                    // Value has been shingled; fuzzy find
                    score = shinglesFound(((ShinglesSizePair) value).shingles, shinglesTokenizer.getUniqueTokens(text));
                    addBestText = true;
                } else {
                    // Value is string; regexp match
                    Matcher matcher = ((Pattern) value).matcher(text);
                    if (matcher.find()) {
                        score = 1d;
                        found.add(attribute);
                    }
                }

                if (score > 0d) {
                    Double existingscore = analysis.get(attribute);
                    if (existingscore == null || score > existingscore) {
                        analysis.put(attribute, score);
                        if (addBestText) {
                            bestText.put(attribute, text);
                        }
                    }
                }
            }

            // If we've exactly found one of the attributes, remove it from the list of things to search for.
            for (String attrib : found) {
                placeAttributes.remove(attrib);
            }

            // If nothing else to look for, short circuit.
            if (placeAttributes.isEmpty()) {
                break;
            }
        }

        // Now we have some candidate texts that may contain the attributes we're looking for. Perform a linear search
        // in the candidate texts.
        for (Map.Entry<String, String> attributeValue : bestText.entrySet()) {
            String attribute = attributeValue.getKey();
            String text = attributeValue.getValue();
            analysis.put(attribute, linearSearch((ShinglesSizePair) placeAttributes.get(attribute), text));
        }

        // Calculate domain / name similarity
        if (placeAttributes.containsKey(NAME)) {
            analysis.put(DOMAIN_NAME_SIMILARITY, calculateDomainNameSimilarity(place.getUrl(), place.getName()));
        }

        return analysis;
    }

    private double calculateDomainNameSimilarity(String url, String name) {
        String domainName = clean(domainNameNormalizer.normalize(url)).replace(" ", "");
        name = name.replace(" ", "");
        int shingleSize = Math.min(domainName.length(), name.length());

        if (shingleSize == 0) return 0d;

        int oldShingleSize = shinglesTokenizer.getShingleSize();
        shinglesTokenizer.setShingleSize(shingleSize);
        Set<String> nameShingles = shinglesTokenizer.getUniqueTokens(name);
        Set<String> domainShingles = shinglesTokenizer.getUniqueTokens(domainName);
        shinglesTokenizer.setShingleSize(oldShingleSize);

        double score = 0d;
        Set<String> union = new HashSet<String>(nameShingles);
        if (!union.isEmpty()) {
            Set<String> intersection = new HashSet<String>(nameShingles);
            intersection.retainAll(domainShingles);
            score = intersection.size() / (double) union.size();
        }
        return score;
    }

    private double linearSearch(ShinglesSizePair shinglesSizePair, String text) {
        if (StringUtils.isEmpty(text)) return 0d;

        double maxScore = 0d;
        int windowSize = (int) (shinglesSizePair.size * 1.3); // Add some slack for additional characters in text
        if (windowSize < text.length()) {
            for (int i = 0; i < text.length() - windowSize; ++i) {
                double score = shinglesFound(shinglesSizePair.shingles,
                        shinglesTokenizer.getUniqueTokens(text.substring(i, i + windowSize)));
                if (score > maxScore)
                    maxScore = score;
            }
        } else {
            maxScore = shinglesFound(shinglesSizePair.shingles, shinglesTokenizer.getUniqueTokens(text));
        }
        return maxScore;
    }

    private class ShinglesSizePair {
        Set<String> shingles;
        int size; // The size of the original string from which shingles were generated.

        private ShinglesSizePair(Set<String> shingles, int size) {
            this.shingles = shingles;
            this.size = size;
        }
    }

    @Override
    public void shutdown() {
    }
}
