package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class IfTokenParser extends AbstractTokenParser {

    public IfTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@if", lexer.tokens.IF())) {
            lexer.setCurrentState(Lexer.CONTENT_STATE_IF_BEGIN);
            return true;
        }
        return false;
    }
}
