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

import org.apache.lucene.search.spans.SpanQuery;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author npendar@groupon.com
 */
public final class VocabularyReview extends AbstractVocabulary {

    public static final String AMBIANCE = "ambiance";
    public static final String AQUARIUM = "aquarium";
    public static final String BALCONY = "balcony";
    public static final String BALLET = "ballet";
    public static final String BASEBALL = "baseball";
    public static final String BASE = "base";
    public static final String BEACH = "beach";
    public static final String BIKE = "bike";
    public static final String BOWL = "bowl";
    public static final String CLEANLINESS = "cleanliness";
    public static final String CLOTHING = "clothing";
    public static final String DIVING = "diving";
    public static final String DYNAMICFEAT = "dynamicfeat";
    public static final String FAMILY = "family";
    public static final String FOOD = "food";
    public static final String GIRLSGETAWAY = "girlsgetaway";
    public static final String GOLF = "golf";
    public static final String HIKE = "hike";
    public static final String JEWELRY = "jewelry";
    public static final String LOBBY = "lobby";
    public static final String LOCATION_AIRPORT = "location_airport";
    public static final String LOCATION = "location";
    public static final String LOUNGE = "lounge";
    public static final String LUXURY = "luxury";
    public static final String MARKET = "market";
    public static final String MUSEUM = "museum";
    public static final String NIGHTLIFE = "nightlife";
    public static final String OCEAN = "ocean";
    public static final String OVERALL = "overall";
    public static final String PARKING = "parking";
    public static final String PET = "pet";
    public static final String PLAYGROUND = "playground";
    public static final String QUIET = "quiet";
    public static final String ROMANCE = "romance";
    public static final String ROOMCOMFORTABLE = "roomcomfortable";
    public static final String ROOMSERVICE = "roomservice";
    public static final String ROOMSIZE = "room_size";
    public static final String SIGHTSEEING = "sightseeing";
    public static final String SKI = "ski";
    public static final String SPA = "spa";
    public static final String STAFF = "staff";
    public static final String SWIM = "swim";
    public static final String THEMEPARK = "themepark";
    public static final String TRANSPORTATION = "transportation";
    public static final String VALUE = "value";
    public static final String VIEW = "view";
    public static final String WATERSPORTS = "watersports";
    public static final String WOMALONE = "womalone";
    public static final String WOULDRETURN = "would_return";
    public static final String ZOO = "zoo";

    private static final String FEATURES = "FEATURES";
    private static final String PROHIBITED = "PROHIBITED";
    private static final String PLUS_ONE = "PLUS_ONE";
    private static final String PLUS_HALF = "PLUS_HALF";
    private static final String MINUS_HALF = "MINUS_HALF";
    private static final String MINUS_ONE = "MINUS_ONE";

    private SpanQuery featuresSQ, prohibitedSQ, plusOneSQ, minusOneSQ;
    private SpanQuery featuresTitleSQ, prohibitedTitleSQ, plusOneTitleSQ,
            minusOneTitleSQ;

    private VocabularyReview(String vocabName) {
        super(vocabName);
    }

