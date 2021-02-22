package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class EndIfTokenParser extends AbstractTokenParser {

    public EndIfTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        return hasToken(position, "@endif", lexer.tokens.ENDIF());
    }
}
