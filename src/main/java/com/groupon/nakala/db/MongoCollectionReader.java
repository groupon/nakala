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

import com.groupon.nakala.core.Constants;
import com.groupon.nakala.exceptions.ResourceInitializationException;
import com.groupon.nakala.exceptions.TextminingException;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author npendar@groupon.com
 */
public abstract class MongoCollectionReader implements CollectionReader {

    protected static final Logger logger = Logger.getLogger(MongoCollectionReader.class);

    protected Mongo mongo;
    protected DBCollection coll;
    protected DBCursor cursor;
    protected int numberOfRecords;
    protected int count;

    public void initialize(CollectionParameters cp) throws ResourceInitializationException {

        Collection<String> unset = cp.ensureSet(
                CollectionParameters.HOST,
                CollectionParameters.PORT,
                CollectionParameters.DB_NAME,
                CollectionParameters.COLLECTION_NAME
        );
        if (!unset.isEmpty()) {
            throw new ResourceInitializationException("Collection parameters missing: " + StringUtils.join(unset, ", "));
        }

        try {
            mongo = new Mongo(cp.getString(CollectionParameters.HOST),
                    cp.getInt(CollectionParameters.PORT));
        } catch (Exception e) {
            throw new ResourceInitializationException("Failed to connect to mongo. " + e.getMessage(), e);
        }

        DB db = mongo.getDB(cp.getString(CollectionParameters.DB_NAME));
        if (db == null) {
            throw new ResourceInitializationException("Can't find database " + cp.getString(CollectionParameters.DB_NAME));
        }

        coll = db.getCollection(cp.getString(CollectionParameters.COLLECTION_NAME));
        if (coll == null) {
            throw new ResourceInitializationException("Can't find collection " + cp.getString(CollectionParameters.COLLECTION_NAME));
        }

        if (cp.contains(Constants.QUERY)) {
            try {
                DBObject query = (DBObject) JSON.parse(cp.getString(Constants.QUERY));
                cursor = coll.find(query);
            } catch (Exception e) {
                throw new ResourceInitializationException("Failed to run query " + cp.getString(Constants.QUERY), e);
            }
        } else {
            cursor = coll.find();
        }

        numberOfRecords = cursor.count();
        count = 0;
    }

    @Override
    public int getSize() {
        return numberOfRecords;
    }

    @Override
    public void reset() throws TextminingException {
        cursor = coll.find();
    }

    @Override
    public void close() {
        try {
            cursor.close();
            mongo.close();
        } catch (Exception e) {
            logger.warn("Failed to closeWriter collection reader. " + e.getMessage());
        }
    }
}
