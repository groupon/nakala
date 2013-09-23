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

package com.groupon.nakala.core;

import com.groupon.nakala.analysis.Initializable;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import com.groupon.util.io.IoUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author npendar@groupon.com
 */
public class ResourceReader implements Initializable {
    private BufferedReader reader;

    @Override
    public void initialize(Parameters params) throws ResourceInitializationException {
        if (params.contains(Constants.FILE_NAME)) {
            try {
                reader = new BufferedReader(IoUtil.read(params.getString(Constants.FILE_NAME)));
            } catch (IOException e) {
                throw new ResourceInitializationException("Failed to initialize resource loader.", e);
            }
        } else if (params.contains(Constants.RESOURCE)) {
            try {
                reader = new BufferedReader(IoUtil.read(getClass(), params.getString(Constants.RESOURCE)));
            } catch (IOException e) {
                throw new ResourceInitializationException("Failed to initialize resource loader.", e);
            }
        } else if (params.contains(Constants.RESOURCE_STREAM)) {
            Object streamObj = params.get(Constants.RESOURCE_STREAM);
            if (!(streamObj instanceof InputStream)) {
                throw new ResourceInitializationException("Expected RESOURCE_STREAM to be instance of InputStream");
            }
            reader = new BufferedReader(new InputStreamReader((InputStream) streamObj));
        } else {
            throw new ResourceInitializationException("No file or resource name specified.");
        }
    }

    @Override
    public void shutdown() {
        try {
            reader.close();
        } catch (Exception e) {

        }
    }

    public BufferedReader getReader() {
        return reader;
    }
}
