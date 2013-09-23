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
import com.groupon.nakala.core.CategorizedTextContent;
import com.groupon.nakala.core.Id;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import com.groupon.nakala.exceptions.TextminingException;
import com.groupon.util.io.IoUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author npendar@groupon.com
 */
public final class JsonCategorizedTextReader extends AbstractTextFileCollectionReader {

    private String label_field, text_field, id_field;
    private JSONParser parser;

    /**
     * @param ps: Requires label field (String), text field (String), and
     *            either a file name (String) or a Reader object.
     *            An id field name (String) is optional.
     */
    @Override
    public void initialize(CollectionParameters ps) {
        if (ps.contains(CollectionParameters.FILE_NAME)) {
            fileName = ps.getString(CollectionParameters.FILE_NAME);
        } else {
            throw new ResourceInitializationException("File name not provided for collection reader.");
        }

        label_field = ps.getString(CollectionParameters.LABEL_FIELD);
        text_field = ps.getString(CollectionParameters.TEXT_FIELD);

        if (ps.contains(CollectionParameters.ID_FIELD)) {
            id_field = ps.getString(CollectionParameters.ID_FIELD);
        } else {
            id_field = null;
        }

        parser = new JSONParser();

        try {
            size = IoUtil.countLines(IoUtil.read(fileName));
        } catch (IOException e) {
            throw new ResourceInitializationException("Failed to load file " + fileName, e);
        }

        initializeInput();
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
                String text = null;
                String label = null;
                Id id = null;
                try {
                    line = in.readLine();
                    JSONObject jsonObject = (JSONObject) parser.parse(line);
                    text = (String) jsonObject.get(text_field);
                    label = (String) jsonObject.get(label_field);

                    if (id_field != null) {
                        id = new Id((String) jsonObject.get(id_field));
                    } else {
                        id = new Id(Integer.toString(count));
                    }
                } catch (Exception e) {
                    throw new TextminingException(e);
                }

                ++count;

                CategorizedTextContent ctc = new CategorizedTextContent(id, text);

                for (String l : label.split(",")) {
                    ctc.addCategory(l.trim());
                }

                return ctc;
            }

            @Override
            public void remove() {
            }
        };
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("USAGE: JsonCategorizedTextReader <ID_FIELD> <LABEL_FIELD> <TEXT_FIELD> <JSON_FILE>");
            System.exit(1);
        }

        CollectionParameters params = new CollectionParameters();
        params.set(CollectionParameters.ID_FIELD, args[0])
                .set(CollectionParameters.LABEL_FIELD, args[1])
                .set(CollectionParameters.TEXT_FIELD, args[2])
                .set(CollectionParameters.FILE_NAME, args[3]);

        JsonCategorizedTextReader reader = new JsonCategorizedTextReader();
        reader.initialize(params);
        for (Analyzable a : reader) {
            CategorizedTextContent ctc = (CategorizedTextContent) a;
            System.out.println(ctc.getId().toString() + '\t' + ctc.getCategories() + '\t' + ctc.getText().replace("\r", " ").replace("\n", " ").replace("\t", " "));
        }
    }
}
