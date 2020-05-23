package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class DefineNameTokenParser extends AbstractTokenParser {
    private final KteLexer lexer;

    public DefineNameTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == KteLexer.CONTENT_STATE_DEFINE_BEGIN) {
            if (hasToken(position, "(", KteTokenTypes.PARAMS_BEGIN)) {
                lexer.setCurrentState(KteLexer.CONTENT_STATE_DEFINE_NAME);
                return true;
            }
        } else if (lexer.getCurrentState() == KteLexer.CONTENT_STATE_DEFINE_NAME && lexer.getCurrentCount() <= 0) {
            if (hasToken(position, ")", KteTokenTypes.PARAMS_END)) {
                lexer.setCurrentCount(0);
                lexer.setCurrentState(KteLexer.CONTENT_STATE_DEFINE_END);
                return true;
            }
        }

        return false;
    }
}