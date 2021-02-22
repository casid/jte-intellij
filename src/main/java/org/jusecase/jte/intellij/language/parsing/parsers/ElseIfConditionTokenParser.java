package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class ElseIfConditionTokenParser extends AbstractTokenParser {

    public ElseIfConditionTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == Lexer.CONTENT_STATE_ELSEIF_BEGIN) {
            if (hasToken(position, "(", lexer.tokens.CONDITION_BEGIN())) {
                lexer.setCurrentState(Lexer.CONTENT_STATE_ELSEIF_CONDITION);
                return true;
            }
        } else if (lexer.getCurrentState() == Lexer.CONTENT_STATE_ELSEIF_CONDITION && lexer.getCurrentCount() <= 0) {
            if (hasToken(position, ")", lexer.tokens.CONDITION_END())) {
                lexer.setCurrentCount(0);
                lexer.setCurrentState(Lexer.CONTENT_STATE_ELSEIF_END);
                return true;
            }
        }

        return false;
    }
}
