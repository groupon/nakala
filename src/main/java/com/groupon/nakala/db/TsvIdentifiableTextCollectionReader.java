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

package com.groupon.nakala.db;

import com.groupon.nakala.core.Analyzable;
import com.groupon.nakala.core.Id;
import com.groupon.nakala.core.IdentifiableTextContent;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import com.groupon.nakala.exceptions.TextminingException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author npendar@groupon.com
 */
public class TsvIdentifiableTextCollectionReader extends AbstractTextFileCollectionReader {
    protected int id_field;

    /**
     * @param ps: Requires label field index (int), text field index (int), and
     *            either a file name (String) or a Reader object.
     *            An id field index is optional.
     */
    @Override
    public void initialize(CollectionParameters ps) throws ResourceInitializationException {
        Collection<String> unset = ps.ensureSet(CollectionParameters.TEXT_FIELD, CollectionParameters.ID_FIELD);

        if (!unset.isEmpty()) {
            throw new ResourceInitializationException("Parameters not set: " + StringUtils.join(unset, ", "));
        }

        super.initialize(ps);
        id_field = ps.getInt(CollectionParameters.ID_FIELD);
    }

    @Override
    public Iterator<Analyzable> iterator() {
        return new Iterator<Analyzable>() {
            int count = 0;

            @Override
            public boolean hasNext() {
                return count < size;
            }

            @Override
            public Analyzable next() {
                String line = null;
                try {
                    line = in.readLine();
                } catch (IOException e) {
                    throw new TextminingException(e);
                }
                String[] pieces = line.trim().split(separator);
                String text = null;
                Id id = null;
                try {
                    text = pieces[text_field];
                    if (id_field >= 0) {
                        id = new Id(pieces[id_field]);
                    } else {
                        id = new Id(Integer.toString(count));
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    logger.warn("Not enough fields. Only " + pieces.length + " fields found in line\n" + line);
                }

                ++count;
                IdentifiableTextContent itc = new IdentifiableTextContent(id, text);

                return itc;
            }

            @Override
            public void remove() {
            }
        };
    }
}
