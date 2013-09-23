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

import com.groupon.nakala.exceptions.ResourceInitializationException;
import com.groupon.util.io.IoUtil;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * @author npendar@groupon.com
 */
public class SerializationStore implements DataStore {

    ObjectOutputStream objectOutputStream;

    @Override
    public void initialize(CollectionParameters params) throws ResourceInitializationException {
        OutputStream outputStream = null;
        if (params.contains(CollectionParameters.OUTPUT_STREAM)) {
            outputStream = (OutputStream) params.get(CollectionParameters.OUTPUT_STREAM);
        } else if (params.contains(CollectionParameters.FILE_NAME)) {
            try {
                outputStream = IoUtil.output(params.getString(CollectionParameters.FILE_NAME));
            } catch (IOException e) {
                throw new ResourceInitializationException("Failed to open file " + params.getString(CollectionParameters.FILE_NAME), e);
            }
        } else {
            throw new ResourceInitializationException("No writer or file name specified.");
        }

        try {
            objectOutputStream = new ObjectOutputStream(outputStream);
        } catch (IOException e) {
            throw new ResourceInitializationException("Failed to create object output stream.", e);
        }
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    @Override
    public void close() {
        try {
            objectOutputStream.close();
        } catch (IOException e) {

        }
    }
}
