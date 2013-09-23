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

import java.util.Set;

/**
 * @author alasdair@groupon.com
 */
public class Predicates {
    public static final Predicate<Object> IS_NULL = new Predicate<Object>() {
        @Override
        public boolean matches(Object obj) {
            return obj == null;
        }
    };

    public static final Predicate<Object> IS_NOT_NULL = new Predicate<Object>() {
        @Override
        public boolean matches(Object obj) {
            return obj != null;
        }
    };

    public static final Predicate<String> IS_BLANK = new Predicate<String>() {
        @Override
        public boolean matches(String str) {
            return str == null || str.length() == 0;
        }
    };

    public static final <T> Predicate<T> IN(final T... items) {
        return new Predicate<T>() {
            public boolean matches(T obj) {
                if (obj == null) {
                    for (T it : items) {
                        if (it == null) {
                            return true;
                        }
                    }
                } else {
                    for (T it : items) {
                        if (obj.equals(it)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    public static final <T> Predicate<T> IN(final Set<T> items) {
        return new Predicate<T>() {
            public boolean matches(T obj) {
                return items.contains(obj);
            }
        };
    }

    public static final <T> Predicate<T> NOT(final Predicate<T> p) {
        return new Predicate<T>() {
            public boolean matches(T obj) {
                return !p.matches(obj);
            }
        };
    }

    public static final <T> Predicate<T> AND(final Predicate<T>... pl) {
        return new Predicate<T>() {
            public boolean matches(T obj) {
                for (Predicate<T> p : pl) {
                    if (!p.matches(obj))
                        return false;
                }
                return true;
            }
        };
    }

    public static final <T> Predicate<T> OR(final Predicate<T>... pl) {
        return new Predicate<T>() {
            public boolean matches(T obj) {
                for (Predicate<T> p : pl) {
                    if (p.matches(obj))
                        return true;
                }
                return false;
            }
        };
    }
}
