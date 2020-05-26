package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class LayoutNameTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public LayoutNameTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == JteLexer.CONTENT_STATE_LAYOUT_BEGIN || lexer.getCurrentState() == JteLexer.CONTENT_STATE_LAYOUT_NAME_BEGIN) {
            if (hasToken(position, ".", JteTokenTypes.NAME_SEPARATOR)) {
                lexer.setCurrentState(JteLexer.CONTENT_STATE_LAYOUT_NAME_BEGIN);
                return true;
            }
        }

        return false;
    }
}
