package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class ElseTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public ElseTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        return hasToken(position, "@else", JteTokenTypes.ELSE);
    }
}
