package com.groupon.nakala;

import com.groupon.nakala.core.Taboo;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author npendar@groupon.com
 */
public class TabooTest {

    @Test
    public void testIsTaboo() throws IOException {
        Taboo taboo = null;
        taboo = Taboo.newInstance();

        assertNotNull(taboo.getTabooSet());
        assertTrue(taboo.isTaboo("asshole"));
        assertTrue(taboo.isTaboo("ass pirate"));
        assertTrue(!taboo.isTaboo("indignant"));
        assertTrue(!taboo.isTaboo("pirates of the carribean"));
    }
}
