package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class StatementTokenParser extends AbstractTokenParser {

    public StatementTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        int state = lexer.getCurrentState();

        if (state == Lexer.CONTENT_STATE_HTML && hasToken(position, "!{", lexer.tokens.STATEMENT_BEGIN())) {
            lexer.setCurrentState(Lexer.CONTENT_STATE_STATEMENT_BEGIN);
            return true;
        }

        if (state == Lexer.CONTENT_STATE_STATEMENT_BEGIN && hasToken(position, "}", lexer.tokens.STATEMENT_END())) {
            lexer.setCurrentState(Lexer.CONTENT_STATE_STATEMENT_END);
            return true;
        }

        return false;
    }
}
