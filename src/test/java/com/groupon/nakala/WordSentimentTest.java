package com.groupon.nakala;

import com.groupon.nakala.core.WordSentiment;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author npendar@groupon.com
 */
public class WordSentimentTest {

    @Test
    public final void testPolarity() throws Exception {
        WordSentiment ws = WordSentiment.getInstance();

        assertEquals(1, ws.polarity("greatest"));
        assertEquals(0, ws.polarity("went"));
        assertEquals(-1, ws.polarity("worst"));
    }

}
