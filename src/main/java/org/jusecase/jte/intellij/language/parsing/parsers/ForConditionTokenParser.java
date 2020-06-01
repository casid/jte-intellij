package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class ForConditionTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public ForConditionTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == JteLexer.CONTENT_STATE_FOR_BEGIN) {
            if (hasToken(position, "(", JteTokenTypes.CONDITION_BEGIN)) {
                lexer.setCurrentState(JteLexer.CONTENT_STATE_FOR_CONDITION);
                return true;
            }
        } else if (lexer.getCurrentState() == JteLexer.CONTENT_STATE_FOR_CONDITION && lexer.getCurrentCount() <= 0) {
            if (hasToken(position, ")", JteTokenTypes.CONDITION_END)) {
                lexer.setCurrentState(JteLexer.CONTENT_STATE_FOR_END);
                return true;
            }
        }

        return false;
    }
}