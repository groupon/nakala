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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author alasdair@groupon.com
 */
public class DateConvertResult {
    public static final String VALID = "valid";
    public static final String INVALID = "invalid";
    public static final String NO_MATCH = "no match";
    public static final String AMBIGUOUS = "ambiguous";
    public static final String EMPTY = "empty";

    public List<SimpleDateFormat> matchingFormats;
    public String result;
    public Date date;
    public String errorMessage;
    private Calendar calendar;

    public DateConvertResult(List<SimpleDateFormat> l, String r, Date d, String e) {
        date = d;
        result = r;
        matchingFormats = l;
        errorMessage = e;
        calendar = null;
        if (date != null) {
            calendar = Calendar.getInstance();
            calendar.setTime(date);
        }
    }

    public Date getTime() {
        return date;
    }

    public boolean isValid() {
        return result.equals(VALID);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getResult() {
        return result;
    }

    public String getResultMessage() {
        return Strings.join(":", result, errorMessage);
    }

    // these functions are for convenience for testing
    // because its hard to use longs in junit and
    // it's hard to work with the Date.toString output because the timezone and other factors
    // may jump around depend on the local of the machine running it.
    public int getYear() {
        if (calendar == null) return -1;
        return calendar.get(Calendar.YEAR);
    }

    public int getDate() {
        if (calendar == null) return -1;
        return calendar.get(Calendar.DATE);
    }

    public int getMonth() {
        if (calendar == null) return -1;
        return calendar.get(Calendar.MONTH);
    }
}
