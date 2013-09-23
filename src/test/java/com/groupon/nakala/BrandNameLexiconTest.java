package com.groupon.nakala;

import com.groupon.nakala.core.BrandNameLexicon;
import opennlp.tools.util.Span;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author npendar@groupon.com
 */
public class BrandNameLexiconTest {

    @Test
    public final void testIsBrandName() throws IOException {
        BrandNameLexicon b = BrandNameLexicon.getInstance();
        assertTrue(b.isBrandName("Caesar's Palace"));
        assertFalse(b.isBrandName("table"));
    }

    @Test
    public final void testGetBrandNames() throws IOException {
        BrandNameLexicon b = BrandNameLexicon.getInstance();
        String s = "We'll be staying at Bradford Homesuites and eat at Monicals Pizza and then go back to Bradford Homesuites.";
        Set<String> bns = b.getBrandNames(s);
        assertTrue(bns.size() == 2);
        assertTrue(bns.contains("Bradford Homesuites"));
        assertTrue(bns.contains("Monicals Pizza"));
    }

    @Test
    public final void testFindBrandNames() throws IOException {
        BrandNameLexicon b = BrandNameLexicon.getInstance();
        String s = "We'll be staying at Bradford Homesuites and eat at Monicals Pizza and then go back to Bradford Homesuites.";
        List<Span> spans = b.findBrandNames(s);
        assertTrue(spans.size() == 3);
        assertEquals("Bradford Homesuites", s.subSequence(spans.get(0).getStart(), spans.get(0).getEnd()));
        assertEquals("Bradford Homesuites", s.subSequence(spans.get(1).getStart(), spans.get(1).getEnd()));
        assertEquals("Monicals Pizza", s.subSequence(spans.get(2).getStart(), spans.get(2).getEnd()));
    }
}
