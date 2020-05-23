package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class LayoutNameTokenParser extends AbstractTokenParser {
    private final KteLexer lexer;

    public LayoutNameTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == KteLexer.CONTENT_STATE_LAYOUT_BEGIN || lexer.getCurrentState() == KteLexer.CONTENT_STATE_LAYOUT_NAME_BEGIN) {
            if (hasToken(position, ".", KteTokenTypes.NAME_SEPARATOR)) {
                lexer.setCurrentState(KteLexer.CONTENT_STATE_LAYOUT_NAME_BEGIN);
                return true;
            }
        }

        return false;
    }
}
