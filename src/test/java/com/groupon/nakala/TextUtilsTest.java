package com.groupon.nakala;

import com.groupon.nakala.core.RegexpTokenizerStream;
import com.groupon.nakala.core.TextUtils;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author npendar@groupon.com
 */
public class TextUtilsTest {

    @Test
    public final void testWordBefore() {
        assertEquals("is", TextUtils.wordBefore("this is a test.", 8));
        assertEquals("this", TextUtils.wordBefore("this is a test.", 5));
        assertEquals("", TextUtils.wordBefore("this is a test.", 0));
    }

    @Test
    public final void testWordAfter() {
        assertEquals("is", TextUtils.wordAfter("this is a test.", 4));
        assertEquals("a", TextUtils.wordAfter("this is a test.", 7));
        assertEquals("", TextUtils.wordAfter("this is a test.", 14));
    }

    @Test
    public final void testGetWordSet() {
        Set<String> wordSet = TextUtils.getWordSet("This, I would say, this is or would be      a *good* test. ",
                new RegexpTokenizerStream());
        // not normalizing for case
        assertEquals(11, wordSet.size());
        assertTrue(wordSet.contains("this"));
        assertTrue(wordSet.contains("I"));
        assertTrue(wordSet.contains("would"));
        assertTrue(wordSet.contains("good"));
        assertFalse(wordSet.contains(""));
        assertFalse(wordSet.contains(" "));
        assertFalse(wordSet.contains("*"));
    }
}
