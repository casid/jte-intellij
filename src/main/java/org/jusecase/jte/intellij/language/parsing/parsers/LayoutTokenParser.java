package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class LayoutTokenParser extends AbstractTokenParser {

    public LayoutTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@layout", lexer.tokens.LAYOUT())) {
            lexer.setCurrentState(Lexer.CONTENT_STATE_TAG_BEGIN);
            return true;
        }
        return false;
    }
}
