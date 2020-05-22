package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class RenderTokenParser extends AbstractTokenParser {
    private final KteLexer lexer;

    public RenderTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@render", KteTokenTypes.RENDER)) {
            lexer.setCurrentState(KteLexer.CONTENT_STATE_RENDER_BEGIN);
            return true;
        }
        return false;
    }
}
