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

/**
 *
 */
package com.groupon.util;

import java.io.File;
import java.util.regex.Pattern;

/**
 * @author alasdair@groupon.com
 */
public class FileFilter implements Filter<File> {
    private File fRoot;
    private int fRootLength;
    private String fPattern;
    private Pattern fCompiled;

    public FileFilter() {
        this(new File("/"));
    }

    public FileFilter(File root) {
        this(root, "**");
    }

    public FileFilter(File root, String pattern) {
        setRoot(root);
        setPattern(pattern);
    }

    public File getRoot() {
        return fRoot;
    }

    public void setRoot(File root) {
        fRoot = root;
        fRootLength = root.getAbsolutePath().length() + 1;
    }

    public int getRootLength() {
        return fRootLength;
    }

    public void setRootLength(int rootLength) {
        fRootLength = rootLength;
    }

    public void setPattern(String p) {
        fPattern = p;
        fCompiled = Pattern.compile(parsePattern(p));
    }

    public String getPattern() {
        return fPattern;
    }

    private String parsePattern(String p) {
        String[] bits = p.split("/");
        StringBuilder r = new StringBuilder();
        r.append("^");
        for (String b : bits) {
            if (b.equals("**")) {
                r.append("(.*/|)");
            } else if ((b.indexOf('*') < 0) && (b.indexOf('?') < 0)) {
                r.append(b);
            } else {
                r.append(b.replaceAll("\\*", "[^/]*"));
            }
        }
        r.append("$");
        return r.toString();
    }

    @Override
    public boolean accept(File f) {
        String path = f.getAbsolutePath().substring(fRootLength);
        return fCompiled.matcher(path).matches();
    }
}