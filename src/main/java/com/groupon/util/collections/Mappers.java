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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author alasdair@groupon.com
 */
public class Mappers {

    public static final Mapper<String, String> APPEND(final String suffix) {
        return new Mapper<String, String>() {
            @Override
            public String map(String key) {
                return key == null ? null : (key + suffix);
            }
        };
    }

    public static final Mapper<String, String> PREPEND(final String prefix) {
        return new Mapper<String, String>() {
            @Override
            public String map(String key) {
                return key == null ? null : (prefix + key);
            }
        };
    }

    public static final Mapper<String, String> REPLACE(final String s, final String with) {
        return new Mapper<String, String>() {
            @Override
            public String map(String key) {
                return key == null ? null : s.replace(s, with);
            }
        };
    }

    public static final Mapper<String, String> REPLACE(final Pattern p, final String with) {
        return new Mapper<String, String>() {
            @Override
            public String map(String input) {
                if (input == null)
                    return null;
                Matcher m = p.matcher(input);
                if (m.matches()) {
                    return m.replaceAll(with);
                }
                return input;
            }
        };
    }
}
