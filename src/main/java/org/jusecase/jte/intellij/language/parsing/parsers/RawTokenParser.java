package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class RawTokenParser extends AbstractTokenParser {
    public RawTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.isInHtmlState() && hasToken(position, "@raw", lexer.tokens.RAW())) {
            lexer.setCurrentState(Lexer.CONTENT_STATE_RAW);
            return true;
        }

        int state = lexer.getCurrentState();
        if (state == Lexer.CONTENT_STATE_RAW && hasToken(position, "@endraw", lexer.tokens.ENDRAW())) {
            lexer.setCurrentState(Lexer.CONTENT_STATE_HTML);
            return true;
        }

        return false;
    }
}
