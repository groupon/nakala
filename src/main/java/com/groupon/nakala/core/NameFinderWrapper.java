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

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author npendar@groupon.com
 */
public final class NameFinderWrapper {

    public static final int DATE = 1;
    public static final int LOCATION = 2;
    public static final int MONEY = 4;
    public static final int ORGANIZATION = 8;
    public static final int PERCENTAGE = 16;
    public static final int PERSON = 32;

    private static final Logger logger = Logger.getLogger(NameFinderWrapper.class);

    private static String dateModelFile = "/nakala/opennlp_models/en-ner-date.bin";
    private static String locationModelFile = "/nakala/opennlp_models/en-ner-location.bin";
    private static String moneyModelFile = "/nakala/opennlp_models/en-ner-money.bin";
    private static String organizationModelFile = "/nakala/opennlp_models/en-ner-organization.bin";
    private static String percentageModelFile = "/nakala/opennlp_models/en-ner-percentage.bin";
    private static String personModelFile = "/nakala/opennlp_models/en-ner-person.bin";

    private Map<Integer, NameFinderME> ner;

    private NameFinderWrapper() {
    }

    public static NameFinderWrapper getInstance(int types) throws InvalidFormatException, IOException {
        NameFinderWrapper n = new NameFinderWrapper();
        n.ner = new HashMap<Integer, NameFinderME>();
        if ((types & DATE) != 0) {
            n.ner.put(DATE, nerFromFile(n.getClass(), dateModelFile));
        }

        if ((types & LOCATION) != 0) {
            n.ner.put(LOCATION, nerFromFile(n.getClass(), locationModelFile));
        }

        if ((types & MONEY) != 0) {
            n.ner.put(MONEY, nerFromFile(n.getClass(), moneyModelFile));
        }

        if ((types & ORGANIZATION) != 0) {
            n.ner.put(ORGANIZATION, nerFromFile(n.getClass(), organizationModelFile));
        }

        if ((types & PERCENTAGE) != 0) {
            n.ner.put(PERCENTAGE, nerFromFile(n.getClass(), percentageModelFile));
        }

        if ((types & PERSON) != 0) {
            n.ner.put(PERSON, nerFromFile(n.getClass(), personModelFile));
        }

        return n;
    }

    @SuppressWarnings("rawtypes")
    private static NameFinderME nerFromFile(Class c, String modelFile) throws InvalidFormatException, IOException {
        InputStream in = c.getResourceAsStream(modelFile);
        TokenNameFinderModel model = new TokenNameFinderModel(in);
        return new NameFinderME(model);
    }

    public Span[] find(String[] tokens, int type) {
        try {
            return ner.get(type).find(tokens);
        } catch (NullPointerException e) {
            logger.warn("NER model for type " + type + " not loaded.");
            return null;
        }
    }

    public static void main(String[] args) {
        String[] tokens = {"John", "Jameson", ",", "who", "was", "from",
                "Ireland", ",", "was", "a", "great", "bewer"};
        try {
            NameFinderWrapper nfw = NameFinderWrapper.getInstance(PERSON | LOCATION);
            Span[] spans = nfw.find(tokens, PERSON);
            for (String token : tokens) {
                System.out.println(token);
            }
            System.out.println("Person:");
            for (Span span : spans) {
                System.out.println(span);
            }
            spans = nfw.find(tokens, LOCATION);
            System.out.println("\nLocation:");
            for (Span span : spans) {
                System.out.println(span);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
