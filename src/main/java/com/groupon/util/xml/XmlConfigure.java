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

import com.groupon.util.ConfigureException;
import com.groupon.util.collections.Mapper;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author alasdair@groupon.com
 */
public abstract class XmlConfigure {

    private static final Logger log = Logger.getLogger(XmlConfigure.class);

    public static String getAttribute(Element el, String name, String def) {
        String v = el.attributeValue(name);
        return v == null ? def : v;
    }

    public static int getIntAttribute(Element el, String name, int def) {
        String v = el.attributeValue(name);
        if (v != null) {
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                log.warn("Unable to parse attribute " + name + "='" + v + "' as an integer");
            }
        }
        return def;
    }

    public static double getDoubleAttribute(Element el, String name, double def) {
        String v = el.attributeValue(name);
        if (v != null) {
            try {
                return Double.parseDouble(v);
            } catch (NumberFormatException e) {
                log.warn("Unable to parse attribute " + name + "='" + v + "' as a double");
            }
        }
        return def;
    }

    public static boolean getBooleanAttribute(Element el, String name, boolean b) {
        String v = el.attributeValue(name);
        if (v == null)
            return b;
        return Boolean.parseBoolean(v);
    }

    public static <E extends Enum<E>> E getEnumAttribute(Element el, String attr, Class<E> ecl, E def) {
        if (el == null)
            return def;
        String v = el.attributeValue(attr);
        if (v == null)
            return def;
        try {
            return Enum.valueOf(ecl, v.toUpperCase());
        } catch (IllegalArgumentException e) {
            try {
                return Enum.valueOf(ecl, v);
            } catch (IllegalArgumentException e1) {
                for (E ev : ecl.getEnumConstants()) {
                    if (ev.name().equalsIgnoreCase(attr))
                        return ev;
                }
            }
        }
        log.warn("Unable to parse attribute " + attr + "='" + v + "' as enum " + ecl.getName());
        return def;
    }

    public static <T, C extends Collection<T>> C parse(Reader in, C into, Mapper<Element, T> map) throws DocumentException {
        return parse(read(in).getRootElement(), into, map);
    }

    public static <T, C extends Collection<T>> C parse(Element el, C into, Mapper<Element, T> map) {
        for (Iterator<Element> i = el.elementIterator(); i.hasNext(); ) {
            into.add(map.map(i.next()));
        }
        return into;
    }

    public static Document read(Reader r) throws DocumentException {
        return new SAXReader().read(r);
    }

    public static <T extends XmlConfigurable> T configure(T obj, Element el) throws ConfigureException {
        obj.configure(el);
        return obj;
    }
}
