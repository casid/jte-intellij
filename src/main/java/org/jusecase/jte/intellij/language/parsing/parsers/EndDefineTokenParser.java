package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class EndDefineTokenParser extends AbstractTokenParser {
    @Override
    public boolean hasToken(int position) {
        return hasToken(position, "@enddefine", JteTokenTypes.ENDDEFINE);
    }
}
