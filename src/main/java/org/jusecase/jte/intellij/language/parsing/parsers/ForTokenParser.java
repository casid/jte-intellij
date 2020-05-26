package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class ForTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public ForTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@for", JteTokenTypes.FOR)) {
            lexer.setCurrentState(JteLexer.CONTENT_STATE_FOR_BEGIN);
            return true;
        }
        return false;
    }
}
