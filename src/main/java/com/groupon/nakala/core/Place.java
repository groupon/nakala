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

package com.groupon.nakala.core;

import com.groupon.nakala.analysis.Analysis;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author npendar@groupon.com
 */
public final class Place implements TitledContent {

    private Id id;
    private Analysis analysis;
    private TitledContentArray reviews;
    private TitledContentArray descriptions;
    private Collection<String> categories;
    private String name;
    private String address;
    private String locality;
    private String region;
    private String postalCode;
    private String phone;
    private String url;

    public String getUrl() {
        return url;
    }

    public Place setUrl(String url) {
        this.url = url;
        return this;
    }

    public Place setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Place setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getLocality() {
        return locality;
    }

    public Place setLocality(String locality) {
        this.locality = locality;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public Place setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public Place setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public Place setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public Place setCategories(Collection<String> cs) {
        categories = new HashSet<String>(cs);
        return this;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder();
        if (descriptions != null) {
            sb.append(descriptions.getTitle()).append('\n');
            sb.append(descriptions.getText()).append('\n');
        }

        if (reviews != null) {
            sb.append(reviews.getTitle()).append('\n');
            sb.append(reviews.getText());
        }
        return sb.toString().trim();
    }

    public Collection<String> getCategories() {
        return categories;
    }

    public Place setReviews(TitledContentArray rs) {
        reviews = rs;
        return this;
    }

    public Place setDescriptions(TitledContentArray ds) {
        descriptions = ds;
        return this;
    }

    public TitledContentArray getReviews() {
        return reviews;
    }

    public TitledContentArray getDescriptions() {
        return descriptions;
    }

    public Id getId() {
        return id;
    }

    public Place setId(Id id) {
        this.id = id;
        return this;
    }

    public Analysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(Analysis a) {
        analysis = a;
    }

    @Override
    public String toString() {
        return "Place{" +
                "id=" + id +
                ", analysis=" + analysis +
                ", reviews=" + reviews +
                ", descriptions=" + descriptions +
                ", categories=" + categories +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", locality='" + locality + '\'' +
                ", region='" + region + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
