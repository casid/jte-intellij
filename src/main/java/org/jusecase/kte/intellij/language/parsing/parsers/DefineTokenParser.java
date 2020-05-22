package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class DefineTokenParser extends AbstractTokenParser {
    private final KteLexer lexer;

    public DefineTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@define", KteTokenTypes.DEFINE)) {
            lexer.setCurrentState(KteLexer.CONTENT_STATE_DEFINE_BEGIN);
            return true;
        }
        return false;
    }
}
