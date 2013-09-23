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

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author alasdair@groupon.com
 */
public class DateConvert {
    final static Pattern ambiguousDatePattern = Pattern.compile("(\\d{1,2})/(\\d{1,2})/(\\d{1,4}).*");
    final static int currentYear = Calendar.getInstance().get(Calendar.YEAR) - 2000;

    List<ArrayList> formats;

    public static void main(String[] args) {
        DateConvert dc = new DateConvert();

        System.out.println("running this from command line will read a file called dates.txt and output 2 files:");
        System.out.println("  1) dates_new.txt - has struct_id,field_id,original date,date as long,date as formatted string.");
        System.out.println("  2) date_errors.txt - has records that could not be converted with a message as to why not.");
        System.out.println("");
        try {
            dc.parseFile("dates.txt");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public DateConvert() {
        initialize();
    }

    public void initialize() {
        formats = new ArrayList<ArrayList>();
        ArrayList<SimpleDateFormat> formats1 = new ArrayList<SimpleDateFormat>();
        ArrayList<SimpleDateFormat> formats2 = new ArrayList<SimpleDateFormat>();
        ArrayList<SimpleDateFormat> formats3 = new ArrayList<SimpleDateFormat>();
        formats.add(formats1);
        formats.add(formats2);
        formats.add(formats3);

        String[] forms1 = {"M/d/y", "MMMM d yyyy", "MMM d y", "d MMM y", "MMMM y", "d-MMM-y", "yyyy-MM-dd"}; // leaving out d/m/y
        for (int i = 0; i < forms1.length; i++) {
            SimpleDateFormat sdf = new SimpleDateFormat(forms1[i]);
            formats1.add(sdf);
        }
        // these may be ambiguous with formats above, we only look for these if we got no matches above
        String[] forms2 = {"MMM y", "MMM-y"};
        for (int i = 0; i < forms2.length; i++) {
            SimpleDateFormat sdf = new SimpleDateFormat(forms2[i]);
            formats2.add(sdf);
        }
        // these may be ambiguous with formats above, we only look for these if we got no matches above
        String[] forms3 = {"yyyy"};
        for (int i = 0; i < forms3.length; i++) {
            SimpleDateFormat sdf = new SimpleDateFormat(forms3[i]);
            formats3.add(sdf);
        }
    }

    public DateConvertResult parseDate(String s) {
        if (s == null || "".equals(s) || " ".equals(s)) {
            return new DateConvertResult(null, DateConvertResult.EMPTY, null, s);
        }

        // normalize as possible
        s = s.replaceAll("\\,\\s*", " ");
        s = s.replaceAll("\\.\\s*", " ");
        s = s.replaceAll("\\Sept\\s+", "Sep "); // apparently Java doesn't recognize Sept as September

        // check for dates that might be invalid for form mm/dd/yyyy
        // like dates that are really dd/mm/yyyy (Euro-style)
        Matcher m = ambiguousDatePattern.matcher(s);
        if (m.matches()) {
            int month = -1;
            try {
                month = Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) {
                ;
            }

            int date = -1;
            try {
                date = Integer.parseInt(m.group(2));
            } catch (NumberFormatException e) {
                ;
            }

            int year = -1;
            try {
                year = Integer.parseInt(m.group(3));
            } catch (NumberFormatException e) {
                ;
            }

            if (month < 1 || month > 12) {
                return new DateConvertResult(null, DateConvertResult.INVALID, null, "illegal month " + month);
            }
            if (date < 1 || date > 31) {
                return new DateConvertResult(null, DateConvertResult.INVALID, null, "illegal date " + date);
            }
            if (year >= 2000) {
                year = year - 2000;
            }
            if (year < 0 || year > currentYear) {
                return new DateConvertResult(null, DateConvertResult.INVALID, null, "illegal year " + year);
            }
        }

        ArrayList<SimpleDateFormat> returnFormats = new ArrayList<SimpleDateFormat>();
        Date lastValidDate = null;
        for (final ArrayList<SimpleDateFormat> l : formats) {
            int i = 0;
            while (i < l.size() && returnFormats.size() == 0) {
                Date d = null;
                try {
                    d = l.get(i).parse(s);
                } catch (ParseException e) {
                    ;
                }
                if (d != null) {
                    lastValidDate = d;
                    //System.out.println( s + "\tmatches " + l.get(i).toPattern() ) ;
                    returnFormats.add(l.get(i));
                }
                i++;
            }
        }

        if (returnFormats.size() == 0) {
            return new DateConvertResult(null, DateConvertResult.NO_MATCH, null, s);
        } else if (returnFormats.size() > 1) {
            return new DateConvertResult(returnFormats, DateConvertResult.AMBIGUOUS, null, showFormats(returnFormats));
        } else {
            return new DateConvertResult(returnFormats, DateConvertResult.VALID, lastValidDate, null);
        }
    }

    public String showFormats(List<SimpleDateFormat> l) {
        StringBuffer sb = new StringBuffer();
        for (final SimpleDateFormat f : l) {
            sb.append(f.toPattern()).append(", ");
        }
        return sb.toString();
    }

    public String generalize(String s) {
        String t = s.replaceAll("[a-zA-Z]", "c");
        String u = t.replaceAll("\\d", "d");
        return u;
    }

    public void parseFile(String filename) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("dates_new.txt"));
            BufferedWriter err = new BufferedWriter(new FileWriter("date_errors.txt"));
            FileInputStream fstream = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            int counter = 0;
            long validCounter = 0;
            long invalidCounter = 0;
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (counter % 100000 == 0) {
                    System.out.println("read " + counter + " lines. " +
                            validCounter + " valid.  " +
                            invalidCounter + " invalid.");
                }
                // columns are: 0)struct_id, 1)field_id, 2)source_id, 3)source, 4)date_value
                String[] parts = strLine.split("\t");
                DateConvertResult dcr = parseDate(parts[4]);
                if (DateConvertResult.VALID.equals(dcr.result)) {
                    //         struct_id         field_id          date_value
                    out.write(parts[0] + "\t" + parts[1] + "\t" + parts[4] + "\t" + dcr.date.getTime() + "\t" + dcr.date.toString() + "\n");
                    validCounter++;
                } else {
                    //         struct_id         field_id          date_value
                    err.write(parts[0] + "\t" + parts[1] + "\t" + parts[4] + "\t" + dcr.result + "\t" + dcr.errorMessage + "\n");
                    invalidCounter++;
                }
                counter++;
            }
            in.close();
            out.close();
            err.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
