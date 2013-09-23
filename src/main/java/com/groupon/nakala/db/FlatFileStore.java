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
import com.groupon.nakala.exceptions.StoreException;
import com.groupon.util.io.IoUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author npendar@groupon.com
 */
public class FlatFileStore implements DataStore {

    String fileName;
    PrintStream printStream;
    OutputStream outputStream;


    @Override
    public void initialize(CollectionParameters params) throws ResourceInitializationException {
        if (params.contains(CollectionParameters.FILE_NAME)) {
            fileName = params.getString(CollectionParameters.FILE_NAME);
        } else {
            throw new ResourceInitializationException("Output file name not set.");
        }
    }

    public OutputStream getOutputStream() throws StoreException {
        if (outputStream == null) {
            try {
                outputStream = IoUtil.output(fileName);
            } catch (IOException e) {
                throw new StoreException(e);
            }
        }
        return outputStream;
    }

    public PrintStream getPrintStream() throws StoreException {
        if (printStream == null)
            printStream = new PrintStream(getOutputStream());
        return printStream;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public void close() {
        try {
            printStream.close();
        } catch (Exception e) {

        }
    }
}
