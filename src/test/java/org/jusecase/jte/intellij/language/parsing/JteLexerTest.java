package org.jusecase.jte.intellij.language.parsing;

import org.junit.Test;

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
}