package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class ImportTokenParser extends AbstractTokenParser {

    public ImportTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (!lexer.isImportOrParamIgnored() && hasToken(position, "@import", lexer.tokens.IMPORT())) {
            lexer.setCurrentState(Lexer.CONTENT_STATE_IMPORT_BEGIN);
            return true;
        }
        return false;
    }
}
