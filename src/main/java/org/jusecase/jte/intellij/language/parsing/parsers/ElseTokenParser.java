package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class ElseTokenParser extends AbstractTokenParser {

    public ElseTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        return hasToken(position, "@else", lexer.tokens.ELSE());
    }
}
