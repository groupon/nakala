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

package com.groupon.util;

import com.groupon.util.collections.CollectionUtil;
import com.groupon.util.io.IoUtil;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple class for managing a set of synonyms and rewriting of strings to
 * replace all synonyms with their canonical form. Takes care of standard
 * synonyms such as numbers (e.g. 7 becomes seven, 283 becomes two hundred
 * and eighty three) if desired.
 *
 * @author alasdair@groupon.com
 */
public class Synonyms {
    private Pattern fWS = Pattern.compile("\\s+");
    private boolean fbExpandNumbers;
    private Map<Pattern, String> fRegexes = new HashMap<Pattern, String>();
    private Map<String, String> fSynonyms = new HashMap<String, String>();
    private Map<String, String> fReplaces = new HashMap<String, String>();
    private Map<String, String> fOrdinals = CollectionUtil.put(new HashMap<String, String>(),
            "first", "1st",
            "second", "2nd",
            "third", "3rd",
            "fourth", "4th",
            "fifth", "5th",
            "sixth", "6th",
            "seventh", "7th",
            "eighth", "8th",
            "ninth", "9th",
            "tenth", "10th",
            "evelenth", "11th",
            "twelfth", "12th",
            "thirteenth", "13th",
            "fourteenth", "14th",
            "fifteenth", "15th",
            "sixteenth", "16th",
            "seventeenth", "17th",
            "eighteenth", "18th",
            "nineteenth", "19th",
            "twentieth", "20th",
            "thirtieth", "30th",
            "fortieth", "40th",
            "fiftieth", "50th",
            "sixtieth", "60th",
            "seventieth", "70th",
            "eightieth", "80th",
            "ninetieth", "90th",
            "hundredth", "100th",
            "thousandth", "1000th");
    private Map<String, Integer> fCardinals = new HashMap<String, Integer>() {{
        put("one", 1);
        put("two", 2);
        put("three", 3);
        put("four", 4);
        put("five", 5);
        put("six", 6);
        put("seven", 7);
        put("eight", 8);
        put("nine", 9);
        put("ten", 10);
        put("eleven", 11);
        put("twelve", 12);
        put("thirteen", 13);
        put("fourteen", 14);
        put("fifteen", 15);
        put("sixteen", 16);
        put("seventeen", 17);
        put("eighteen", 18);
        put("nineteen", 19);
        put("twenty", 20);
        put("thirty", 30);
        put("forty", 40);
        put("fifty", 50);
        put("sixty", 60);
        put("seventy", 70);
        put("eighty", 80);
        put("ninety", 90);
        put("hundred", 100);
        put("thousand", 1000);
    }};


    private class ParsedStr {
        private String[] fBits;
        private List<String> fOut;
        private int fiBit;
        private int foBit;

        public ParsedStr(String input) {
            fBits = fWS.split(input);
        }

        public String current() {
            return fBits[fiBit];
        }

        public void advance() {
            fiBit++;
        }

        public boolean hasMore() {
            return fiBit < fBits.length;
        }

        public String parsed() {
            if (fOut != null)
                return CollectionUtil.join(" ", fBits);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < foBit; i++) {
                if (sb.length() > 0)
                    sb.append(" ");
                sb.append(fBits[i]);
            }
            return sb.toString();
        }

        public void skip() {
            set(fBits[fiBit++]);
        }

