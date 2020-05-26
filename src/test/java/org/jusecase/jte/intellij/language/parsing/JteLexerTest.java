package org.jusecase.jte.intellij.language.parsing;

import com.intellij.psi.tree.IElementType;
import org.junit.Assert;
import org.junit.Test;

import static org.jusecase.jte.intellij.language.parsing.JteTokenTypes.*;

public class JteLexerTest {
    JteLexer lexer = new JteLexer();

    @Test
    public void testImport() {
        givenInput("@import test\n");

        thenTokensAre(
                IMPORT, "@import",
                WHITESPACE, " ",
                JAVA_INJECTION, "test",
                HTML_CONTENT, "\n"
        );
    }

    @Test
    public void testOutput() {
        givenInput("@param Model x\nbla ${model.hello} bla");

        thenTokensAre(
                PARAM, "@param",
                WHITESPACE, " ",
                JAVA_INJECTION, "Model x",
                HTML_CONTENT, "\nbla ",
                OUTPUT_BEGIN, "${",
                JAVA_INJECTION, "model.hello",
                OUTPUT_END, "}",
                HTML_CONTENT, " bla"
        );
    }

    @Test
    public void testIfOutput() {
        givenInput("@param Model x\n@if ((model.x == true) && !somethingElse)\n${model.x}@endif");

        thenTokensAre(
                PARAM, "@param",
                WHITESPACE, " ",
                JAVA_INJECTION, "Model x",
                HTML_CONTENT, "\n",
                IF, "@if",
                WHITESPACE, " ",
                CONDITION_BEGIN, "(",
                JAVA_INJECTION, "(model.x == true) && !somethingElse",
                CONDITION_END, ")",
                HTML_CONTENT, "\n",
                OUTPUT_BEGIN, "${",
                JAVA_INJECTION, "model.x",
                OUTPUT_END, "}",
                ENDIF, "@endif"
        );
    }

    @Test
    public void testTag_simple() {
        givenInput("@tag.simple()");

        thenTokensAre(
                TAG, "@tag",
                NAME_SEPARATOR, ".",
                TAG_NAME, "simple",
                PARAMS_BEGIN, "(",
                PARAMS_END, ")"
        );
    }

    @Test
    public void testTag_simple_withPackage() {
        givenInput("@tag.my.test.simple()");

        thenTokensAre(
                TAG, "@tag",
                NAME_SEPARATOR, ".",
                TAG_NAME, "my",
                NAME_SEPARATOR, ".",
                TAG_NAME, "test",
                NAME_SEPARATOR, ".",
                TAG_NAME, "simple",
                PARAMS_BEGIN, "(",
                PARAMS_END, ")"
        );
    }

    @Test
    public void testTag_withParams() {
        givenInput("@tag.simple(a, b, c)");

        thenTokensAre(
                TAG, "@tag",
                NAME_SEPARATOR, ".",
                TAG_NAME, "simple",
                PARAMS_BEGIN, "(",
                JAVA_INJECTION, "a, b, c",
                PARAMS_END, ")"
        );
    }

    @Test
    public void testTag_withParamsCallingMethods() {
        givenInput("@tag.simple(a.getDuration(x.getOffset()), b, c)");

        thenTokensAre(
                TAG, "@tag",
                NAME_SEPARATOR, ".",
                TAG_NAME, "simple",
                PARAMS_BEGIN, "(",
                JAVA_INJECTION, "a.getDuration(x.getOffset()), b, c",
                PARAMS_END, ")"
        );
    }

    @Test
    public void testLayout_withParamsCallingMethods() {
        givenInput("@layout.simple(a.getDuration(x.getOffset()), b, c)\n" +
                "@define(content)\n" +
                "<p>Hello, ${x}</p>\n" +
                "@enddefine\n" +
                "@endlayout");

        thenTokensAre(
                LAYOUT, "@layout",
                NAME_SEPARATOR, ".",
                LAYOUT_NAME, "simple",
                PARAMS_BEGIN, "(",
                JAVA_INJECTION, "a.getDuration(x.getOffset()), b, c",
                PARAMS_END, ")",
                HTML_CONTENT, "\n",
                DEFINE, "@define",
                PARAMS_BEGIN, "(",
                DEFINE_NAME, "content",
                PARAMS_END, ")",
                HTML_CONTENT, "\n<p>Hello, ",
                OUTPUT_BEGIN, "${",
                JAVA_INJECTION, "x",
                OUTPUT_END, "}",
                HTML_CONTENT, "</p>\n",
                ENDDEFINE, "@enddefine",
                HTML_CONTENT, "\n",
                ENDLAYOUT, "@endlayout"
        );
    }

    @Test
    public void testLayout() {
        givenInput("@param String name\n" +
                "@render(header)\n" +
                "@render(content)\n" +
                "@render(footer)\n");

        thenTokensAre(
                PARAM, "@param",
                WHITESPACE, " ",
                JAVA_INJECTION, "String name",
                HTML_CONTENT, "\n",
                RENDER, "@render",
                PARAMS_BEGIN, "(",
                RENDER_NAME, "header",
                PARAMS_END, ")",
                HTML_CONTENT, "\n",
                RENDER, "@render",
                PARAMS_BEGIN, "(",
                RENDER_NAME, "content",
                PARAMS_END, ")",
                HTML_CONTENT, "\n",
                RENDER, "@render",
                PARAMS_BEGIN, "(",
                RENDER_NAME, "footer",
                PARAMS_END, ")",
                HTML_CONTENT, "\n"
        );
    }

    @Test
    public void testLayout_simple_withPackage() {
        givenInput("@layout.my.test.simple()\n@endlayout");

        thenTokensAre(
                LAYOUT, "@layout",
                NAME_SEPARATOR, ".",
                LAYOUT_NAME, "my",
                NAME_SEPARATOR, ".",
                LAYOUT_NAME, "test",
                NAME_SEPARATOR, ".",
                LAYOUT_NAME, "simple",
                PARAMS_BEGIN, "(",
                PARAMS_END, ")",
                HTML_CONTENT, "\n",
                ENDLAYOUT, "@endlayout"
        );
    }

    @Test
    public void testStatement() {
        givenInput("!{String x = \"hello\"}");

        thenTokensAre(
                STATEMENT_BEGIN, "!{",
                JAVA_INJECTION, "String x = \"hello\"",
                STATEMENT_END, "}"
        );
    }

    @Test
    public void testStatementWithKotlinTemplate() {
        givenInput("!{String x = \"hello ${world}\"}");

        thenTokensAre(
                STATEMENT_BEGIN, "!{",
                JAVA_INJECTION, "String x = \"hello ${world}\"",
                STATEMENT_END, "}"
        );
    }

    private void givenInput(String input) {
        lexer.start(input);
    }

    private void thenTokensAre(Object... expectedTokenInfo) {
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