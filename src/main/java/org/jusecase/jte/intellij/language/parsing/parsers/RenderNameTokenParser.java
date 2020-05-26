package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class RenderNameTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public RenderNameTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == JteLexer.CONTENT_STATE_RENDER_BEGIN) {
            if (hasToken(position, "(", JteTokenTypes.PARAMS_BEGIN)) {
                lexer.setCurrentState(JteLexer.CONTENT_STATE_RENDER_NAME);
                return true;
            }
        } else if (lexer.getCurrentState() == JteLexer.CONTENT_STATE_RENDER_NAME && lexer.getCurrentCount() <= 0) {
            if (hasToken(position, ")", JteTokenTypes.PARAMS_END)) {
                lexer.setCurrentCount(0);
                lexer.setCurrentState(JteLexer.CONTENT_STATE_RENDER_END);
                return true;
            }
        }

        return false;
    }
}
