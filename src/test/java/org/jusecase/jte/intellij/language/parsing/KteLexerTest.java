package org.jusecase.jte.intellij.language.parsing;

import org.junit.Test;

import static org.jusecase.jte.intellij.language.parsing.KteTokenTypes.*;

public class KteLexerTest extends LexerTest {

    public KteLexerTest() {
        super(new KteLexer());
    }

    @Test
    public void param_nativeDefaultValueSupport() {
        givenInput("@param x = 32");
        thenTokensAre(PARAM, "@param",
                WHITESPACE, " ",
                JAVA_INJECTION, "x = 32");
    }
}
