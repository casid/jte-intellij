package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class TagNameTokenParser extends AbstractTokenParser {
    private final KteLexer lexer;

    public TagNameTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == KteLexer.CONTENT_STATE_TAG_BEGIN) {
            if (hasToken(position, ".", KteTokenTypes.NAME_BEGIN)) {
                lexer.setCurrentState(KteLexer.CONTENT_STATE_TAG_NAME_BEGIN);
                return true;
            }
        }

        return false;
    }
}
