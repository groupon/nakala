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

import com.groupon.util.io.IndentWriter;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

/**
 * @author alasdair@groupon.com
 */
public class XmlPrintWriter extends PrintWriter implements XmlWriter {
    private Writer fOut;

    public XmlPrintWriter(Writer w) {
        super(IndentWriter.cast(w));
        fOut = w;
    }

    private boolean fbAutoNL = true;
    private List<String> fTagStack = new LinkedList<String>();

    public boolean getAutoNL() {
        return fbAutoNL;
    }

    public Writer getOut() {
        return fOut;
    }

    public XmlPrintWriter setAutoNL(boolean autoNL) {
        fbAutoNL = autoNL;
        getIndentWriter().setAutoIndent(autoNL);
        return this;
    }

    public IndentWriter getIndentWriter() {
        return (IndentWriter) out;
    }

    @Override
    public void close() {
        closeElements();
        super.close();
    }

    public static XmlWriter wrap(Writer w) {
        return (w instanceof XmlWriter) ? (XmlWriter) w : new XmlPrintWriter(w);
    }

    public static <W extends Writer> W write(W w, Object o) {
        XmlWriter xw = XmlPrintWriter.wrap(w);
        if (o instanceof XmlWritable) {
            ((XmlWritable) o).toXML(xw);
        } else {
            xw.element("object", "value", o);
        }
        return w;
    }

    private void printAttrs(Object... attrs) {
        for (int i = 0; i < attrs.length / 2; i++) {
            Object name = attrs[i * 2];
            Object value = attrs[i * 2 + 1];
            if (value != null) {
                print(' ');
                print(name);
                print("=\"");
                print(escapeAttributeValue(value));
                print("\"");
            }
        }
    }

    public XmlPrintWriter tag(String tag, String value, Object... attrs) {
        if (value == null)
            return element(tag, attrs);
        print('<');
        print(tag);
        printAttrs(attrs);
        print('>');
        print(encode(value));
        print("</");
        print(tag);
        println(">");
        return this;
    }

    public XmlPrintWriter tag(String tag, Object value, Object... attrs) {
        if (value == null) {
            element(tag, attrs);
        } else {
            boolean bAuto = fbAutoNL;
            fbAutoNL = false;
            openElement(tag, attrs);
            if (value instanceof XmlWritable) {
                ((XmlWritable) value).toXML(this);
            } else {
                print(encode(String.valueOf(value)));
            }
            fbAutoNL = bAuto;
            closeElement();
        }
        return this;
    }

    public XmlPrintWriter element(String tag, Object... attrs) {
        print('<');
        print(tag);
        printAttrs(attrs);
        println("/>");
        return this;
    }

    public XmlPrintWriter openElement(String tag, Object... attrs) {
        openTag(tag, attrs);
        newline();
        return this;
    }

    public XmlPrintWriter openTag(String tag, Object... attrs) {
        fTagStack.add(0, tag);
        print('<');
        print(tag);
        printAttrs(attrs);
        print(">");
        getIndentWriter().push();
        return this;
    }

    public XmlPrintWriter closeElement() {
        getIndentWriter().pop();
        print("</");
        print(fTagStack.remove(0));
        print(">");
        newline();
        return this;
    }

    public XmlPrintWriter closeElement(String tag) {
        boolean bFound = false;
        for (; (fTagStack.size() > 0) && !bFound; ) {
            bFound = fTagStack.get(0).equals(tag);
            closeElement();
        }
        return this;
    }

    public XmlPrintWriter closeElements() {
        while (fTagStack.size() > 0)
            closeElement();
        return this;
    }

    private void newline() {
        if (!fbAutoNL) return;
        println();
    }

    public static String escapeAttributeValue(Object v) {
        String s = v == null ? "" : StringEscapeUtils.escapeXml(v.toString());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\t' || c == '\n' || c == '\r')
                continue;

            if (Character.isISOControl(s.charAt(i))) {
                StringBuilder sb = new StringBuilder();
                if (i > 0) {
                    sb.append(s.substring(0, i));
                }
                for (; i < s.length(); i++) {
                    if (!Character.isISOControl(s.charAt(i))) {
                        sb.append(s.charAt(i));
                    } else {
                        sb.append("CTRL-" + (char) ('A' + (int) s.charAt(i) - 1));
                    }
                }
                return sb.toString();
            }
        }
        return s;
    }

    public static String encode(String s) {
        return StringEscapeUtils.escapeXml(s);
    }

    public XmlPrintWriter cdata(String text) {
        boolean bAuto = getAutoNL();
        setAutoNL(false);
        print("<![CDATA[");
        print(text);
        if (bAuto) {
            println("]]>");
        } else {
            print("]]>");
        }
        setAutoNL(bAuto);
        return this;
    }

    public XmlPrintWriter openCDATA() {
        println("<![CDATA[");
        return this;
    }

    public XmlPrintWriter closeCDATA() {
        println("]]>");
        return this;
    }

    public XmlPrintWriter text(String text) {
        println(text);
        return this;
    }

    public XmlPrintWriter writeXML(XmlWritable o) {
        if (o != null)
            o.toXML(this);
        return this;
    }
}
