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
package com.groupon.util.collections;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * @author alasdair@groupon.com
 */
public class UnionFind<T> {
    private T fObject;
    private UnionFind<T> fParent;
    private Set<UnionFind<T>> fKids;
    private int fnDepth;

    public UnionFind(T object) {
        fObject = object;
        fParent = this;
        fnDepth = 0;
    }

    protected UnionFind() {
        this(null);
    }

    protected void setObject(T object) {
        fObject = object;
    }

    public T getObject() {
        return fObject;
    }

    public UnionFind<T> find() {
        if (fParent == this)
            return this;
        return setParent(fParent.find());
    }

    public UnionFind<T> union(UnionFind<T> with) {
        if (with == this)
            return this;
        if (with.fnDepth > fnDepth)
            return setParent(with);
        if (fnDepth > with.fnDepth)
            return with.setParent(this);
        fnDepth++;
        return with.setParent(this);
    }

    private UnionFind<T> setParent(UnionFind<T> parent) {
        if (fParent != this)
            fParent.fKids.remove(this);
        if (parent.fKids == null)
            parent.fKids = new HashSet<UnionFind<T>>();
        parent.fKids.add(this);
        return fParent = parent;
    }

    public String toString() {
        UnionFind<T> root = find();
        return root.write(new StringWriter()).toString();
    }

    public <W extends Writer> W write(W out) {
        try {
            out.write(String.valueOf(fObject));
            if (fKids != null) {
                for (UnionFind<T> kid : fKids) {
                    out.write(" :: ");
                    kid.write(out);
                }
            }
            return out;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}