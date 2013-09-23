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

import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

/**
 * @author alasdair@groupon.com
 */
public class TextProgressListener implements ProgressListener {

    private PrintWriter fOut;
    private long fnLastElapsed = 0;
    private long fnFrequency = 1000;
    private double fnMinWork;
    private boolean fbWholeNumbers;

    private List<Sample> fSamples = new LinkedList<Sample>();

    private class Sample {
        private double fDone;
        private long fnTime;

        public Sample(double done) {
            fDone = done;
            fnTime = System.currentTimeMillis();
        }

        public double getDone() {
            return fDone;
        }

        public long getTime() {
            return fnTime;
        }
    }


    public TextProgressListener(Writer w) {
        fOut = (w instanceof PrintWriter) ? (PrintWriter) w : new PrintWriter(w);
    }

    public TextProgressListener setWholeNumbers(boolean b) {
        fbWholeNumbers = b;
        return this;
    }

    public boolean getWholeNumbers() {
        return fbWholeNumbers;
    }

    public TextProgressListener setUpdateFrequency(long millis) {
        fnFrequency = millis;
        return this;
    }

    public double getMinWork() {
        return fnMinWork;
    }

    public TextProgressListener setMinWork(double minWork) {
        fnMinWork = minWork;
        return this;
    }

    public long getUpdateFrequency() {
        return fnFrequency;
    }

    private double getRecentRate() {
        if (fSamples.size() > 5) {
            Sample s = fSamples.remove(0);
            Sample e = fSamples.get(fSamples.size() - 1);
            return 1000.0 * (e.getDone() - s.getDone()) / (double) (e.getTime() - s.getTime());
        }
        return 0;
    }

    @Override
    public void progressUpdated(Progress p) {
        if (p.getDone() >= fnMinWork && (p.getElapsed() - fnLastElapsed > fnFrequency || p.getStopped())) {
            double rr = getRecentRate();
            if (p.getTarget() > 0.0) {
                fOut.print(p.getName() + " completed");
                if (fbWholeNumbers) {
                    fOut.print(String.format(" %d/%d", (int) p.getDone(), (int) p.getTarget()));
                } else {
                    fOut.print(String.format(" %g/%g", p.getDone(), p.getTarget()));
                }

                fOut.print(String.format(" (%.2f %%) in %s - overall %.2f/s - EST %s",
                        p.getPercentDone(),
                        TimeUtil.getHMS(p.getElapsed()),
                        p.getRate(),
                        TimeUtil.getHMS(p.getEstimate())));
                if (rr > 0) {
                    fOut.print(String.format(" - recent %.2f/s - EST %s",
                            rr, TimeUtil.getHMS(p.getEstimate(rr))));
                }
                fOut.println();
            } else {
                fOut.print(p.getName() + " completed");
                if (fbWholeNumbers) {
                    fOut.print(String.format(" %d", (int) p.getDone()));
                } else {
                    fOut.print(String.format(" %g", p.getDone()));
                }

                fOut.print(String.format(" in %s - overall %.2f/s",
                        TimeUtil.getHMS(p.getElapsed()),
                        p.getRate()));
                if (rr > 0) {
                    fOut.print(String.format(" - recent %.2f/s", rr));
                }
                fOut.println();
            }
            fOut.flush();
            fnLastElapsed = p.getElapsed();
            fSamples.add(new Sample(p.getDone()));
        }
    }
}
