package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class ContentBlockTokenParser extends AbstractTokenParser {

    public ContentBlockTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (hasToken(position, "@`", lexer.tokens.CONTENT_BEGIN())) {
            if (lexer.getCurrentState() == Lexer.CONTENT_STATE_PARAM_NAME) {
                lexer.setCurrentState(Lexer.CONTENT_STATE_TEMPLATE_PARAMS);
            }
            lexer.pushPreviousState();

            // To prevent syntax highlighter bugs, see https://intellij-support.jetbrains.com/hc/en-us/community/posts/115000549784-Is-there-any-way-to-start-syntax-highlighting-lexer-for-the-whole-file-instead-of-starting-it-for-the-part-that-have-changed-
            // The highlighting lexer is only restarted at points where it returned 0 from getState, so we use the special state CONTENT_STATE_HTML_CONTENT_BLOCK for content blocks,
            // which rely on a deque to restore the previous state (they can't handle everything in one integer).
            lexer.setCurrentState(Lexer.CONTENT_STATE_HTML_CONTENT_BLOCK);
            return true;
        }
        return false;
    }
}
