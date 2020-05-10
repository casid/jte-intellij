package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class ElseIfTokenParser extends AbstractTokenParser {
    private final KteLexer lexer;

    public ElseIfTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@elseif", KteTokenTypes.ELSEIF)) {
            lexer.setCurrentState(KteLexer.CONTENT_STATE_ELSEIF_BEGIN);
            return true;
        }
        return false;
    }
}
