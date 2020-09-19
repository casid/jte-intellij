package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class ParamTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public ParamTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (!lexer.isImportOrParamIgnored() && hasToken(position, "@param", JteTokenTypes.PARAM)) {
            lexer.setCurrentState(JteLexer.CONTENT_STATE_PARAM_BEGIN);
            return true;
        }
        return false;
    }
}
