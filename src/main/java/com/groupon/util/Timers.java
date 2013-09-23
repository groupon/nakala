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
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author alasdair@groupon.com
 */
public class Timers {

    private Map<String, Timer> fTimers = new TreeMap<String, Timer>();

    public class Timer {
        private String fName;
        private int fnCalls;
        private long fnTotal;
        private long fnMinTime = Long.MAX_VALUE;
        private long fnMaxTime;
        private long fnTotal2;
        private long fStart;
        private long fnLast;

        public Timer(String name) {
            fName = name;
        }

        public Timer start() {
            fStart = System.currentTimeMillis();
            return this;
        }

        public Timer stop() {
            long now = System.currentTimeMillis();
            long call = now - fStart;
            fnLast = call;
            if (call < fnMinTime) fnMinTime = call;
            if (call > fnMaxTime) fnMaxTime = call;
            fnTotal += call;
            fnTotal2 += (call * call);
            fnCalls++;
            return this;
        }

        public String getName() {
            return fName;
        }

        public int getNumCalls() {
            return fnCalls;
        }

        public long getTotalTime() {
            return fnTotal;
        }

        public long getLastTime() {
            return fnLast;
        }

        public long getMinTime() {
            return fnMinTime;
        }

        public long getMaxTime() {
            return fnMaxTime;
        }

        public double getAverageTime() {
            return fnTotal / (double) fnCalls;
        }

        public double getStdevTime() {
            if (fnCalls <= 1)
                return 0.0;
            return Math.sqrt((fnTotal2 - fnTotal * fnTotal / (double) fnCalls) / (fnCalls - 1));
        }
    }

    public Timer getTimer(String name) {
        Timer t = fTimers.get(name);
        if (t == null) {
            fTimers.put(name, t = new Timer(name));
        }
        return t;
    }

    public <W extends Writer> W writeReport(W w) {
        PrintWriter pw = new PrintWriter(w);
        int n = 20;
        for (String name : fTimers.keySet()) {
            int l = name.length();
            if (l > n)
                l = n;
        }
        pw.println(String.format("%-" + n + "s  %7s %7s %7s %7s %7s %7s", "Timer", "Calls", "Last", "Avg", "Min", "Max", "Stdev"));
        for (Map.Entry<String, Timer> e : fTimers.entrySet()) {
            Timer t = e.getValue();
            pw.println(String.format("%-" + n + "s: %7d %7d %7.2f %7d %7d %7.2f",
                    e.getKey(), t.getNumCalls(), t.getLastTime(), t.getAverageTime(), t.getMinTime(), t.getMaxTime(), t.getStdevTime()));
        }
        pw.flush();
        return w;
    }

    @Override
    public String toString() {
        return writeReport(new StringWriter()).toString();
    }
}
