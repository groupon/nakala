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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author alasdair@groupon.com
 */
public class UrlParser {

    private static Pattern gPattern = Pattern.compile("\\s*((https?)://)?((www\\.)?([^/?]*))(/([^?]*))?(\\?(.*))?", Pattern.CASE_INSENSITIVE);

    private String fURL;
    private boolean fbMatched;
    private String fProtocol;
    private String fSite;
    private String fDomain;
    private String fPath;
    private String fQuery;

    public UrlParser(String url) {
        fURL = url;
        Matcher m = gPattern.matcher(url);
        if (m.matches()) {
            fbMatched = true;
            try {
                fProtocol = m.group(2);
                fSite = m.group(3);
                fDomain = m.group(5);
                fPath = m.group(7);
                fQuery = m.group(9);

                if ((fPath == null) || (fPath.length() == 0)) {
                    int off = fDomain.indexOf(';');
                    if (off >= 0) {
                        fPath = fDomain.substring(off).trim();
                        fDomain = fDomain.substring(0, off).trim();
                        fSite = fSite.substring(0, fSite.indexOf(';'));
                    }
                }
            } catch (IndexOutOfBoundsException e) {
            }
            if (fPath != null && fPath.length() == 0)
                fPath = null;
        }
    }

    public String getURL() {
        return fURL;
    }

    public boolean getMatched() {
        return fbMatched;
    }

    public String getProtocol() {
        return fProtocol;
    }

    public String getSite() {
        return fSite;
    }

    public String getDomain() {
        return fDomain;
    }

    public String getPath() {
        return fPath;
    }

    public String getQuery() {
        return fQuery;
    }

    public void setProtocol(String protocol) {
        fProtocol = protocol;
    }

    public void setSite(String site) {
        fSite = site;
    }

    public void setDomain(String domain) {
        fDomain = domain;
    }

    public void setPath(String path) {
        fPath = path;
    }

    public void setQuery(String query) {
        fQuery = query;
    }

    @Override
    public String toString() {
        return fURL;
    }

    public String reconstitute() {
        StringBuilder sb = new StringBuilder();
        sb.append(getProtocol()).append("://");
        sb.append(getSite());
        if ((fPath != null) && (fPath.length() > 0))
            sb.append('/').append(fPath);
        if ((fQuery != null) && (fQuery.length() > 0))
            sb.append('?').append(fQuery);
        return sb.toString();
    }
}
