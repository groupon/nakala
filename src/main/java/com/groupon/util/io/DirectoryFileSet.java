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

import com.groupon.util.ConfigureException;
import com.groupon.util.FileFilter;
import com.groupon.util.Filter;
import com.groupon.util.xml.XmlConfigurable;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Models a set of files. Each file set is rooted
 * in a specific directory and can contain any number
 * of inclusion and exclusion filters. Exclusion filters
 * are applied after all exclusion filters.
 *
 * @author alasdair@groupon.com
 */
public class DirectoryFileSet extends AbstractFileSet implements XmlConfigurable {
    private File fRoot;
    private int fRootLength;
    private List<Filter<File>> fInclusions = new LinkedList<Filter<File>>();
    private List<Filter<File>> fExclusions = new LinkedList<Filter<File>>();
    private List<File> fFiles = new LinkedList<File>();

    private class DirIterator implements Iterator<File> {
        private Iterator<File> fExplicit = fFiles.iterator();
        private List<File> fStack = new LinkedList<File>();
        private File fNext;

        public DirIterator() {
            File[] files = fRoot.listFiles();
            if (files != null) {
                for (File f : fRoot.listFiles()) {
                    fStack.add(f);
                }
                advance();
            }
        }

        private void advance() {
            fNext = null;
            if (fExplicit.hasNext()) {
                fNext = fExplicit.next();
                return;
            }
            for (; fStack.size() > 0; ) {
                File cur = fStack.remove(0);
                if (cur.isDirectory()) {
                    File[] l = cur.listFiles();
                    if (l != null) {
                        for (int i = l.length - 1; i >= 0; i--) {
                            fStack.add(0, l[i]);
                        }
                    }
                } else {
                    boolean bAccepted = false;
                    for (Filter<File> inc : fInclusions) {
                        if (bAccepted = inc.accept(cur)) break;
                    }
                    if (bAccepted) {
                        boolean bExcluded = false;
                        for (Filter<File> exc : fExclusions) {
                            if (bExcluded = exc.accept(cur)) break;
                        }
                        if (!bExcluded) {
                            fNext = cur;
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public boolean hasNext() {
            return fNext != null;
        }

        @Override
        public File next() {
            try {
                return fNext;
            } finally {
                advance();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public DirectoryFileSet() {
        this(null);
    }

    public DirectoryFileSet(File root) {
        root = root == null ? new File(System.getProperty("user.dir")) : root;
        setRoot(root);
    }

    public File getRoot() {
        return fRoot;
    }

    public DirectoryFileSet setRoot(File f) {
        fRoot = f;
        fRootLength = f.getAbsolutePath().length();
        return this;
    }

    public List<Filter<File>> getInclusions() {
        return fInclusions;
    }

    public List<Filter<File>> getExclusions() {
        return fExclusions;
    }

    @Override
    public void configure(Element el) throws ConfigureException {
        String dir = el.attributeValue("dir");
        if (dir == null)
            throw new ConfigureException("Missing dir attribute on fileset.");
        fRoot = new File(dir);
        parsePatterns(el.selectNodes("include"), fInclusions);
        parsePatterns(el.selectNodes("exclude"), fExclusions);

        String files = el.attributeValue("files");
        if (files != null) {
            StringTokenizer st = new StringTokenizer(files, ",");
            fFiles.add(new File(st.nextToken().trim()));
        }

        List<Element> fileEls = el.selectNodes("file");
        for (Element f : fileEls) {
            fFiles.add(new File(f.getTextTrim()));
        }
    }

    private void parsePatterns(List<Element> els, List<Filter<File>> filters) throws ConfigureException {
        for (Element el : els) {
            String name = el.attributeValue("name");
            if (name == null)
                throw new ConfigureException("Missing name on include/exclude in filterset");
            filters.add(new FileFilter(fRoot, name));
        }
    }

    @Override
    public Iterator<File> iterator() {
        return new DirIterator();
    }

    public void addIncludePattern(String pattern) {
        fInclusions.add(new FileFilter(fRoot, pattern));
    }

    public void addExcludePattern(String pattern) {
        fExclusions.add(new FileFilter(fRoot, pattern));
    }

    public void add(File file) {
        fFiles.add(file);
    }
}
