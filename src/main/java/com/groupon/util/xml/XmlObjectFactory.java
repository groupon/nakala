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
import com.groupon.util.NamedObjectFactory;
import org.dom4j.Element;

import java.lang.reflect.ParameterizedType;


/**
 * @author alasdair@groupon.com
 */
public class XmlObjectFactory<T> {

    private NamedObjectFactory<T> fKnownTypes;
    private Class<? extends T> fClass;
    private boolean fbConcreteClass;

    public XmlObjectFactory() {
        Class cl = getClass();
        if (cl != XmlObjectFactory.class) {
            for (; cl.getSuperclass() != XmlObjectFactory.class; cl = cl.getSuperclass()) ;
            ParameterizedType type = (ParameterizedType) cl.getGenericSuperclass();
            fClass = (Class) type.getActualTypeArguments()[0];
        }
    }

    public XmlObjectFactory(Class<? extends T> cl) {
        fClass = cl;
    }

    public XmlObjectFactory(NamedObjectFactory<T> types) {
        fKnownTypes = types;
    }

    public XmlObjectFactory<T> setConcreteClass(Class<? extends T> cl) {
        fClass = cl;
        fbConcreteClass = true;
        return this;
    }

    public T instance(Element node) throws ConfigureException {
        String type = node.attributeValue("type");
        T r = null;
        if (type != null) {
            r = fKnownTypes.getNamedObject(type);
            if (r == null) {
                throw new ConfigureException("Unknown type: " + type);
            }
        } else {
            String cls = node.attributeValue("class");
            if (cls == null) {
                if (!fbConcreteClass)
                    throw new ConfigureException("No type or class specified: " + node.asXML());
                try {
                    r = (T) fClass.newInstance();
                } catch (Throwable t) {
                    throw new ConfigureException(t);
                }
            } else {
                try {
                    Class cl = Class.forName(cls);
                    if (!cl.isAssignableFrom(cl)) {
                        throw new ConfigureException("Class must be derived from " + fClass.getName() + ": " + node.asXML());
                    }
                    fClass = cl;
                    r = (T) cl.newInstance();
                } catch (Throwable t) {
                    throw new ConfigureException(node.asXML(), t);
                }
            }
        }
        if (r instanceof XmlConfigurable) {
            ((XmlConfigurable) r).configure(node);
        }
        return r;
    }
}
