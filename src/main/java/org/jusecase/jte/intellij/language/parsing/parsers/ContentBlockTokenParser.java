package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class ContentBlockTokenParser extends AbstractTokenParser {

    public ContentBlockTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@`", lexer.tokens.CONTENT_BEGIN())) {
            if (lexer.getCurrentState() == Lexer.CONTENT_STATE_PARAM_NAME) {
                lexer.setCurrentState(Lexer.CONTENT_STATE_TAG_PARAMS);
            }
            lexer.pushPreviousState();
            lexer.setCurrentState(Lexer.CONTENT_STATE_HTML);
            lexer.setCurrentCount(1); // To prevent syntax highlighter bugs, see https://intellij-support.jetbrains.com/hc/en-us/community/posts/115000549784-Is-there-any-way-to-start-syntax-highlighting-lexer-for-the-whole-file-instead-of-starting-it-for-the-part-that-have-changed-
            return true;
        }
        return false;
    }
}
