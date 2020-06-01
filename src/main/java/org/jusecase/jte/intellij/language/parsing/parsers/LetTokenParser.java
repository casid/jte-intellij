package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class LetTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public LetTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@let", JteTokenTypes.LET)) {
            lexer.setCurrentState(JteLexer.CONTENT_STATE_IF_BEGIN);
            return true;
        }
        return false;
    }
}
