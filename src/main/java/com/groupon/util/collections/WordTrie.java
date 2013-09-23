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

package com.groupon.util.collections;

import com.groupon.util.Strings;
import com.groupon.util.io.IndentWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author alasdair@groupon.com
 */
public class WordTrie<T> {

    private Node fRoot = new Node();

    public class Node {
        private T fValue;
        private Map<String, Node> fKids = new HashMap<String, Node>();

        private void add(String[] words, int iWord, T value) {
            if (iWord >= words.length) {
                fValue = value;
                return;
            }
            Node kid = fKids.get(words[iWord]);
            if (kid == null) {
                fKids.put(words[iWord], kid = new Node());
            }
            kid.add(words, iWord + 1, value);
        }

        private Node match(String[] words, int iWord) {
            if (iWord >= words.length)
                return this;
            Node next = fKids.get(words[iWord]);
            if (next != null)
                return next.match(words, iWord + 1);
            return null;
        }

        public Node match(String[] words) {
            return match(words, 0);
        }

        public Node match(String words) {
            return match(Strings.splitWS(words));
        }

        private void print(IndentWriter iw) throws IOException {
            if (fValue != null) {
                iw.write("[" + fValue + "]\n");
            }
            for (Map.Entry<String, Node> e : fKids.entrySet()) {
                iw.write(e.getKey() + " => \n");
                iw.push();
                e.getValue().print(iw);
                iw.pop();
            }
        }
    }

    public void add(String[] words, T value) {
        fRoot.add(words, 0, value);
    }

    public void add(String words, T value) {
        add(Strings.splitWS(words), value);
    }

    public Node match(String[] words) {
        return fRoot.match(words, 0);
    }

    public Node match(String words) {
        return match(Strings.splitWS(words));
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        IndentWriter iw = new IndentWriter(sw);
        try {
            fRoot.print(iw);
            return sw.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        WordTrie<Integer> words = new WordTrie<Integer>();
        words.add("the potato farm", 1);
        words.add("a spud place", 2);
        words.add("the farm", 3);
        System.out.println(words);
        WordTrie<Integer>.Node n = words.match("the potato");
        System.out.println(n);
        n = n.match("farm");
        System.out.println(n);
    }
}
