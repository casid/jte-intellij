package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class TagTokenParser extends AbstractTokenParser {

    public TagTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@tag", lexer.tokens.TAG())) {
            lexer.setCurrentState(Lexer.CONTENT_STATE_TAG_BEGIN);
            return true;
        }
        return false;
    }
}
