package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class StatementTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public StatementTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        int state = lexer.getCurrentState();

        if (state == JteLexer.CONTENT_STATE_HTML && hasToken(position, "!{", JteTokenTypes.STATEMENT_BEGIN)) {
            lexer.setCurrentState(JteLexer.CONTENT_STATE_STATEMENT_BEGIN);
            return true;
        }

        if (state == JteLexer.CONTENT_STATE_STATEMENT_BEGIN && hasToken(position, "}", JteTokenTypes.STATEMENT_END)) {
            lexer.setCurrentState(JteLexer.CONTENT_STATE_STATEMENT_END);
            return true;
        }

        return false;
    }
}
