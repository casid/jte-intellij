package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class TagNameTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public TagNameTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getCurrentState() == JteLexer.CONTENT_STATE_JAVA_TAG_BEGIN) {
            if (hasToken(position, ".", JteTokenTypes.NAME_BEGIN)) {
                lexer.setCurrentState(JteLexer.CONTENT_STATE_JAVA_TAG_NAME_BEGIN);
                return true;
            }
        }

        return false;
    }
}
