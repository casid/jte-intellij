package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class ElseTokenParser extends AbstractTokenParser {
    private final KteLexer lexer;

    public ElseTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        return hasToken(position, "@else", KteTokenTypes.ELSE);
    }
}
