package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class IfConditionTokenParser extends AbstractTokenParser {

    public IfConditionTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == Lexer.CONTENT_STATE_IF_BEGIN) {
            if (hasToken(position, "(", lexer.tokens.CONDITION_BEGIN())) {
                lexer.setCurrentState(Lexer.CONTENT_STATE_IF_CONDITION);
                return true;
            }
        } else if (lexer.getCurrentState() == Lexer.CONTENT_STATE_IF_CONDITION && lexer.getCurrentCount() <= 0) {
            if (hasToken(position, ")", lexer.tokens.CONDITION_END())) {
                lexer.setCurrentCount(0);
                lexer.setCurrentState(Lexer.CONTENT_STATE_IF_END);
                return true;
            }
        }

        return false;
    }
}
