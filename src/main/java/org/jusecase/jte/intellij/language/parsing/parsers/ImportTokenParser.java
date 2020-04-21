package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class ImportTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public ImportTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@import", JteTokenTypes.IMPORT)) {
            lexer.setState(JteLexer.CONTENT_STATE_JAVA_IMPORT_BEGIN);
            return true;
        }
        return false;
    }
}
