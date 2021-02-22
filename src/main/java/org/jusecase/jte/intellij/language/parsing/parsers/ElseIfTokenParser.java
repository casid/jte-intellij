package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class ElseIfTokenParser extends AbstractTokenParser {

    public ElseIfTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@elseif", lexer.tokens.ELSEIF())) {
            lexer.setCurrentState(Lexer.CONTENT_STATE_ELSEIF_BEGIN);
            return true;
        }
        return false;
    }
}
