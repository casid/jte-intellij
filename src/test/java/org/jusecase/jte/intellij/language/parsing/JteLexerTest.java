package org.jusecase.jte.intellij.language.parsing;

import com.intellij.psi.CustomHighlighterTokenType;
import com.intellij.psi.tree.IElementType;
import org.junit.Assert;
import org.junit.Test;

import static org.jusecase.jte.intellij.language.parsing.JteTokenTypes.*;

public class JteLexerTest {
    final JteLexer lexer = new JteLexer();

    @Test
    public void testImport() {
        givenInput("@import test\n");

        thenTokensAre(
                IMPORT, "@import",
                WHITESPACE, " ",
                JAVA_INJECTION, "test",
                WHITESPACE, "\n"
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
                WHITESPACE, "\n",
                IF, "@if",
                WHITESPACE, " ",
                CONDITION_BEGIN, "(",
                JAVA_INJECTION, "(model.x == true) && !somethingElse",
                CONDITION_END, ")",
                WHITESPACE, "\n",
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
    public void testTag_withNamedParams1() {
        givenInput("@tag.named(one=\"Hello\")");
        thenTokensAre(
                TAG, "@tag",
                NAME_SEPARATOR, ".",
                TAG_NAME, "named",
                PARAMS_BEGIN, "(",
                PARAM_NAME, "one",
                EQUALS, "=",
                JAVA_INJECTION, "\"Hello\"",
                PARAMS_END, ")"
        );
    }

    @Test
    public void testTag_withNamedParams2() {
        givenInput("@tag.named(two = 1 == 2 ? 1 : 0, one = 1)");
        thenTokensAre(
                TAG, "@tag",
                NAME_SEPARATOR, ".",
                TAG_NAME, "named",
                PARAMS_BEGIN, "(",
                PARAM_NAME, "two",
                WHITESPACE, " ",
                EQUALS, "=",
                WHITESPACE, " ",
                JAVA_INJECTION, "1 == 2 ? 1 : 0",
                COMMA, ",",
                WHITESPACE, " ",
                PARAM_NAME, "one",
                WHITESPACE, " ",
                EQUALS, "=",
                WHITESPACE, " ",
                JAVA_INJECTION, "1",
                PARAMS_END, ")"
                );
    }

    @Test
    public void testTag_withNamedParams3() {
        givenInput("@tag.named(one=\"Hello, my name = two\")");
        thenTokensAre(
                TAG, "@tag",
                NAME_SEPARATOR, ".",
                TAG_NAME, "named",
                PARAMS_BEGIN, "(",
                PARAM_NAME, "one",
                EQUALS, "=",
                JAVA_INJECTION, "\"Hello, my name = two\"",
                PARAMS_END, ")"
        );
    }

    @Test
    public void testTag_withNamedParams4() {
        givenInput("@tag.named(one=\"Hello, my name = \\\"two\")");
        thenTokensAre(
                TAG, "@tag",
                NAME_SEPARATOR, ".",
                TAG_NAME, "named",
                PARAMS_BEGIN, "(",
                PARAM_NAME, "one",
                EQUALS, "=",
                JAVA_INJECTION, "\"Hello, my name = \\\"two\"",
                PARAMS_END, ")"
        );
    }

    @Test
    public void testTag_withNamedParams5() {
        givenInput("@tag.named(\none=\"Hello\"\n)");
        thenTokensAre(
                TAG, "@tag",
                NAME_SEPARATOR, ".",
                TAG_NAME, "named",
                PARAMS_BEGIN, "(",
                WHITESPACE, "\n",
                PARAM_NAME, "one",
                EQUALS, "=",
                JAVA_INJECTION, "\"Hello\"",
                WHITESPACE, "\n",
                PARAMS_END, ")"
        );
    }

    @Test
    public void testTag_withNamedParams6() {
        givenInput("@tag.named(\r\none=\"Hello\"\r\n)");
        thenTokensAre(
                TAG, "@tag",
                NAME_SEPARATOR, ".",
                TAG_NAME, "named",
                PARAMS_BEGIN, "(",
                WHITESPACE, "\r\n",
                PARAM_NAME, "one",
                EQUALS, "=",
                JAVA_INJECTION, "\"Hello\"",
                WHITESPACE, "\r\n",
                PARAMS_END, ")"
        );
    }

    @Test
    public void testLayout_withParamsCallingMethods() {
        givenInput("@layout.simple(a.getDuration(x.getOffset(), 5), b, c, content = @`\n" +
                "<p>Hello, ${x}</p>\n" +
                "`)");

        thenTokensAre(
                LAYOUT, "@layout",
                NAME_SEPARATOR, ".",
                TAG_NAME, "simple",
                PARAMS_BEGIN, "(",
                JAVA_INJECTION, "a.getDuration(x.getOffset(), 5), b, c",
                COMMA, ",",
                WHITESPACE, " ",
                PARAM_NAME, "content",
                WHITESPACE, " ",
                EQUALS, "=",
                WHITESPACE, " ",
                CONTENT_BEGIN, "@`",
                HTML_CONTENT, "\n<p>Hello, ",
                OUTPUT_BEGIN, "${",
                JAVA_INJECTION, "x",
                OUTPUT_END, "}",
                HTML_CONTENT, "</p>\n",
                CONTENT_END, "`",
                PARAMS_END, ")"
        );
    }

    @Test
    public void testLayout_simple_withPackage() {
        givenInput("@layout.my.test.simple()\n");

        thenTokensAre(
                LAYOUT, "@layout",
                NAME_SEPARATOR, ".",
                TAG_NAME, "my",
                NAME_SEPARATOR, ".",
                TAG_NAME, "test",
                NAME_SEPARATOR, ".",
                TAG_NAME, "simple",
                PARAMS_BEGIN, "(",
                PARAMS_END, ")",
                WHITESPACE, "\n"
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

    @Test
    public void testFor() {
        givenInput("@for(int z = 0; z < 10; ++z)${z}@endfor");

        thenTokensAre(
                FOR, "@for",
                CONDITION_BEGIN, "(",
                JAVA_INJECTION, "int z = 0; z < 10; ++z",
                CONDITION_END, ")",
                OUTPUT_BEGIN, "${",
                JAVA_INJECTION, "z",
                OUTPUT_END, "}",
                ENDFOR, "@endfor"
        );
    }

    @Test
    public void testParam() {
        givenInput("@param");
        thenTokensAre(PARAM, "@param");
    }

    @Test
    public void testParam_typing() {
        givenInput("@param Pa");
        thenTokensAre(
                PARAM, "@param",
                WHITESPACE, " ",
                JAVA_INJECTION, "Pa");
    }

    @Test
    public void testParam_defaultValue() {
        givenInput("@param String value = \"something\"\n" +
                "Hello ${value}");
        thenTokensAre(
                PARAM, "@param",
                WHITESPACE, " ",
                JAVA_INJECTION, "String value",
                WHITESPACE, " ",
                EQUALS, "=",
                WHITESPACE, " ",
                EXTRA_JAVA_INJECTION, "\"something\"",
                HTML_CONTENT, "\nHello ",
                OUTPUT_BEGIN, "${",
                JAVA_INJECTION, "value",
                OUTPUT_END, "}"
        );
    }

    @Test
    public void testCommentBeforeParam() {
        givenInput("<%-- Comment --%>\n" +
                "@param String value = \"something\"\n" +
                "Hello ${value}");
        thenTokensAre(
                COMMENT, "<%-- Comment --%>",
                WHITESPACE, "\n",
                PARAM, "@param",
                WHITESPACE, " ",
                JAVA_INJECTION, "String value",
                WHITESPACE, " ",
                EQUALS, "=",
                WHITESPACE, " ",
                EXTRA_JAVA_INJECTION, "\"something\"",
                HTML_CONTENT, "\nHello ",
                OUTPUT_BEGIN, "${",
                JAVA_INJECTION, "value",
                OUTPUT_END, "}"
        );
    }

    @Test
    public void testContentWithinTagParam() {
        givenInput("@tag.test(foo = @`<b>static</b>`)");
        thenTokensAre(
                TAG, "@tag",
                NAME_SEPARATOR, ".",
                TAG_NAME, "test",
                PARAMS_BEGIN, "(",
                PARAM_NAME, "foo",
                WHITESPACE, " ",
                EQUALS, "=",
                WHITESPACE, " ",
                CONTENT_BEGIN, "@`",
                HTML_CONTENT, "<b>static</b>",
                CONTENT_END, "`",
                PARAMS_END, ")"
        );
    }

    @Test
    public void testContentWithinTagParam_output() {
        givenInput("@tag.test(foo = @`<b>${data}</b>`)");
        thenTokensAre(
                TAG, "@tag",
                NAME_SEPARATOR, ".",
                TAG_NAME, "test",
                PARAMS_BEGIN, "(",
                PARAM_NAME, "foo",
                WHITESPACE, " ",
                EQUALS, "=",
                WHITESPACE, " ",
                CONTENT_BEGIN, "@`",
                HTML_CONTENT, "<b>",
                OUTPUT_BEGIN, "${",
                JAVA_INJECTION, "data",
                OUTPUT_END, "}",
                HTML_CONTENT, "</b>",
                CONTENT_END, "`",
                PARAMS_END, ")"
        );
    }

    @Test
    public void testContentWithinJava() {
        givenInput("@tag.test(foo = localize(key, @`<b>static</b>!{var x = \"Hello\";}${x}`, 3))");
        thenTokensAre(
                TAG, "@tag",
                NAME_SEPARATOR, ".",
                TAG_NAME, "test",
                PARAMS_BEGIN, "(",
                PARAM_NAME, "foo",
                WHITESPACE, " ",
                EQUALS, "=",
                WHITESPACE, " ",
                JAVA_INJECTION, "localize(key, ",
                CONTENT_BEGIN, "@`",
                HTML_CONTENT, "<b>static</b>",
                STATEMENT_BEGIN, "!{",
                JAVA_INJECTION, "var x = \"Hello\";",
                STATEMENT_END, "}",
                OUTPUT_BEGIN, "${",
                JAVA_INJECTION, "x",
                OUTPUT_END, "}",
                CONTENT_END, "`",
                JAVA_INJECTION, ", 3)",
                PARAMS_END, ")"
        );
    }

    @Test
    public void nestedContent() {
        givenInput("@param String value\n" +
                "@param test.Localizer localizer\n" +
                "@tag.simple(text = localizer.localize(\"key\", @`\n" +
                "        @tag.verySimple(value = @`<b>${value}</b>`, localizer = localizer)\n" +
                "    `,\n" +
                "    @`<b>${value}</b>`, \"bar\")\n" +
                ")");
        thenTokensAre(
                PARAM, "@param",
                WHITESPACE, " ",
                JAVA_INJECTION, "String value",
                WHITESPACE, "\n",
                PARAM, "@param",
                WHITESPACE, " ",
                JAVA_INJECTION, "test.Localizer localizer",
                WHITESPACE, "\n",
                TAG, "@tag",
                NAME_SEPARATOR, ".",
                TAG_NAME, "simple",
                PARAMS_BEGIN, "(",
                PARAM_NAME, "text",
                WHITESPACE, " ",
                EQUALS, "=",
                WHITESPACE, " ",
                JAVA_INJECTION, "localizer.localize(\"key\", ",
                CONTENT_BEGIN, "@`",
                WHITESPACE, "\n        ",
                TAG, "@tag",
                NAME_SEPARATOR, ".",
                TAG_NAME, "verySimple",
                PARAMS_BEGIN, "(",
                PARAM_NAME, "value",
                WHITESPACE, " ",
                EQUALS, "=",
                WHITESPACE, " ",
                CONTENT_BEGIN, "@`",
                HTML_CONTENT, "<b>",
                OUTPUT_BEGIN, "${",
                JAVA_INJECTION, "value",
                OUTPUT_END, "}",
                HTML_CONTENT, "</b>",
                CONTENT_END, "`",
                COMMA, ",",
                WHITESPACE, " ",
                PARAM_NAME, "localizer",
                WHITESPACE, " ",
                EQUALS, "=",
                WHITESPACE, " ",
                JAVA_INJECTION, "localizer",
                PARAMS_END, ")",
                WHITESPACE, "\n    ",
                CONTENT_END, "`",
                JAVA_INJECTION, ",\n    ",
                CONTENT_BEGIN, "@`",
                HTML_CONTENT, "<b>",
                OUTPUT_BEGIN, "${",
                JAVA_INJECTION, "value",
                OUTPUT_END, "}",
                HTML_CONTENT, "</b>",
                CONTENT_END, "`",
                JAVA_INJECTION, ", \"bar\")",
                WHITESPACE, "\n",
                PARAMS_END, ")"
        );
    }

    @Test
    public void contentOutput() {
        givenInput("${display(@`<b>${foo}</b>`)}");
        thenTokensAre(
                OUTPUT_BEGIN, "${",
                JAVA_INJECTION, "display(",
                CONTENT_BEGIN, "@`",
                HTML_CONTENT, "<b>",
                OUTPUT_BEGIN, "${",
                JAVA_INJECTION, "foo",
                OUTPUT_END, "}",
                HTML_CONTENT, "</b>",
                CONTENT_END, "`",
                JAVA_INJECTION, ")",
                OUTPUT_END, "}"
        );
    }

    @Test
    public void defaultParamValues() {
        givenInput("@param Content content = @`x${Integer.MAX_VALUE}`\n" +
                "@param Content content2 = null\n");

        thenTokensAre(
                PARAM, "@param",
                WHITESPACE, " ",
                JAVA_INJECTION, "Content content",
                WHITESPACE, " ",
                EQUALS, "=",
                WHITESPACE, " ",
                CONTENT_BEGIN, "@`",
                HTML_CONTENT, "x",
                OUTPUT_BEGIN, "${",
                JAVA_INJECTION, "Integer.MAX_VALUE",
                OUTPUT_END, "}",
                CONTENT_END, "`",
                CustomHighlighterTokenType.WHITESPACE, "\n",
                PARAM, "@param",
                WHITESPACE, " ",
                JAVA_INJECTION, "Content content2",
                WHITESPACE, " ",
                EQUALS, "=",
                WHITESPACE, " ",
                EXTRA_JAVA_INJECTION, "null",
                WHITESPACE, "\n"
        );
    }

    @Test
    public void cssImport() {
        givenInput("<style type=\"text/css\" rel=\"stylesheet\" media=\"all\">\n" +
                "    @import url(\"https://fonts.googleapis.com/css?family=Nunito+Sans:400,700&display=swap\"); /* <--- Right here */");
        thenTokensAre(HTML_CONTENT, "<style type=\"text/css\" rel=\"stylesheet\" media=\"all\">\n" +
                "    @import url(\"https://fonts.googleapis.com/css?family=Nunito+Sans:400,700&display=swap\"); /* <--- Right here */");
    }

    @Test
    public void paramAfterOutput() {
        givenInput("Hello @param");
        thenTokensAre(HTML_CONTENT, "Hello @param");
    }

    @Test // See https://github.com/casid/jte/issues/27
    public void incompleteIf() {
        givenInput("@if(\n@for(int i = 0; i < 1; ++i)\n@endfor");

        thenTokensAre(
                IF, "@if",
                CONDITION_BEGIN, "(",
                JAVA_INJECTION, "\n",
                FOR, "@for",
                CONDITION_BEGIN, "(",
                JAVA_INJECTION, "int i = 0; i < 1; ++i",
                CONDITION_END, ")",
                WHITESPACE, "\n",
                ENDFOR, "@endfor"
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