    public static VocabularyReview newInstance(String vocabName) throws IOException {
        VocabularyReview vr = new VocabularyReview(vocabName);
        vr.vocabPath = vr.vocabPath + "review/";

        vr.labels = Collections.synchronizedSet(new HashSet<String>());
        vr.labels.add(FEATURES);
        vr.labels.add(PROHIBITED);
        vr.labels.add(PLUS_ONE);
        vr.labels.add(MINUS_ONE);

        vr.wordsOfInterest = Collections.synchronizedSet(new HashSet<Pattern>());

        vr.queryMapContents = Collections.synchronizedMap(new HashMap<String, Set<SpanQuery>>());
        vr.queryMapContents.put(FEATURES, new HashSet<SpanQuery>());
        vr.queryMapContents.put(PROHIBITED, new HashSet<SpanQuery>());
        vr.queryMapContents.put(PLUS_ONE, new HashSet<SpanQuery>());
        vr.queryMapContents.put(PLUS_HALF, new HashSet<SpanQuery>());
        vr.queryMapContents.put(MINUS_ONE, new HashSet<SpanQuery>());
        vr.queryMapContents.put(MINUS_HALF, new HashSet<SpanQuery>());

        vr.queryMapTitle = Collections.synchronizedMap(new HashMap<String, Set<SpanQuery>>());
        vr.queryMapTitle.put(FEATURES, new HashSet<SpanQuery>());
        vr.queryMapTitle.put(PROHIBITED, new HashSet<SpanQuery>());
        vr.queryMapTitle.put(PLUS_ONE, new HashSet<SpanQuery>());
        vr.queryMapTitle.put(PLUS_HALF, new HashSet<SpanQuery>());
        vr.queryMapTitle.put(MINUS_ONE, new HashSet<SpanQuery>());
        vr.queryMapTitle.put(MINUS_HALF, new HashSet<SpanQuery>());

        Set<String> fields = Collections.synchronizedSet(new HashSet<String>());
        fields.add(FEATURES);
        fields.add(PROHIBITED);
        fields.add(PLUS_ONE);
        fields.add(PLUS_HALF);
        fields.add(MINUS_ONE);
        fields.add(MINUS_HALF);

        vr.readVocabFile(vr.domain, fields, null, ExcerptIndexer.CONTENTS, vr.queryMapContents);
        vr.readVocabFile(vr.domain, fields, null, ExcerptIndexer.TITLE, vr.queryMapTitle);
        return vr;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String[] labelList = {FEATURES, PROHIBITED, PLUS_ONE, PLUS_HALF,
                MINUS_HALF, MINUS_ONE};
        for (String l : labelList) {
            if (queryMapContents.get(l).isEmpty()) {
                continue;
            }
            List<SpanQuery> entries = Collections.synchronizedList(new ArrayList<SpanQuery>(queryMapContents
                    .get(l)));
            sb.append(l + '\n');
            for (SpanQuery e : entries) {
                sb.append("  " + e.toString() + '\n');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private SpanQuery getQuery(SpanQuery sq, Map<String, Set<SpanQuery>> queryMapContents, String field) {
        if (sq == null) {
            sq = VocabUtils.buildQueryFromSpanQuerySet(queryMapContents.get(field));
        }
        return sq;
    }

    public SpanQuery getFeatures() {
        return getQuery(featuresSQ, queryMapContents, FEATURES);
    }

    public SpanQuery getProhibited() {
        return getQuery(prohibitedSQ, queryMapContents, PROHIBITED);
    }

    public SpanQuery getAttitudePlusOne() {
        return getQuery(plusOneSQ, queryMapContents, PLUS_ONE);
    }

    public SpanQuery getAttitudeMinusOne() {
        return getQuery(minusOneSQ, queryMapContents, MINUS_ONE);
    }

    public SpanQuery getFeaturesTitle() {
        return getQuery(featuresTitleSQ, queryMapTitle, FEATURES);
    }

    public SpanQuery getProhibitedTitle() {
        return getQuery(prohibitedTitleSQ, queryMapTitle, PROHIBITED);
    }

    public SpanQuery getAttitudePlusOneTitle() {
        return getQuery(plusOneTitleSQ, queryMapTitle, PLUS_ONE);
    }

    public SpanQuery getAttitudeMinusOneTitle() {
        return getQuery(minusOneTitleSQ, queryMapTitle, MINUS_ONE);
    }

    public static void main(String[] args) throws Exception {
        VocabularyReview vocab = VocabularyReview
                .newInstance(VocabularyReview.FAMILY);
        System.out.println("==========\nfamily\n==========");
        System.out.print(vocab.toString());

        vocab = VocabularyReview.newInstance(VocabularyReview.AMBIANCE);
        System.out.println("\n\n==========\nambiance\n==========");
        System.out.print(vocab.toString());

        vocab = VocabularyReview.newInstance(VocabularyReview.NIGHTLIFE);
        System.out.println("\n\n==========\nnightlife\n==========");
        System.out.print(vocab.toString());

        vocab = VocabularyReview.newInstance(VocabularyReview.SPA);
        System.out.println("\n\n==========\nspa\n==========");
        System.out.print(vocab.toString());
    }
}
