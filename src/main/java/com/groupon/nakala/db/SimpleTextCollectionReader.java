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
import com.groupon.nakala.exceptions.TextminingException;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author npendar@groupon.com
 */
public class SimpleTextCollectionReader extends AbstractTextFileCollectionReader {

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
                String text = null;
                try {
                    String line = in.readLine();
                    ++count;
                    if (separator == null) {
                        text = line.trim();
                    } else {
                        String[] fields = line.split(separator);
                        try {
                            text = fields[text_field];
                        } catch (IndexOutOfBoundsException e) {
                            throw new TextminingException("Insufficient number of fields in line " + count);
                        }
                    }
                } catch (IOException e) {
                    throw new TextminingException(e);
                }
                return new IdentifiableTextContent(new Id(count), text);
            }

            @Override
            public void remove() {
            }
        };
    }
}
