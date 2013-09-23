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

import com.groupon.util.collections.CollectionUtil;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * @author alasdair@groupon.com
 */
public class ColumnSerializer {
    public static Object[] serialize(ColumnSerializable cs, Object[] cols) {
        int iCol = 0;
        String[] props = cs.getColumnProperties();
        cols = (cols == null) ? new Object[props.length] : cols;
        for (String p : props) {
            try {
                Object value = PropertyUtils.getSimpleProperty(cs, p);
                cols[iCol++] = value;
            } catch (Exception e) {
                throw new RuntimeException("Error accessing bean property " + p + " on " + cs, e);
            }
        }
        return cols;
    }

    public static Object[] serialize(ColumnSerializable cs) {
        return serialize(cs, null);
    }

    public static String toColumns(ColumnSerializable cs, String sep) {
        return CollectionUtil.join(sep, serialize(cs));
    }

    public static String toColumns(ColumnSerializable cs) {
        return toColumns(cs, "\t");
    }
}
