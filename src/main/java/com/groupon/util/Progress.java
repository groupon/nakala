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

package com.groupon.util;

import com.groupon.util.io.LogWriter;

import java.util.LinkedList;
import java.util.List;

/**
 * @author alasdair@groupon.com
 */
public class Progress {
    private String fName;
    private double fTarget;
    private double fDone;
    private long fnElapsed;
    private long fnStart;
    private boolean fbStopped;
    private boolean fbAutoStart;
    private boolean fbAutoReport = true;

    private List<ProgressListener> fListeners = new LinkedList<ProgressListener>();

    public Progress(String name, double target) {
        fName = name;
        fTarget = target;
        fDone = 0.0;
    }

    public void setName(String name) {
        fName = name;
    }

    public boolean getAutoReport() {
        return fbAutoReport;
    }

    public void setAutoReport(boolean b) {
        fbAutoReport = b;
    }

    public boolean getAutoStart() {
        return fbAutoStart;
    }

    public void setAutoStart() {
        fbAutoStart = true;
    }

    public boolean getStopped() {
        return fbStopped;
    }

    public void start() {
        fnStart = System.currentTimeMillis();
    }

    public void stop() {
        fnElapsed += System.currentTimeMillis() - fnStart;
        fnStart = 0;
        fbStopped = true;
        for (ProgressListener pl : fListeners) {
            pl.progressUpdated(this);
        }
    }

    public double work(double n) {
        if (fbAutoStart) {
            fnStart = System.currentTimeMillis();
            fbAutoStart = false;
        }
        fDone += n;
        if (fbAutoReport) {
            report();
        }
        return fDone;
    }

    public void advanceTo(double n) {
        fDone = Math.max(fDone, n);
    }

    public void report() {
        for (ProgressListener pl : fListeners) {
            pl.progressUpdated(this);
        }
    }

    public double getTarget() {
        return fTarget;
    }

    public void setTarget(double target) {
        fTarget = target;
    }

    public double getPercentDone() {
        return fDone * 100.0 / fTarget;
    }

    protected void setElapsed(long e) {
        fnElapsed = e;
    }

    public long getElapsed() {
        return fnElapsed + (fnStart == 0 ? 0 : (System.currentTimeMillis() - fnStart));
    }

    public double getRate() {
        return fDone * 1000.0 / getElapsed();
    }

    public long getEstimate() {
        return getEstimate(getRate());
    }

    public long getEstimate(double rate) {
        return (long) (1000 * (fTarget - fDone) / rate);
    }

    public String getName() {
        return fName;
    }

    public double getDone() {
        return fDone;
    }

    public List<ProgressListener> getProgressListeners() {
        return fListeners;
    }

    public <T extends ProgressListener> T addProgressListener(T pl) {
        fListeners.add(pl);
        return pl;
    }

    public void removeProgressListener(ProgressListener pl) {
        fListeners.remove(pl);
    }

    public static Progress text(String name, int size, LogWriter w) {
        Progress pg = new Progress(name, size);
        TextProgressListener tpl = pg.addProgressListener(new TextProgressListener(w));
        tpl.setWholeNumbers(true);
        tpl.setUpdateFrequency(1000);
        return pg;
    }
}
