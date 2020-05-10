package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class ImportTokenParser extends AbstractTokenParser {
    private final KteLexer lexer;

    public ImportTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@import", KteTokenTypes.IMPORT)) {
            lexer.setCurrentState(KteLexer.CONTENT_STATE_IMPORT_BEGIN);
            return true;
        }
        return false;
    }
}
