package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class IfTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public IfTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@if", JteTokenTypes.IF)) {
            lexer.setCurrentState(JteLexer.CONTENT_STATE_IF_BEGIN);
            return true;
        }
        return false;
    }
}