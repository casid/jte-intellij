package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class OutputTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public OutputTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        int state = lexer.getCurrentState();

        if (state == JteLexer.CONTENT_STATE_HTML && hasToken(position, "${", JteTokenTypes.OUTPUT_BEGIN)) {
            lexer.setCurrentState(JteLexer.CONTENT_STATE_JAVA_OUTPUT_BEGIN);
            return true;
        }

        if (state == JteLexer.CONTENT_STATE_JAVA_OUTPUT_BEGIN && hasToken(position, "}", JteTokenTypes.OUTPUT_END)) {
            lexer.setCurrentState(JteLexer.CONTENT_STATE_JAVA_OUTPUT_END);
            return true;
        }

        return false;
    }
}
