package org.jusecase.jte.intellij.language.parsing;

import com.intellij.psi.tree.IElementType;
import org.junit.Assert;

public abstract class LexerTest {
    private final Lexer lexer;

    protected LexerTest(Lexer lexer) {
        this.lexer = lexer;
    }

    protected void givenInput(String input) {
        lexer.start(input);
    }

    protected void thenTokensAre(Object... expectedTokenInfo) {
        StringBuilder expected = new StringBuilder();
        for (Object tokenInfo : expectedTokenInfo) {
            if (tokenInfo instanceof IElementType) {
                appendTokenInfo(expected, (IElementType) tokenInfo);
            } else if (tokenInfo instanceof String) {
                appendTokenInfo(expected, (String) tokenInfo);
            } else {
                throw new IllegalArgumentException("Token info must be either IElementType or String.");
            }
        }

        StringBuilder actual = new StringBuilder();
        while (lexer.getCurrentPosition().getOffset() < lexer.getBufferEnd()) {
            appendTokenInfo(actual, lexer.getTokenType(), lexer.getTokenText());
            lexer.advance();
        }

        Assert.assertEquals(expected.toString(), actual.toString());
    }

    private void appendTokenInfo(StringBuilder result, IElementType tokenType, String tokenText) {
        appendTokenInfo(result, tokenType);
        appendTokenInfo(result, tokenText);
    }

    private void appendTokenInfo(StringBuilder result, IElementType tokenType) {
        result.append(tokenType).append(": ");
    }

    private void appendTokenInfo(StringBuilder result, String tokenText) {
        result.append(tokenText).append('\n');
    }
}
