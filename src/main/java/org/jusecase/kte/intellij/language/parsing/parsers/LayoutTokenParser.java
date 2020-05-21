package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class LayoutTokenParser extends AbstractTokenParser {
    private final KteLexer lexer;

    public LayoutTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@layout", KteTokenTypes.LAYOUT)) {
            lexer.setCurrentState(KteLexer.CONTENT_STATE_LAYOUT_BEGIN);
            return true;
        }
        return false;
    }
}
