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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.BreakIterator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

/**
 * @author npendar@groupon.com
 */
public final class SimpleSentenceBreaker implements Iterable<String> {

    public static final HashSet<String> abbr = new HashSet<String>();

    static {
        abbr.add("capt.");
        abbr.add("col.");
        abbr.add("dr.");
        abbr.add("etc.");
        abbr.add("gen.");
        abbr.add("gov.");
        abbr.add("lt.");
        abbr.add("mr.");
        abbr.add("mrs.");
        abbr.add("mt.");
        abbr.add("prof.");
        abbr.add("rev.");
        abbr.add("sgt.");
        abbr.add("vs.");
    }

    private String txt;

    public SimpleSentenceBreaker(String s) {
        txt = s;
    }

    @Override
    public Iterator<String> iterator() {
        return new SentIterator(txt);
    }

    public static String[] sentDetect(String txt) {
        LinkedList<String> sents = new LinkedList<String>();
        Iterator<String> iter = new SentIterator(txt);
        while (iter.hasNext()) {
            sents.add(iter.next());
        }
        return sents.toArray(new String[1]);
    }

    public static void main(String[] args) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            while ((line = in.readLine()) != null) {
                for (String s : SimpleSentenceBreaker.sentDetect(line)) {
                    System.out.println(s);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading input." + e);
            System.exit(1);
        }
    }
}


class SentIterator implements Iterator<String> {
    private BreakIterator bi;
    private String sent;
    private int start;
    private String txt;

    public SentIterator(String txt) {
        this.txt = txt;
        bi = BreakIterator.getSentenceInstance(Locale.US);
        bi.setText(txt);
        start = bi.first();
    }

    @Override
    public boolean hasNext() {
        for (int end = bi.next(); end != BreakIterator.DONE; end = bi.next()) {
            String s = txt.substring(start, end).trim();
            String lastWord = s.substring(s.lastIndexOf(' ') + 1).toLowerCase();
            if (SimpleSentenceBreaker.abbr.contains(lastWord)) {
                continue;
            }
            sent = s;
            start = end;
            return true;
        }
        return false;
    }

    @Override
    public String next() {
        return sent;
    }

    @Override
    public void remove() {
        throw new RuntimeException("Remove is not implemented.");
    }
}
