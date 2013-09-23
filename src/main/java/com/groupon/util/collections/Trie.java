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

import com.groupon.util.io.IndentWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author alasdair@groupon.com
 */
public class Trie<T> {

    private Node fRoot = new Node();

    public class Match {
        private int fLength;
        private Node fNode;

        public Match(int length, Node node) {
            fLength = length;
            fNode = node;
        }

        public Match match(String input) {
            return fNode.match(input, fLength);
        }

        @Override
        public String toString() {
            return fLength + ":: " + fNode;
        }
    }

    private class Node {
        private T fValue;
        private Map<String, Node> fKids = new HashMap<String, Node>();

        public Node() {
        }

        public Node(T value) {
            fValue = value;
        }

        public boolean add(String key, T value) {
            Node n = fKids.get(key);
            if (n != null) {
                n.fValue = value;
                return false;
            }
            if (fKids.size() == 0) {
                fKids.put(key, new Node(value));
                return true;
            }
            for (Map.Entry<String, Node> e : fKids.entrySet()) {
                String kk = e.getKey();
                int overlap = overlap(key, kk);
                if (overlap != 0) {
                    if (overlap == kk.length()) {
                        return e.getValue().add(key.substring(overlap), value);
                    } else if (overlap == key.length()) {
                        e.getValue().fValue = value;
                        return true;
                    } else {
                        Node split = new Node();
                        String kn = kk.substring(0, overlap);
                        fKids.remove(kk);
                        fKids.put(kn, split);
                        split.fKids.put(kk.substring(overlap), e.getValue());
                        split.fKids.put(key.substring(overlap), new Node(value));
                        return true;
                    }
                }
            }
            fKids.put(key, new Node(value));
            return false;
        }

        private int overlap(String k1, String k2) {
            int n = Math.min(k1.length(), k2.length());
            for (int i = 0; i < n; i++) {
                char c1 = k1.charAt(i);
                char c2 = k2.charAt(i);
                if (c1 != c2) return i;
            }
            return n;
        }

        public Match match(String input, int off) {
            if (input.length() == 0) {
                return new Match(off, this);
            }
            for (Map.Entry<String, Node> e : fKids.entrySet()) {
                String key = e.getKey();
                int overlap = overlap(key, input);
                if (overlap > 0) {
                    return e.getValue().match(input.substring(overlap), off + overlap);
                }
            }
            return null;
        }

        public void print(IndentWriter iw) throws IOException {
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

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        IndentWriter iw = new IndentWriter(sw);
        try {
            fRoot.print(iw);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return sw.toString();
    }

    public boolean add(String key, T value) {
        return fRoot.add(key, value);
    }

    public Match match(String input) {
        return fRoot.match(input, 0);
    }

    public static void main(String[] args) {
        Trie<Integer> t = new Trie<Integer>();
        t.add("the potato club", 1);
        t.add("potato bar", 2);
        t.add("spuds place", 3);
        t.add("potato place", 4);

        System.out.println(t);
        Trie<Integer>.Match m = t.match("potato");
        System.out.println(m);
        System.out.println(m.match(" bar"));
    }
}
