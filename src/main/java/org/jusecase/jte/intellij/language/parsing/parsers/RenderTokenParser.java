package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class RenderTokenParser extends AbstractTokenParser {
    private final JteLexer lexer;

    public RenderTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@render", JteTokenTypes.RENDER)) {
            lexer.setCurrentState(JteLexer.CONTENT_STATE_RENDER_BEGIN);
            return true;
        }
        return false;
    }
}
