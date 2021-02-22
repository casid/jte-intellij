package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class ParamTokenParser extends AbstractTokenParser {

    public ParamTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (!lexer.isImportOrParamIgnored() && hasToken(position, "@param", lexer.tokens.PARAM())) {
            lexer.setCurrentState(Lexer.CONTENT_STATE_PARAM_BEGIN);
            return true;
        }
        return false;
    }
}
