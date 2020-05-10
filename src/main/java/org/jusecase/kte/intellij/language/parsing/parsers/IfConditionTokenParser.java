package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class IfConditionTokenParser extends AbstractTokenParser {
    private final KteLexer lexer;

    public IfConditionTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == KteLexer.CONTENT_STATE_IF_BEGIN) {
            if (hasToken(position, "(", KteTokenTypes.CONDITION_BEGIN)) {
                lexer.setCurrentState(KteLexer.CONTENT_STATE_IF_CONDITION);
                return true;
            }
        } else if (lexer.getCurrentState() == KteLexer.CONTENT_STATE_IF_CONDITION && lexer.getCurrentCount() <= 0) {
            if (hasToken(position, ")", KteTokenTypes.CONDITION_END)) {
                lexer.setCurrentCount(0);
                lexer.setCurrentState(KteLexer.CONTENT_STATE_IF_END);
                return true;
            }
        }

        return false;
    }
}
