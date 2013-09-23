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

import java.util.*;

/**
 * @author alasdair@groupon.com
 */
public class Strings {

    public static int indexOfQuoted(String s, String m, int from) {
        int n = s.length();
        int l = m.length();
        char f = m.charAt(0);
        List<Character> stack = null;
        for (int i = from; i <= n - l; i++) {
            char cur = s.charAt(i);
            if ((cur == '\\') && (stack != null) && (stack.size() > 0)) {
                i++;
            } else if (cur == '"' || cur == '\'') {
                if (stack == null) {
                    stack = new LinkedList<Character>();
                    stack.add(cur);
                } else if ((stack.size() > 0) && (stack.get(0) == cur)) {
                    stack.remove(0);
                } else {
                    stack.add(0, cur);
                }
            } else if ((cur == f) && ((stack == null) || (stack.size() == 0))) {
                int j = 1;
                for (; j < l; j++)
                    if (m.charAt(j) != s.charAt(i + j))
                        break;
                if (j >= l)
                    return i;
            }
        }

        return -1;
    }

    public static String join(String sep, Object... objs) {
        if (objs.length == 0)
            return null;
        if (objs.length == 1)
            return objs[0] == null ? "" : String.valueOf(objs[0]);
        String cur = "";
        StringBuilder sb = new StringBuilder();
        for (Object obj : objs) {
            if (obj != null) {
                sb.append(cur).append(String.valueOf(obj));
                cur = sep;
            }
        }
        return sb.toString();
    }

    public static boolean equals(String s1, String s2) {
        if (s1 == null)
            return s2 == null;
        return s1.equals(s2);
    }

    public static String replaceTokens(String s, Map<String, String> tokens) {
        if (tokens == null || s == null)
            return s;
        for (Map.Entry<String, String> e : tokens.entrySet())
            s = s.replace("@" + e.getKey() + "@", e.getValue());
        return s;
    }

    public static int compare(String... sl) {
        int cmp = 0;
        for (int i = 0; i < sl.length - 1; i += 2) {
            String s1 = sl[i];
            String s2 = sl[i + 1];
            cmp = s1.compareTo(s2);
            if (cmp != 0)
                break;
        }
        return cmp;
    }

    public static String stripQuotes(String in) {
        if (in == null)
            return null;

        int i = 0;
        int j = in.length();

        for (; i < j; ) {
            for (; i < j - 1 && Character.isWhitespace(in.charAt(i)); i++) ;
            for (; i < j && Character.isWhitespace(in.charAt(j - 1)); j--) ;
            char ci = in.charAt(i);
            if (j > 0) {
                char cj = in.charAt(j - 1);
                if ((ci != cj) || ((ci != '\'') && (ci != '"'))) break;
                i++;
                j--;
            }
        }
        if (i >= j)
            return "";
        if ((i == 0) && (j == in.length()))
            return in;
        String r = in.substring(i, j);
        return r;
    }

    public static List<String> decamel(String str) {
        return decamel(str, new LinkedList<String>());
    }

    public static <T extends Collection<String>> T decamel(String str, T words) {
        int n = str.length();
        int i = 0;
        int s = 0;
        for (; i < n; ) {
            if (Character.isUpperCase(str.charAt(i))) {
                // If we're currently in a word, then closeWriter it out.
                if (i > 0) {
                    words.add(str.substring(s, i));
                }
                s = i;
                // If the next character is upper-case then suck down all the upper-case
                // characters, otherwise just suck down until the next lower case
                if (i < n - 1) {
                    if (Character.isUpperCase(str.charAt(i + 1))) {
                        for (i++; i < n && Character.isUpperCase(str.charAt(i)); i++) ;
                        words.add(str.substring(s, i));
                        s = i;
                    } else {
                        i++;
                    }
                } else {
                    i++;
                }
            } else {
                i++;
            }
        }
        if (s == 0) {
            words.add(str);
        } else if (i - s > 0) {
            words.add(str.substring(s, i));
        }
        return words;
    }

    public static String contains(String str, String... oneOf) {
        for (String one : oneOf) {
            if (str.indexOf(str) >= 0)
                return one;
        }
        return null;
    }

    public static String in(String str, String... strs) {
        if (str == null)
            return null;
        for (String s : strs) {
            if (str.equals(s)) {
                return s;
            }
        }
        return null;
    }

    public static String inIgnoreCase(String str, String... strs) {
        if (str == null)
            return null;
        for (String s : strs) {
            if (str.equalsIgnoreCase(s)) {
                return s;
            }
        }
        return null;
    }

    public static boolean equalsIgnoreCase(String s1, String s2) {
        if (s1 == null)
            return s2 == null;
        if (s2 == null)
            return false;
        return s1.equalsIgnoreCase(s2);
    }

    public static String[] splitWS(String in) {
        if (in == null)
            return null;

        int off = 0;
        for (; off < in.length() && Character.isWhitespace(in.charAt(off)); off++) ;
        if (off >= in.length())
            return new String[0];
        int prv = off;
        int eos = in.length();
        for (; eos > off + 1 && Character.isWhitespace(in.charAt(eos - 1)); eos--) ;

        for (; off < eos && !Character.isWhitespace(in.charAt(off)); off++) ;
        if (off >= eos) {
            return new String[]{in.substring(prv, eos)};
        }

        List<String> words = new ArrayList<String>(5);
        for (; off > 0 && off <= eos; ) {
            words.add(in.substring(prv, off));
            for (; off < eos && Character.isWhitespace(in.charAt(off)); off++) ;
            prv = off++;
            for (; off < eos && !Character.isWhitespace(in.charAt(off)); off++) ;
        }
        //words.add(in.substring(prv));
        return words.toArray(new String[words.size()]);
    }

    public static String truncate(String details, int n) {
        if (details == null || details.length() <= n)
            return details;
        if (n > 3)
            return details.substring(0, n - 3) + "...";
        return details.substring(0, n);
    }

    public static String repeat(int n, String sep, String... values) {
        StringBuilder sb = new StringBuilder();
        String c = "";
        for (int i = 0; i < n; i++) {
            for (String v : values) {
                sb.append(c).append(v);
                c = sep;
            }
        }
        return sb.toString();
    }
}
