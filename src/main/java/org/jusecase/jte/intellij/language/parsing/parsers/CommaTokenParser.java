package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class CommaTokenParser extends AbstractTokenParser {
    public CommaTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == Lexer.CONTENT_STATE_PARAM_NAME) {
            return hasToken(position, ",", lexer.tokens.COMMA());
        }
        return false;
    }
}
