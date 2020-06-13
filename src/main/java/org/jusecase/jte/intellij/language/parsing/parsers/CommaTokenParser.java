package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class CommaTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public CommaTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == JteLexer.CONTENT_STATE_PARAM_NAME) {
            return hasToken(position, ",", JteTokenTypes.COMMA);
        }
        return false;
    }
}
