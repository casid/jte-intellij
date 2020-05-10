package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class StatementTokenParser extends AbstractTokenParser {
    private final KteLexer lexer;

    public StatementTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        int state = lexer.getCurrentState();

        if (state == KteLexer.CONTENT_STATE_HTML && hasToken(position, "!{", KteTokenTypes.STATEMENT_BEGIN)) {
            lexer.setCurrentState(KteLexer.CONTENT_STATE_STATEMENT_BEGIN);
            return true;
        }

        if (state == KteLexer.CONTENT_STATE_STATEMENT_BEGIN && hasToken(position, "}", KteTokenTypes.STATEMENT_END)) {
            lexer.setCurrentState(KteLexer.CONTENT_STATE_STATEMENT_END);
            return true;
        }

        return false;
    }
}
