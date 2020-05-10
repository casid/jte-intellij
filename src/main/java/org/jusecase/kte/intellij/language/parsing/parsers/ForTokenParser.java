package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class ForTokenParser extends AbstractTokenParser {
    private final KteLexer lexer;

    public ForTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@for", KteTokenTypes.FOR)) {
            lexer.setCurrentState(KteLexer.CONTENT_STATE_FOR_BEGIN);
            return true;
        }
        return false;
    }
}
