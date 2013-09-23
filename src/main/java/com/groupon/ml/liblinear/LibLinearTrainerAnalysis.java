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

package com.groupon.ml.liblinear;

import com.groupon.ml.svm.ValueScaler;
import com.groupon.nakala.analysis.Analysis;
import com.groupon.nakala.db.DataStore;
import com.groupon.nakala.db.FlatFileStore;
import com.groupon.nakala.exceptions.StoreException;
import de.bwaldvogel.liblinear.Model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * @author npendar@groupon.com
 */
public class LibLinearTrainerAnalysis implements Analysis {

    private Model model;
    private ValueScaler scaler;
    private List<String> labels;

    public LibLinearTrainerAnalysis(Model model, ValueScaler scaler, List<String> labels) {
        this.model = model;
        this.scaler = scaler;
        this.labels = labels;
    }

    public Model getModel() {
        return model;
    }

    public ValueScaler getScaler() {
        return scaler;
    }

    public List<String> getLabels() {
        return labels;
    }

    @Override
    public void store(DataStore ds) throws StoreException {
        if (!(ds instanceof FlatFileStore)) {
            throw new StoreException("Only FlatFileStore is supported.");
        }

        FlatFileStore ffs = (FlatFileStore) ds;

        String fileStem = ffs.getFileName();
        try {
            model.save(new File(fileStem + ".model"));
        } catch (IOException e) {
            throw new StoreException("Failed to save LibLinear model.", e);
        }

        try {
            FileOutputStream fos = new FileOutputStream(fileStem + ".range");
            scaler.save(fos);
            fos.close();
        } catch (Exception e) {
            throw new StoreException("Failed to save range file.", e);
        }

        try {
            PrintStream printStream = new PrintStream(new FileOutputStream(fileStem + ".labels"));
            for (String label : labels) {
                printStream.println(label);
            }
            printStream.close();
        } catch (Exception e) {
            throw new StoreException("Failed to save labels file.", e);
        }
    }
}
