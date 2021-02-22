package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class TagParamsTokenParser extends AbstractTokenParser {

    public TagParamsTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == Lexer.CONTENT_STATE_TAG_NAME_BEGIN) {
            if (hasToken(position, "(", lexer.tokens.PARAMS_BEGIN())) {
                lexer.setCurrentState(Lexer.CONTENT_STATE_TAG_PARAMS);
                return true;
            }
        } else if (lexer.getCurrentState() == Lexer.CONTENT_STATE_TAG_PARAMS && lexer.getCurrentCount() <= 0) {
            if (hasToken(position, ")", lexer.tokens.PARAMS_END())) {
                lexer.setCurrentCount(0);
                lexer.setCurrentState(Lexer.CONTENT_STATE_TAG_END);
                return true;
            }
        }

        return false;
    }
}
