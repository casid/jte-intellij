package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class TagTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public TagTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@tag", JteTokenTypes.TAG)) {
            lexer.setCurrentState(JteLexer.CONTENT_STATE_JAVA_TAG_BEGIN);
            return true;
        }
        return false;
    }
}
