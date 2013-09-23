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
import com.groupon.nakala.exceptions.TextminingException;
import com.groupon.util.io.IoUtil;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author npendar@groupon.com
 */
public abstract class AbstractTextFileCollectionReader implements CollectionReader {
    protected static final Logger logger = Logger.getLogger(AbstractTextFileCollectionReader.class);
    protected String fileName;
    protected int text_field;
    protected String separator;
    protected int size;
    protected BufferedReader in;

    @Override
    public void initialize(CollectionParameters ps) {
        if (ps.contains(CollectionParameters.FILE_NAME)) {
            fileName = ps.getString(CollectionParameters.FILE_NAME);
        } else {
            throw new ResourceInitializationException("File name not provided for collection reader.");
        }

        separator = ps.getString(CollectionParameters.SEPARATOR);

        logger.debug("Input file: " + fileName);
        logger.debug("Separator: '" + separator + "'");

        if (separator != null && !ps.contains(CollectionParameters.TEXT_FIELD)) {
            throw new ResourceInitializationException("Separator set but no text field index specified.");
        }

        if (ps.contains(CollectionParameters.TEXT_FIELD)) {
            text_field = ps.getInt(CollectionParameters.TEXT_FIELD);
            logger.debug("Text Field: " + text_field);
        }

        try {
            size = IoUtil.countLines(IoUtil.read(fileName));
        } catch (IOException e) {
            throw new ResourceInitializationException("Failed to load file " + fileName, e);
        }

        logger.debug("Number of lines: " + size);

        initializeInput();
    }

    protected void initializeInput() throws ResourceInitializationException {
        try {
            in = new BufferedReader(IoUtil.read(fileName));
        } catch (IOException e) {
            throw new ResourceInitializationException("Failed to read from file " + fileName, e);
        }
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void reset() throws TextminingException {
        close();
        initializeInput();
    }

    @Override
    public void close() {
        try {
            in.close();
        } catch (Exception e) {

        }
    }
}
