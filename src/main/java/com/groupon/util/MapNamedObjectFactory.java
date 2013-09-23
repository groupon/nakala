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

package com.groupon.util;

import com.groupon.util.xml.XmlObjectFactory;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author alasdair@groupon.com
 */
public abstract class MapNamedObjectFactory<T> implements NamedObjectFactory<T> {
    private Map<String, Class> fClasses = new HashMap<String, Class>();
    private XmlObjectFactory<T> fXmlFactory = new XmlObjectFactory<T>(this);

    private static final Logger log = Logger.getLogger(MapNamedObjectFactory.class);

    protected void put(String name, Class cl) {
        fClasses.put(name, cl);
    }

    public XmlObjectFactory<T> getXmlFactory() {
        return fXmlFactory;
    }

    public Class<T> setNamedClass(String name, Class<? extends T> cl) {
        return (Class<T>) fClasses.put(name, cl);
    }

    @Override
    public T getNamedObject(String name) {
        Class cl = fClasses.get(name);
        if (cl == null)
            return null;
        try {
            return (T) cl.newInstance();
        } catch (Throwable t) {
            log.error("getNamedObject", t);
            return null;
        }
    }
}
