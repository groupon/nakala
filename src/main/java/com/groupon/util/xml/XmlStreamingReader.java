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

package com.groupon.util.xml;

import com.groupon.util.io.CountingReader;
import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author alasdair@groupon.com
 */
public class XmlStreamingReader {

    private String fListElement;
    private CountingReader fReader;
    private long fnMaxBytes;
    private StringBuilder fBuffer = new StringBuilder();
    private boolean fbBufferFull;
    private boolean fbDone;
    private Exception fException;
    private Thread fSplitThread;

    private class Splitter implements Runnable {
        private class Opened {
            private String fURI;
            private String fLocalName;
            private String fQName;
            private List<String> fAttributes = new ArrayList<String>();

            public Opened(String uri, String localName, String qName, Attributes attributes) {
                fURI = uri;
                fLocalName = localName;
                fQName = qName;
                if (attributes != null) {
                    for (int i = 0; i < attributes.getLength(); i++) {
                        fAttributes.add(attributes.getQName(i) + "=\"" + StringEscapeUtils.escapeXml(attributes.getValue(i)) + "\"");
                    }
                }
            }
        }

        private List<Opened> fOpen = new ArrayList<Opened>();
        private int fnInElement;

        public void run() {
            try {
                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                parser.parse(new InputSource(fReader), new DefaultHandler() {

                    private void output(String el, Attributes attributes, List<String> pfxs) {
                        fBuffer.append("<").append(el);
                        if (attributes != null) {
                            int n = attributes.getLength();
                            for (int i = 0; i < n; i++) {
                                fBuffer.append(" ").append(attributes.getQName(i))
                                        .append("=\"").append(attributes.getValue(i)).append("\"");
                            }
                        }
                        if (pfxs != null) {
                            for (String pfx : pfxs) {
                                fBuffer.append(" ").append(pfx);
                            }
                        }
                        fBuffer.append(">");
                    }

                    @Override
                    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                        super.startElement(uri, localName, qName, attributes);
                        if (qName.equals(fListElement)) {
                            fnInElement++;
                        }
                        if (fnInElement == 0) {
                            fOpen.add(new Opened(uri, localName, qName, attributes));
                        }
                        output(qName, attributes, null);
                    }

                    @Override
                    public void characters(char[] ch, int start, int length) throws SAXException {
                        super.characters(ch, start, length);
                        fBuffer.append(StringEscapeUtils.escapeXml(new String(ch, start, length)));
                    }

                    @Override
                    public void endElement(String uri, String localName, String qName) throws SAXException {
                        super.endElement(uri, localName, qName);
                        fBuffer.append("</").append(qName).append(">");
                        if (qName.equals(fListElement)) {
                            if (--fnInElement == 0) {
                                if (fReader.getBytes() >= fnMaxBytes) {
                                    for (int i = fOpen.size() - 1; i >= 0; i--) {
                                        fBuffer.append("</").append(fOpen.get(i).fQName).append(">");
                                    }
                                    synchronized (XmlStreamingReader.this) {
                                        fbBufferFull = true;
                                        XmlStreamingReader.this.notifyAll();
                                        while (fbBufferFull) {
                                            try {
                                                XmlStreamingReader.this.wait();
                                            } catch (InterruptedException e) {
                                            }
                                        }
                                        fReader.setBytes(0);
                                        for (Opened op : fOpen) {
                                            output(op.fQName, null, op.fAttributes);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void endDocument() throws SAXException {
                        super.endDocument();
                        synchronized (XmlStreamingReader.this) {
                            fbBufferFull = true;
                            XmlStreamingReader.this.notifyAll();
                        }
                    }
                });
            } catch (Exception e) {
                synchronized (XmlStreamingReader.this) {
                    fException = e;
                    XmlStreamingReader.this.notifyAll();
                }
            }
            synchronized (XmlStreamingReader.this) {
                fbDone = true;
                XmlStreamingReader.this.notifyAll();
            }
        }
    }

    public XmlStreamingReader(Reader r, String listElement, long maxBytes) {
        fReader = new CountingReader(r);
        fListElement = listElement;
        fnMaxBytes = maxBytes;
    }

    public Reader read() throws Exception {
        if (fSplitThread == null) {
            fSplitThread = new Thread(new Splitter(), "XML Splitter");
            fSplitThread.setDaemon(true);
            fSplitThread.start();
        }
        synchronized (this) {
            while (!fbBufferFull && fException == null && !fbDone) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            if (fException != null) {
                throw fException;
            }
            if (fbBufferFull) {
                try {
                    return new StringReader(fBuffer.toString());
                } finally {
                    fBuffer = new StringBuilder();
                    fbBufferFull = false;
                    notifyAll();
                }
            }
            return null;
        }
    }
}
