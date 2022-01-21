package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class TemplateParamsTokenParser extends AbstractTokenParser {

    public TemplateParamsTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == Lexer.CONTENT_STATE_TEMPLATE_NAME_BEGIN) {
            if (hasToken(position, "(", lexer.tokens.PARAMS_BEGIN())) {
                lexer.setCurrentState(Lexer.CONTENT_STATE_TEMPLATE_PARAMS);
                return true;
            }
        } else if (lexer.getCurrentState() == Lexer.CONTENT_STATE_TEMPLATE_PARAMS && lexer.getCurrentCount() <= 0) {
            if (hasToken(position, ")", lexer.tokens.PARAMS_END())) {
                lexer.setCurrentCount(0);
                lexer.setCurrentState(Lexer.CONTENT_STATE_TEMPLATE_END);
                return true;
            }
        }

        return false;
    }
}