        public void set(String out) {
            if (fOut != null) {
                fOut.add(out);
            } else if (foBit >= fiBit) {
                fOut = new ArrayList<String>(fBits.length + 5);
                for (int i = 0; i < fiBit; i++) {
                    fOut.add(fBits[i]);
                }
                fOut.add(out);
            } else {
                fBits[foBit++] = out;
            }
        }
    }

    public Synonyms() {
    }

    public Synonyms(Map<String, String> syns) {
        fSynonyms = syns;
    }

    public Synonyms setExpandNumbers(boolean b) {
        fbExpandNumbers = b;
        return this;
    }

    public boolean getExpandNumbers() {
        return fbExpandNumbers;
    }

    public Synonyms read(Reader r) throws IOException {
        Pattern eq = Pattern.compile("\\s*=\\s*");
        Pattern cm = Pattern.compile("\\s*,\\s*");
        for (String line : IoUtil.readLines(r)) {
            if ((line = line.trim()).length() == 0 || line.startsWith("#"))
                continue;
            String[] parts = eq.split(line, 2);
            String key = parts[0].toLowerCase().replace("\t", " ").trim();
            for (String part : cm.split(parts[1])) {
                part = part.toLowerCase().replace("\t", " ").trim();
                if (part.startsWith("/") && part.endsWith("/")) {
                    fRegexes.put(Pattern.compile(part.substring(1, part.length() - 1)), key);
                } else if (part.indexOf(" ") >= 0) {
                    fReplaces.put(part, key);
                } else {
                    fSynonyms.put(part.toLowerCase(), key);
                }
            }
        }
        return this;
    }

    public String getSynonym(String in) {
        if (in == null)
            return null;
        String out = fSynonyms.get(in);
        if (out == null) {
            if (in.endsWith(".") && in.length() > 1) {
                out = getSynonym(in.substring(0, in.length() - 1));
            }
        }
        return out;
    }

    private boolean getSynonym(ParsedStr ps) {
        String syn = getSynonym(ps.current());
        if (syn == null)
            return false;

        ps.advance();
        ps.set(syn);
        return true;
    }

    private String suffix(int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    private boolean getNumber(ParsedStr ps) {
        if (!fbExpandNumbers)
            return false;

        /*--- look for a series of ordinals followed by a cardinal ... */
        int s = ps.fiBit;
        int i = s;
        for (; i < ps.fBits.length; i++) {
            if (ps.fBits[i] == null)
                continue;
            String ord = fOrdinals.get(ps.fBits[i]);
            if (ord == null) {
                Integer card = fCardinals.get(ps.fBits[i]);
                if (card == null) {
                    boolean bAccept = false;
                    if (ps.fBits[i].equals("and")) {
                        if (i + 1 < ps.fBits.length) {
                            String nxt = ps.fBits[i + 1];
                            if (fOrdinals.containsKey(nxt) || fCardinals.containsKey(nxt)) {
                                /*--- keep going */
                                bAccept = true;
                            }
                        }
                    }
                    if (!bAccept) {
                        /*--- nothing we know or care about - we're done */
                        break;
                    }
                }
            } else {
                /*--- an ordinal - see if we are preceeded by some cardinals e.g. one hundred twenty fifth */
                if (i > s) {
                    /*--- yes - add them all together */
                    int prior = 0;
                    int n = Integer.parseInt(ord.substring(0, ord.length() - 2));
                    for (; s < i; s++) {
                        Integer cur = fCardinals.get(ps.fBits[s]);
                        if (cur == null)
                            continue;
                        if (prior == 0) {
                            prior = cur;
                        } else if (prior > cur) {
                            prior += cur;
                        } else {
                            prior *= cur;
                        }
                        if (cur >= 1000) {
                            n += prior;
                            prior = 0;
                        }
                    }
                    n += prior;
                    /*--- now figure out what the suffix should be... */
                    ps.fiBit = i + 1;
                    ps.set(String.valueOf(n) + suffix(n));
                    return true;
                } else {
                    /*--- a plain old ordinal */
                    ps.advance();
                    ps.set(ord);
                    return true;
                }
            }
        }
        if (i == s)
            return false;
        /*--- just book the numbers as regular strings */
        int prior = 0;
        int n = 0;
        for (; s < i; s++) {
            Integer ord = fCardinals.get(ps.fBits[s]);
            if (ord == null)
                continue;
            if (prior == 0) {
                prior = ord;
            } else if (prior > ord) {
                prior += ord;
            } else {
                prior *= ord;
            }
            if (ord >= 1000) {
                n += prior;
                prior = 0;
            }
        }
        n += prior;
        ps.fiBit = i;
        ps.set(String.valueOf(n));
        return true;
    }

    public String replaceSynonyms(String input) {
        if (input == null)
            return null;
        ParsedStr ps = new ParsedStr(input);
        for (; ps.hasMore(); ) {
            if (!getSynonym(ps)) {
                if (!getNumber(ps)) {
                    ps.skip();
                }
            }
        }
        String parsed = ps.parsed();
        if (fReplaces != null && fReplaces.size() > 0) {
            for (Map.Entry<String, String> e : fReplaces.entrySet()) {
                parsed = parsed.replace(e.getValue(), e.getKey());
            }
        }
        if (fRegexes != null && fRegexes.size() > 0) {
            for (Map.Entry<Pattern, String> e : fRegexes.entrySet()) {
                Matcher m = e.getKey().matcher(parsed);
                if (m.find()) {
                    parsed = m.replaceAll(e.getValue());
                }
            }
        }
        return parsed;
    }
}
