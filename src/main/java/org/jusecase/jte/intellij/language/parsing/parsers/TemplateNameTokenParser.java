package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class TemplateNameTokenParser extends AbstractTokenParser {

    public TemplateNameTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == Lexer.CONTENT_STATE_TEMPLATE_BEGIN || lexer.getCurrentState() == Lexer.CONTENT_STATE_TEMPLATE_NAME_BEGIN) {
            if (hasToken(position, ".", lexer.tokens.NAME_SEPARATOR())) {
                lexer.setCurrentState(Lexer.CONTENT_STATE_TEMPLATE_NAME_BEGIN);
                return true;
            }
        }

        return false;
    }
}
