package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class ContentTokenParser extends AbstractTokenParser {
    private static final String[] KEYWORDS = {
            "${",
            "!{",
            "@if",
            "@else",
            "@elseif",
            "@endif",
            "@for",
            "@import",
            "@param",
            "@tag",
            "@layout",
            "@section"
    };

    private final JteLexer lexer;

    public ContentTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (lexer.getState() == JteLexer.CONTENT_STATE_JAVA_IMPORT_BEGIN && isWhitespace(position)) {
            return skipWhitespaces(position);
        }

        if (isBeginOfJteKeyword(position)) {
            return false;
        }

        if (lexer.isInJavaEndState()) {
            lexer.setState(JteLexer.CONTENT_STATE_HTML);
        }

        int currentState = lexer.getState();
        int start = position;
        position++;

        while (position < myEndOffset && !isBeginOfJteKeyword(position)) {
            position++;
        }

        if (currentState == JteLexer.CONTENT_STATE_HTML) {
            myTokenInfo.updateData(start, position, JteTokenTypes.HTML_CONTENT);
        } else {
            myTokenInfo.updateData(start, position, JteTokenTypes.JAVA_INJECTION);
        }
        return true;
    }

    private boolean isBeginOfJteKeyword(int position) {
        for (String keyword : KEYWORDS) {
            if (isBeginOf(position, keyword)) {
                return true;
            }
        }

        if (isBeginOf(position, "\n")) {
            switch (lexer.getState()) {
                case JteLexer.CONTENT_STATE_JAVA_IMPORT_BEGIN:
                    lexer.setState(JteLexer.CONTENT_STATE_JAVA_IMPORT_END);
                    return true;
                case JteLexer.CONTENT_STATE_JAVA_PARAM_BEGIN:
                    lexer.setState(JteLexer.CONTENT_STATE_JAVA_PARAM_END);
                    return true;
            }
        }

        if (isBeginOf(position, "}")) {
            switch (lexer.getState()) {
                case JteLexer.CONTENT_STATE_JAVA_OUTPUT_BEGIN:
                    return true;
            }
        }

        return false;
    }
}
