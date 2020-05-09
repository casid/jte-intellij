package org.jusecase.jte.intellij.language.parsing;

import org.junit.Test;

// TODO write proper tests after proof of concept is done!
public class JteLexerTest {
    @Test
    public void testImport() {
        JteLexer lexer = new JteLexer();

        lexer.start("@import test\n");
        System.out.println(lexer.getTokenType());
        lexer.advance();
        System.out.println(lexer.getTokenType());
        lexer.advance();
        System.out.println(lexer.getTokenType());
    }

    @Test
    public void testOutput() {
        JteLexer lexer = new JteLexer();

        lexer.start("@param Model x\nbla ${model.hello} bla");
        System.out.println(lexer.getTokenType() + ": " + lexer.getTokenText());
        lexer.advance();
        System.out.println(lexer.getTokenType() + ": " + lexer.getTokenText());
        lexer.advance();
        System.out.println(lexer.getTokenType() + ": " + lexer.getTokenText());
        lexer.advance();
        System.out.println(lexer.getTokenType() + ": " + lexer.getTokenText());
        lexer.advance();
        System.out.println(lexer.getTokenType() + ": " + lexer.getTokenText());
    }

    @Test
    public void testIfOutput() {
        JteLexer lexer = new JteLexer();

        lexer.start("@param Model x\n@if ((model.x == true) && !somethingElse)\n${model.x}@endif");
        while (lexer.getCurrentPosition().getOffset() < lexer.getBufferEnd()) {
            System.out.println(lexer.getTokenType() + ": " + lexer.getTokenText());
            lexer.advance();
        }
    }

    @Test
    public void testTag_simple() {
        JteLexer lexer = new JteLexer();

        lexer.start("@tag.simple()");

        while (lexer.getCurrentPosition().getOffset() < lexer.getBufferEnd()) {
            System.out.println(lexer.getTokenType() + ": " + lexer.getTokenText());
            lexer.advance();
        }
    }

    @Test
    public void testTag_withParams() {
        JteLexer lexer = new JteLexer();

        lexer.start("@tag.simple(a, b, c)");

        while (lexer.getCurrentPosition().getOffset() < lexer.getBufferEnd()) {
            System.out.println(lexer.getTokenType() + ": " + lexer.getTokenText());
            lexer.advance();
        }
    }

    @Test
    public void testTag_withParamsCallingMethods() {
        JteLexer lexer = new JteLexer();

        lexer.start("@tag.simple(a.getDuration(x.getOffset()), b, c)");

        while (lexer.getCurrentPosition().getOffset() < lexer.getBufferEnd()) {
            System.out.println(lexer.getTokenType() + ": " + lexer.getTokenText());
            lexer.advance();
        }
    }

    @Test
    public void testStatement() {
        JteLexer lexer = new JteLexer();

        lexer.start("!{String x = \"hello\"}");

        while (lexer.getCurrentPosition().getOffset() < lexer.getBufferEnd()) {
            System.out.println(lexer.getTokenType() + ": " + lexer.getTokenText());
            lexer.advance();
        }
    }
}