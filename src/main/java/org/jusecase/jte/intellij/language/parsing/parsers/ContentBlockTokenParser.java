package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class ContentBlockTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public ContentBlockTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@`", JteTokenTypes.CONTENT_BEGIN)) {
            if (lexer.getCurrentState() == JteLexer.CONTENT_STATE_PARAM_NAME) {
                lexer.setCurrentState(JteLexer.CONTENT_STATE_TAG_PARAMS);
            }
            lexer.pushPreviousState();
            lexer.setCurrentState(JteLexer.CONTENT_STATE_HTML);
            return true;
        }
        return false;
    }
}
