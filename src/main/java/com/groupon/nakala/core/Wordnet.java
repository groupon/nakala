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

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.dictionary.MorphologicalProcessor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author npendar@groupon.com
 */
public final class Wordnet {
    private static final String PROPERTIES_FILE = "/nakala/wordnet/config/file_properties.xml";

    private Dictionary dict;
    private PointerUtils pu;
    private MorphologicalProcessor morph;

    private Wordnet() {
    }

    public static Wordnet getInstance() throws FileNotFoundException, JWNLException, IOException {
        Wordnet wn = new Wordnet();
        InputStream is = wn.getClass().getResourceAsStream(PROPERTIES_FILE);

        if (is == null) {
            String path = System.getProperty("java.class.path");
            throw new IOException("Can't find " + PROPERTIES_FILE
                    + " in the context " + path);
        }
        JWNL.initialize(is);
        wn.dict = Dictionary.getInstance();
        wn.morph = wn.dict.getMorphologicalProcessor();
        wn.pu = PointerUtils.getInstance();
        return wn;
    }

    public String getLemma(POS pos, String word) throws JWNLException {
        synchronized (this.dict) {
            synchronized (this.morph) {
                synchronized (pos) {
                    IndexWord iw = morph.lookupBaseForm(pos, word);
                    if (iw != null) {
                        return iw.getLemma();
                    }
                }
            }
        }
        return null;
    }

    public Dictionary getDict() {
        return dict;
    }

    public PointerUtils getPointerUtils() {
        return pu;
    }

    public synchronized Map<Long, Set<String>> getRelated(POS pos,
                                                          PointerType type, String word) throws JWNLException {
        return getRelated(pos, type, word, -1);
    }

    public Map<Long, Set<String>> getRelated(POS pos, PointerType type,
                                             String word, int max) throws JWNLException {
        Map<Long, Set<String>> synset2words = Collections
                .synchronizedMap(new HashMap<Long, Set<String>>());
        synchronized (this.dict) {
            IndexWord iw = null;
            synchronized (pos) {
                iw = dict.lookupIndexWord(pos, word);
            }
            if (iw != null) {
                int size = 0;
                for (Synset s : iw.getSenses()) {
                    Set<String> ws = Collections
                            .synchronizedSet(new HashSet<String>());
                    Pointer[] pointerArr;
                    synchronized (type) {
                        pointerArr = s.getPointers(type);
                        for (Pointer p : pointerArr) {
                            Synset target = p.getTargetSynset();
                            for (Word w : target.getWords()) {
                                ws.add(w.getLemma());
                                if (++size == max) {
                                    return synset2words;
                                }
                            }
                        }
                    }
                    if (!ws.isEmpty()) {
                        synset2words.put(s.getOffset(), ws);
                    }
                }
            }
        }
        return synset2words;
    }

    public static void main(String[] args) throws JWNLException, IOException {
        Wordnet wn = Wordnet.getInstance();

        System.out.println("Antonyms of 'dog':\n  "
                + wn.getRelated(POS.NOUN, PointerType.ANTONYM, "dog"));
        System.out.println("Antonyms of 'big':\n  "
                + wn.getRelated(POS.ADJECTIVE, PointerType.ANTONYM, "big"));
        System.out.println("Antonyms of 'small':\n  "
                + wn.getRelated(POS.ADJECTIVE, PointerType.ANTONYM, "small"));
        System.out.println("Antonyms of 'come':\n  "
                + wn.getRelated(POS.VERB, PointerType.ANTONYM, "come"));

        System.out.println("Synonyms of 'dog':\n  "
                + wn.getRelated(POS.NOUN, PointerType.SIMILAR_TO, "dog"));
        System.out.println("Synonyms of 'big':\n  "
                + wn.getRelated(POS.ADJECTIVE, PointerType.SIMILAR_TO, "big"));
        System.out
                .println("Synonyms of 'small':\n  "
                        + wn.getRelated(POS.ADJECTIVE, PointerType.SIMILAR_TO,
                        "small"));
        System.out.println("Synonyms of 'come':\n  "
                + wn.getRelated(POS.VERB, PointerType.SIMILAR_TO, "come"));

        System.out.println("Hypernyms of 'dog':\n  "
                + wn.getRelated(POS.NOUN, PointerType.HYPERNYM, "dog"));
        System.out.println("Hypernyms of 'big':\n  "
                + wn.getRelated(POS.ADJECTIVE, PointerType.HYPERNYM, "big"));
        System.out.println("Hypernyms of 'small':\n  "
                + wn.getRelated(POS.ADJECTIVE, PointerType.HYPERNYM, "small"));
        System.out.println("Hypernyms of 'come':\n  "
                + wn.getRelated(POS.VERB, PointerType.HYPERNYM, "come"));

        System.out.println("Hyponyms of 'dog':\n  "
                + wn.getRelated(POS.NOUN, PointerType.HYPONYM, "dog"));
        System.out.println("Hyponyms of 'big':\n  "
                + wn.getRelated(POS.ADJECTIVE, PointerType.HYPONYM, "big"));
        System.out.println("Hyponyms of 'small':\n  "
                + wn.getRelated(POS.ADJECTIVE, PointerType.HYPONYM, "small"));
        System.out.println("Hyponyms of 'come':\n  "
                + wn.getRelated(POS.VERB, PointerType.HYPONYM, "come"));

        System.out.println("Lemma for 'cottages' " + wn.getLemma(POS.NOUN, "cottages"));
        System.out.println("Lemma for 'churches' " + wn.getLemma(POS.NOUN, "churches"));
        System.out.println("Lemma for 'church' " + wn.getLemma(POS.NOUN, "church"));
        System.out.println("Lemma for 'best' " + wn.getLemma(POS.ADJECTIVE, "best"));
        System.out.println("Lemma for 'greatest' " + wn.getLemma(POS.ADJECTIVE, "greatest"));
        System.out.println("Lemma for 'great' " + wn.getLemma(POS.ADJECTIVE, "great"));

    }
}
