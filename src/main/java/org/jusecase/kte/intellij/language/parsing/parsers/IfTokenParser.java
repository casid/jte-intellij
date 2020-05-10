package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class IfTokenParser extends AbstractTokenParser {
    private final KteLexer lexer;

    public IfTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@if", KteTokenTypes.IF)) {
            lexer.setCurrentState(KteLexer.CONTENT_STATE_IF_BEGIN);
            return true;
        }
        return false;
    }
}
