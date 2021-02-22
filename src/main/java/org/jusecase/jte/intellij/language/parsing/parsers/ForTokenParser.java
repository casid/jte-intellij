package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class ForTokenParser extends AbstractTokenParser {

    public ForTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@for", lexer.tokens.FOR())) {
            lexer.setCurrentState(Lexer.CONTENT_STATE_FOR_BEGIN);
            return true;
        }
        return false;
    }
}
