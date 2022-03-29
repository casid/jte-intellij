package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class TemplateTokenParser extends AbstractTokenParser {

    public TemplateTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (isBeginOf(position + "@template".length(), '.') && hasToken(position, "@template", lexer.tokens.TEMPLATE())) {
            lexer.setCurrentState(Lexer.CONTENT_STATE_TEMPLATE_BEGIN);
            return true;
        }
        return false;
    }
}
