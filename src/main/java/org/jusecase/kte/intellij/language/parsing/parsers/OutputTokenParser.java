package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class OutputTokenParser extends AbstractTokenParser {
    private final KteLexer lexer;

    public OutputTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        int state = lexer.getCurrentState();

        if (state == KteLexer.CONTENT_STATE_HTML && hasToken(position, "${", KteTokenTypes.OUTPUT_BEGIN)) {
            lexer.setCurrentState(KteLexer.CONTENT_STATE_OUTPUT_BEGIN);
            return true;
        }

        if (state == KteLexer.CONTENT_STATE_OUTPUT_BEGIN && hasToken(position, "}", KteTokenTypes.OUTPUT_END)) {
            lexer.setCurrentState(KteLexer.CONTENT_STATE_OUTPUT_END);
            return true;
        }

        return false;
    }
}
