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

import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

/**
 * @author alasdair@groupon.com
 */
public class SaxExample {

    private static final Logger log = Logger.getLogger(SaxExample.class);

    private interface Term {
        public boolean evaluate();
    }

    private class Expression implements Term {
        private boolean fbValue;

        protected Expression() {
        }

        public Expression(boolean b) {
            fbValue = b;
        }

        protected void setValue(boolean b) {
            fbValue = b;
        }

        public boolean evaluate() {
            return fbValue;
        }

        public String toString() {
            return String.valueOf(fbValue);
        }
    }

    private abstract class CompoundTerm implements Term {
        private List<Term> fTerms = new LinkedList<Term>();
        private String fSeparator;

        protected CompoundTerm(String sep) {
            fSeparator = sep;
        }

        public List<Term> getTerms() {
            return fTerms;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            String sep = "";
            sb.append("(");
            for (Term t : fTerms) {
                sb.append(sep).append(t);
                sep = fSeparator;
            }
            sb.append(")");
            return sb.toString();
        }
    }

    private class AndTerm extends CompoundTerm {
        public AndTerm() {
            super(" && ");
        }

        @Override
        public boolean evaluate() {
            for (Term t : getTerms())
                if (!t.evaluate())
                    return false;
            return true;
        }
    }

    private class OrTerm extends CompoundTerm {
        public OrTerm() {
            super(" || ");
        }

        @Override
        public boolean evaluate() {
            for (Term t : getTerms())
                if (t.evaluate())
                    return true;
            return false;
        }
    }

    private class NotTerm implements Term {
        private Term fTerm;

        protected NotTerm() {
        }

        public NotTerm(Term t) {
            fTerm = t;
        }

        protected void setTerm(Term t) {
            fTerm = t;
        }

        public Term getTerm() {
            return fTerm;
        }

        @Override
        public boolean evaluate() {
            return !fTerm.evaluate();
        }

        public String toString() {
            return "! " + fTerm;
        }
    }

    private class Rule extends AndTerm {
        private String fID;

        public Rule(String id) {
            fID = id;
        }

        public String getID() {
            return fID;
        }

        public String toString() {
            return "Rule " + fID + " :: " + super.toString();
        }
    }

    private class RuleParser {
        private List<Rule> fRules = new LinkedList<Rule>();
        private List<Term> fTermStack = new LinkedList<Term>();

        private class RuleHandler implements ElementHandler {
            @Override
            public void onStart(ElementPath path) {
                Element el = path.getCurrent();
                String elName = el.getName();
                Term term;
                log.debug(path.getCurrent());
                if (elName.equals("rules")) {
                    return;
                } else if (elName.equals("rule")) {
                    Rule rule = new Rule(el.attributeValue("id"));
                    fRules.add(rule);
                    term = rule;
                } else if (elName.equals("and")) {
                    term = new AndTerm();
                } else if (elName.equals("or")) {
                    term = new OrTerm();
                } else if (elName.equals("not")) {
                    term = new NotTerm();
                } else if (elName.equals("expression")) {
                    term = new Expression(el.attributeValue("value").equals("true"));
                } else {
                    throw new RuntimeException("XML validation should have caught this!");
                    // throw an error
                }

                if (fTermStack.size() > 0) {
                    Term parent = fTermStack.get(0);
                    if (parent instanceof CompoundTerm) {
                        ((CompoundTerm) parent).getTerms().add(term);
                    } else if (parent instanceof NotTerm) {
                        NotTerm not = (NotTerm) parent;
                        if (not.getTerm() != null) {
                            throw new RuntimeException("XML validation should have caught this!");
                        }
                        not.setTerm(term);
                    }
                }
                fTermStack.add(0, term);
            }

            @Override
            public void onEnd(ElementPath path) {
                if (fTermStack.size() > 0)
                    fTermStack.remove(0);
                log.debug(path.getCurrent().getText());
            }
        }

        public RuleParser() {
        }

        public void parse(Reader r) throws DocumentException {
            SAXReader sax = new SAXReader();
            sax.setDefaultHandler(new RuleHandler());
            sax.read(r);
            for (Rule rule : fRules) {
                System.out.println(rule + " evaluates to " + rule.evaluate());
            }
        }
    }

    public static void main(String[] args) throws IOException, DocumentException {
        SaxExample example = new SaxExample();
        //example.parseFiles(args);
        example.parseString("<rules>" +
                "  <rule id='foo'>" +
                "    <and>" +
                "      <or>" +
                "	<expression value='true' />" +
                "	<expression value='false' />" +
                "      </or>" +
                "      <not>" +
                "	<expression value='false'/>" +
                "      </not>" +
                "    </and>" +
                "  </rule>" +
                "  <rule id='bar'>" +
                "    <or>" +
                "      <expression value='false'/>" +
                "      <and>" +
                "	<or>" +
                "	  <expression value='true'/>" +
                "	  <expression value='false'/>" +
                "	</or>" +
                "	<not>" +
                "	  <expression value='false'/>" +
                "	</not>" +
                "      </and>" +
                "    </or>" +
                "  </rule>" +
                "</rules>");
    }

    private void parseString(String s) throws DocumentException {
        RuleParser rp = new RuleParser();
        rp.parse(new StringReader(s));
    }
//	
//	private void parseFiles(String [] args) throws IOException, DocumentException {
//		for (String arg : args) {
//			Reader r = new FileReader(arg);
//			RuleParser rp = new RuleParser();
//			rp.parse(r);
//			r.closeWriter();
//		}
//	}
}
