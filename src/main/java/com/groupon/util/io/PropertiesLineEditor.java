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

package com.groupon.util.io;

import java.util.HashMap;
import java.util.Map;

/**
 * @author alasdair@groupon.com
 */
public class PropertiesLineEditor implements LineEditor {

    private Map<String, String> fProperties;

    public PropertiesLineEditor() {
        this(new HashMap<String, String>());
    }

    public PropertiesLineEditor(Map<String, String> properties) {
        fProperties = properties;
    }

    public Map<String, String> getProperties() {
        return fProperties;
    }

    @Override
    public String edit(String line) {
        if (line == null)
            return null;
        int off = line.indexOf("@");
        if (off < 0)
            return line;
        for (; off >= 0; ) {
            int end = line.indexOf("@", off + 1);
            if (end < 0)
                break;
            String tok = line.substring(off + 1, end);
            String rep = fProperties.get(tok);
            if (rep == null) {
                if (fProperties.keySet().contains(tok)) {
                    rep = "";
                } else {
                    rep = "'`!" + tok + "'`!";
                }
            }
            line = line.substring(0, off) + rep + line.substring(end + 1);
            off = line.indexOf("@");
        }
        return line.replace("'`!", "@");
    }
}
