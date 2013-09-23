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

package com.groupon.util.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.LinkedList;
import java.util.List;

/**
 * @author alasdair@groupon.com
 */
public class XmlDomWriter implements XmlWriter {
    private Document fDocument;
    private Element fRoot;
    private List<Element> fStack = new LinkedList<Element>();

    public XmlDomWriter(Document doc) {
        fDocument = doc;
    }

    public Element getRoot() {
        return fRoot;
    }

    @Override
    public XmlWriter cdata(String text) {
        fStack.get(0).appendChild(fDocument.createCDATASection(text));
        return this;
    }

    @Override
    public void close() {
        fStack.clear();
    }

    @Override
    public XmlWriter closeElement() {
        fStack.remove(0);
        return this;
    }

    @Override
    public XmlWriter closeElement(String tag) {
        for (; ; ) {
            Element el = fStack.remove(0);
            if (el.getTagName().equals(tag))
                break;
        }
        return this;
    }

    @Override
    public XmlWriter closeElements() {
        fStack.clear();
        return this;
    }

    @Override
    public XmlWriter element(String tag, Object... attrs) {
        openElement(tag, attrs);
        closeElement();
        return this;
    }

    @Override
    public void flush() {
    }

    @Override
    public XmlWriter openElement(String tag, Object... attrs) {
        Element el = fDocument.createElement(tag);
        setAttributes(el, attrs);
        if (fStack.size() > 0) {
            fStack.get(0).appendChild(el);
        }
        fStack.add(0, el);
        if (fRoot == null) {
            fRoot = el;
        }
        return this;
    }

    @Override
    public XmlWriter openTag(String tag, Object... attrs) {
        openElement(tag, attrs);
        closeElement();
        return this;
    }

    private void setAttributes(Element el, Object... attrs) {
        for (int i = 0; i < attrs.length; i += 2) {
            if (attrs[i + 1] != null) {
                el.setAttribute(attrs[i].toString(), attrs[i + 1].toString());
            }
        }
    }

    @Override
    public XmlWriter tag(String tag, Object value, Object... attrs) {
        openElement(tag, attrs);
        if (value != null) {
            fStack.get(0).appendChild(fDocument.createTextNode(value.toString()));
        }
        closeElement();
        return this;
    }

    @Override
    public XmlWriter tag(String tag, String value, Object... attrs) {
        return tag(tag, (Object) value, attrs);
    }

    @Override
    public XmlWriter text(String text) {
        fStack.get(0).appendChild(fDocument.createTextNode(text));
        return this;
    }

    @Override
    public XmlWriter writeXML(XmlWritable obj) {
        if (obj != null)
            obj.toXML(this);
        return this;
    }
}
