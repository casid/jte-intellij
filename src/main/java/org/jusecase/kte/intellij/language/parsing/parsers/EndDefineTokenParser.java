package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class EndDefineTokenParser extends AbstractTokenParser {
    @Override
    public boolean hasToken(int position) {
        return hasToken(position, "@enddefine", KteTokenTypes.ENDDEFINE);
    }
}