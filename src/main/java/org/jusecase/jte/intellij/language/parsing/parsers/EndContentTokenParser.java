package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class EndContentTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public EndContentTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@endcontent", JteTokenTypes.ENDCONTENT)) {
            lexer.popPreviousState();
            return true;
        }
        return false;
    }
}
