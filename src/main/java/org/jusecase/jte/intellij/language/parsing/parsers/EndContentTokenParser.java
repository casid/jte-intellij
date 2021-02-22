package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class EndContentTokenParser extends AbstractTokenParser {

    public EndContentTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "`", lexer.tokens.CONTENT_END())) {
            lexer.popPreviousState();
            return true;
        }
        return false;
    }
}
