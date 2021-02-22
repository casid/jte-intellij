package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class ForConditionTokenParser extends AbstractTokenParser {

    public ForConditionTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == Lexer.CONTENT_STATE_FOR_BEGIN) {
            if (hasToken(position, "(", lexer.tokens.CONDITION_BEGIN())) {
                lexer.setCurrentState(Lexer.CONTENT_STATE_FOR_CONDITION);
                return true;
            }
        } else if (lexer.getCurrentState() == Lexer.CONTENT_STATE_FOR_CONDITION && lexer.getCurrentCount() <= 0) {
            if (hasToken(position, ")", lexer.tokens.CONDITION_END())) {
                lexer.setCurrentState(Lexer.CONTENT_STATE_FOR_END);
                return true;
            }
        }

        return false;
    }
}
