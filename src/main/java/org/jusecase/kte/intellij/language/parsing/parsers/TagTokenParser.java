package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class TagTokenParser extends AbstractTokenParser {
    private final KteLexer lexer;

    public TagTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@tag", KteTokenTypes.TAG)) {
            lexer.setCurrentState(KteLexer.CONTENT_STATE_TAG_BEGIN);
            return true;
        }
        return false;
    }
}
