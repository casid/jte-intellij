package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class EqualsTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public EqualsTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == JteLexer.CONTENT_STATE_PARAM_DEFAULT_VALUE) {
            return hasToken(position, "=", JteTokenTypes.EQUALS);
        }

        if (lexer.getCurrentState() == JteLexer.CONTENT_STATE_PARAM_NAME) {
            if (hasToken(position, "=", JteTokenTypes.EQUALS)) {
                if (lexer.getCurrentCount() == JteLexer.CONTENT_COUNT_PARAM_NAME_TAG) {
                    lexer.setCurrentCount(JteLexer.CONTENT_COUNT_PARAM_NAME_TAG_DONE);
                    return true;
                }
            }
        }

        return false;
    }
}
