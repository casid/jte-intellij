package org.jusecase.jte.intellij.language.parsing;

public class JteLexer extends Lexer {
    public JteLexer() {
        super(JteTokenTypes.INSTANCE);
    }

    @Override
    public boolean isExtraParamInjectionRequired() {
        return true;
    }
}
