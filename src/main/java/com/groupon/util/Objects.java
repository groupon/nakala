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

/**
 * @author alasdair@groupon.com
 */
public final class Objects {

    public static int countNull(Object... ol) {
        int n = 0;
        for (Object o : ol)
            if (o == null)
                n++;
        return n;
    }

    public static <T> T newInstance(String name, Class<T> cls) {
        try {
            Class<?> cl = Class.forName(name);
            if (!cls.isAssignableFrom(cl))
                throw new IllegalArgumentException(cl.getName() + " not compatible with " + cls.getName());
            return (T) cl.newInstance();
        } catch (Throwable t) {
            if (t instanceof RuntimeException)
                throw (RuntimeException) t;
            throw new RuntimeException(t);
        }
    }

    public static int hashCode(Object... objs) {
        int hc = 0;
        for (Object obj : objs) {
            hc = hc * hc + (obj == null ? 0 : obj.hashCode());
        }
        return hc;
    }

    public static <T extends Object> T coalesce(T... objs) {
        for (T obj : objs)
            if (obj != null)
                return obj;
        return null;
    }

    public static Object[] varargs(Object... vals) {
        if (vals.length == 1 && vals[0] instanceof Object[]) {
            return (Object[]) vals[0];
        }
        return vals;
    }

    public static boolean equals(Object o1, Object o2) {
        if (o1 == o2)
            return true;
        if (o1 == null)
            return o2 == null;
        if (o2 == null)
            return false;
        return o1.equals(o2);
    }
}
