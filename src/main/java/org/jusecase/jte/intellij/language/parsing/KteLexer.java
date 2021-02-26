package org.jusecase.jte.intellij.language.parsing;

public class KteLexer extends Lexer {
    public KteLexer() {
        super(KteTokenTypes.INSTANCE);
    }

    @Override
    public boolean isExtraParamInjectionRequired() {
        return false;
    }
}
