package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class ContentBlockTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public ContentBlockTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@content", JteTokenTypes.CONTENT)) {
            lexer.pushPreviousState();
            lexer.setCurrentState(JteLexer.CONTENT_STATE_HTML);
            return true;
        }
        return false;
    }
}
