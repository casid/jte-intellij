package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class EqualsTokenParser extends AbstractTokenParser {

    public EqualsTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == Lexer.CONTENT_STATE_PARAM_DEFAULT_VALUE) {
            return hasToken(position, "=", lexer.tokens.EQUALS());
        }

        if (lexer.getCurrentState() == Lexer.CONTENT_STATE_PARAM_NAME) {
            if (hasToken(position, "=", lexer.tokens.EQUALS())) {
                if (lexer.getCurrentCount() == Lexer.CONTENT_COUNT_PARAM_NAME_TEMPLATE) {
                    lexer.setCurrentCount(Lexer.CONTENT_COUNT_PARAM_NAME_TEMPLATE_DONE);
                    return true;
                }
            }
        }

        return false;
    }
}
