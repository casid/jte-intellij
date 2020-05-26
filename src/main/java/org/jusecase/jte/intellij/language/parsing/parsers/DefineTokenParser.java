package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class DefineTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public DefineTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@define", JteTokenTypes.DEFINE)) {
            lexer.setCurrentState(JteLexer.CONTENT_STATE_DEFINE_BEGIN);
            return true;
        }
        return false;
    }
}
