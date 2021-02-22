package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class OutputTokenParser extends AbstractTokenParser {

    public OutputTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        int state = lexer.getCurrentState();

        if (state == Lexer.CONTENT_STATE_HTML && hasToken(position, "${", lexer.tokens.OUTPUT_BEGIN())) {
            lexer.setCurrentState(Lexer.CONTENT_STATE_OUTPUT_BEGIN);
            return true;
        }

        if (state == Lexer.CONTENT_STATE_HTML && hasToken(position, "$unsafe{", lexer.tokens.OUTPUT_BEGIN())) {
            lexer.setCurrentState(Lexer.CONTENT_STATE_OUTPUT_BEGIN);
            return true;
        }

        if (state == Lexer.CONTENT_STATE_OUTPUT_BEGIN && hasToken(position, "}", lexer.tokens.OUTPUT_END())) {
            lexer.setCurrentState(Lexer.CONTENT_STATE_OUTPUT_END);
            return true;
        }

        return false;
    }
}
