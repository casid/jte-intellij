package org.jusecase.kte.intellij.language.parsing;

import com.intellij.psi.tree.IElementType;
import org.junit.Assert;
import org.junit.Test;

import static org.jusecase.kte.intellij.language.parsing.KteTokenTypes.*;

public class KteLexerTest {
    KteLexer lexer = new KteLexer();

    @Test
    public void testImport() {
        givenInput("@import test\n");

        thenTokensAre(
                IMPORT, "@import",
                WHITESPACE, " ",
                KOTLIN_INJECTION, "test",
                HTML_CONTENT, "\n"
        );
    }

    @Test
    public void testOutput() {
        givenInput("@param x:Model\nbla ${model.hello} bla");

        thenTokensAre(
                PARAM, "@param",
                WHITESPACE, " ",
                KOTLIN_INJECTION, "x:Model",
                HTML_CONTENT, "\nbla ",
                OUTPUT_BEGIN, "${",
                KOTLIN_INJECTION, "model.hello",
                OUTPUT_END, "}",
                HTML_CONTENT, " bla"
        );
    }

    @Test
    public void testIfOutput() {
        givenInput("@param x:Model\n@if ((model.x == true) && !somethingElse)\n${model.x}@endif");

        thenTokensAre(
                PARAM, "@param",
                WHITESPACE, " ",
                KOTLIN_INJECTION, "x:Model",
                HTML_CONTENT, "\n",
                IF, "@if",
                WHITESPACE, " ",
                CONDITION_BEGIN, "(",
                KOTLIN_INJECTION, "(model.x == true) && !somethingElse",
                CONDITION_END, ")",
                HTML_CONTENT, "\n",
                OUTPUT_BEGIN, "${",
                KOTLIN_INJECTION, "model.x",
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
                KOTLIN_INJECTION, "a, b, c",
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
                KOTLIN_INJECTION, "a.getDuration(x.getOffset()), b, c",
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
                KOTLIN_INJECTION, "a.getDuration(x.getOffset()), b, c",
                PARAMS_END, ")",
                HTML_CONTENT, "\n",
                DEFINE, "@define",
                PARAMS_BEGIN, "(",
                DEFINE_NAME, "content",
                PARAMS_END, ")",
                HTML_CONTENT, "\n<p>Hello, ",
                OUTPUT_BEGIN, "${",
                KOTLIN_INJECTION, "x",
                OUTPUT_END, "}",
                HTML_CONTENT, "</p>\n",
                ENDDEFINE, "@enddefine",
                HTML_CONTENT, "\n",
                ENDLAYOUT, "@endlayout"
        );
    }

    @Test
    public void testLayout() {
        givenInput("@param name:String\n" +
                "@render(header)\n" +
                "@render(content)\n" +
                "@render(footer)\n");

        thenTokensAre(
                PARAM, "@param",
                WHITESPACE, " ",
                KOTLIN_INJECTION, "name:String",
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
        givenInput("!{x:String = \"hello\"}");

        thenTokensAre(
                STATEMENT_BEGIN, "!{",
                KOTLIN_INJECTION, "x:String = \"hello\"",
                STATEMENT_END, "}"
        );
    }

//    @Test
//    public void testStatementWithKotlinTemplate() {
//        KteLexer lexer = new KteLexer();
//
//        lexer.start("!{String x = \"hello ${world}\"}");
//
//        while (lexer.getCurrentPosition().getOffset() < lexer.getBufferEnd()) {
//            System.out.println(lexer.getTokenType() + ": " + lexer.getTokenText());
//            lexer.advance();
//        }
//    }

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