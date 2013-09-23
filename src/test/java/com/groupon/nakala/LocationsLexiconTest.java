package com.groupon.nakala;

import com.groupon.nakala.core.LocationsLexicon;
import opennlp.tools.util.Span;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author npendar@groupon.com
 */
public class LocationsLexiconTest {

    @Test
    public final void testIsLocation() throws IOException {
        LocationsLexicon l = LocationsLexicon.getInstance();
        assertTrue(l.isLocation("New York"));
        assertFalse(l.isLocation("table"));
    }

    @Test
    public final void testFindLocations() throws IOException {
        LocationsLexicon l = LocationsLexicon.getInstance();
        String s = "We'll be travelling to Salt Lake City next year and after that to San Francisco.";
        List<Span> spans = l.findLocations(s);
        assertTrue(spans.size() == 2);
        assertEquals("Salt Lake City", s.subSequence(spans.get(0).getStart(), spans.get(0).getEnd()));
        assertEquals("San Francisco", s.subSequence(spans.get(1).getStart(), spans.get(1).getEnd()));

        s = " I think East Trion and East Troy are both great cities, but I like East Troy better.";
        spans = l.findLocations(s);
        assertTrue(spans.size() == 3);
        assertEquals("East Trion", s.subSequence(spans.get(0).getStart(), spans.get(0).getEnd()));
        assertEquals("East Troy", s.subSequence(spans.get(1).getStart(), spans.get(1).getEnd()));
        assertEquals("East Troy", s.subSequence(spans.get(2).getStart(), spans.get(2).getEnd()));
    }

    @Test
    public final void testGetLocations() throws IOException {
        LocationsLexicon l = LocationsLexicon.getInstance();
        String s = "They'll arrive at Abbotsford Airport and then drive to Absecon Highlands for the weekend.";
        Set<String> locs = l.getLocations(s);
        assertTrue(locs.size() == 2);
        assertTrue(locs.contains("Abbotsford Airport"));
        assertTrue(locs.contains("Absecon Highlands"));

        s = " I think East Trion and East Troy are both great cities, but I like East Troy better.";
        locs = l.getLocations(s);
        assertTrue(locs.size() == 2);
        assertTrue(locs.contains("East Trion"));
        assertTrue(locs.contains("East Troy"));
    }
}
