package com.groupon.nakala;

import com.groupon.nakala.core.Lexicon;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author npendar@groupon.com
 */
public class LexiconTest {

    @Test
    public final void testIsEnglish() throws Exception {
        Lexicon lex = Lexicon.newInstance("english");

        assertTrue(lex.isInLexicon("the"));
        assertTrue(lex.isInLexicon("people"));
        assertTrue(lex.isInLexicon("excellent"));
        assertTrue(lex.isInLexicon("indispensible"));
        assertTrue(!lex.isInLexicon(null));
        assertTrue(!lex.isInLexicon(""));
        assertTrue(!lex.isInLexicon("fdksjlf"));
    }

    @Test
    public final void testIsTextEnglish() throws Exception {
        Lexicon lex = Lexicon.newInstance("english");

        String english = "The defects of my home-made Berlitz became painfully "
                + "evident. It’s humiliating, when you have your 2000 new nouns at "
                + "your fingers’ ends, and hundreds of old ones; and yet can’t "
                + "understand the first thing a knecht say.";

        String german = "Der kleine Herr Friedemann kümmerte sich nicht viel um "
                + "die drei Mädchen: sie aber hielten treu zusammen und waren stets "
                + "einer Meinung. Besonders wenn eine Verlobung in ihrer Bekanntschaft "
                + "sich ereignete, betonten sie einstimmig, dass dies ja sehr erfreulich sei.";

        double score = lex.percentWordsInLexicon(english);
        assertTrue(score >= 0.7);

        score = lex.percentWordsInLexicon(german);
        assertTrue(score < 0.7);
    }
}
