package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class ParamTokenParser extends AbstractTokenParser {
    private final KteLexer lexer;

    public ParamTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@param", KteTokenTypes.PARAM)) {
            lexer.setCurrentState(KteLexer.CONTENT_STATE_PARAM_BEGIN);
            return true;
        }
        return false;
    }
}
