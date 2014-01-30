package com.groupon.nakala.core;

import com.groupon.nakala.normalization.StringNormalizer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by nickp on 1/29/14.
 */
public abstract class AbstractTokenizerStream implements TokenizerStream {

    @Override
    public Set<String> getUniqueTokens(String s) {
        setText(s);
        Set<String> uniques = new HashSet<String>();
        String tok = null;
        while ((tok = next()) != null)
            uniques.add(tok);
        return uniques;
    }

    @Override
    public List<String> getTokens(String s) {
        setText(s);
        List<String> tokens = new LinkedList<String>();
        String tok = null;
        while ((tok = next()) != null)
            tokens.add(tok);
        return tokens;
    }

    @Override
    public Set<String> getUniqueTokens(String s, List<StringNormalizer> normalizers) {
        setText(s);
        Set<String> uniques = new HashSet<String>();
        String tok = null;
        while ((tok = next()) != null) {
            if (normalizers != null) {
                for (StringNormalizer normalizer : normalizers) {
                    tok = normalizer.normalize(tok);
                }
            }
            uniques.add(tok);
        }
        return uniques;
    }

    @Override
    public List<String> getTokens(String s, List<StringNormalizer> normalizers) {
        setText(s);
        List<String> tokens = new LinkedList<String>();
        String tok = null;
        while ((tok = next()) != null) {
            if (normalizers != null) {
                for (StringNormalizer normalizer : normalizers) {
                    tok = normalizer.normalize(tok);
                }
            }
            tokens.add(tok);
        }
        return tokens;
    }
}
