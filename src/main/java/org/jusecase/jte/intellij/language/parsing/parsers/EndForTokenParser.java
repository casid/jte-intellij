package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class EndForTokenParser extends AbstractTokenParser {

    public EndForTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        return hasToken(position, "@endfor", lexer.tokens.ENDFOR());
    }
}
