package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class ContentTokenParser extends AbstractTokenParser {
    private static final String[] KEYWORDS = {
            "${",
            "!{",
            "<%--",
            "@if",
            "@else",
            "@elseif",
            "@endif",
            "@for",
            "@endfor",
            "@import",
            "@param",
            "@tag",
            "@layout", // TODO
            "@define", // TODO
            "@render" // TODO
    };

    private final JteLexer lexer;

    public ContentTokenParser(JteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (shouldSkipWhitespaces() && isWhitespace(position)) {
            return skipWhitespaces(position);
        }

        if (isBeginOfJteKeyword(position)) {
            return false;
        }

        if (lexer.isInJavaEndState()) {
            lexer.setCurrentState(JteLexer.CONTENT_STATE_HTML);
        }

        int currentState = lexer.getCurrentState();
        int start = position;
        position++;

        while (position < myEndOffset && !isBeginOfJteKeyword(position)) {
            position++;
        }

        if (currentState == JteLexer.CONTENT_STATE_JAVA_TAG_NAME_BEGIN) {
            myTokenInfo.updateData(start, position, JteTokenTypes.TAG_NAME);
        } else if (currentState == JteLexer.CONTENT_STATE_HTML) {
            myTokenInfo.updateData(start, position, JteTokenTypes.HTML_CONTENT);
        } else {
            myTokenInfo.updateData(start, position, JteTokenTypes.JAVA_INJECTION);
        }
        return true;
    }

    private boolean shouldSkipWhitespaces() {
        int state = lexer.getCurrentState();
        return
                state == JteLexer.CONTENT_STATE_JAVA_IMPORT_BEGIN ||
                state == JteLexer.CONTENT_STATE_JAVA_PARAM_BEGIN ||
                state == JteLexer.CONTENT_STATE_JAVA_FOR_BEGIN ||
                state == JteLexer.CONTENT_STATE_JAVA_ELSEIF_BEGIN ||
                state == JteLexer.CONTENT_STATE_JAVA_IF_BEGIN;
    }

    private boolean isBeginOfJteKeyword(int position) {
        for (String keyword : KEYWORDS) {
            if (isBeginOf(position, keyword)) {
                return true;
            }
        }

        if (isBeginOf(position, '\n')) {
            switch (lexer.getCurrentState()) {
                case JteLexer.CONTENT_STATE_JAVA_IMPORT_BEGIN:
                    lexer.setCurrentState(JteLexer.CONTENT_STATE_JAVA_IMPORT_END);
                    return true;
                case JteLexer.CONTENT_STATE_JAVA_PARAM_BEGIN:
                    lexer.setCurrentState(JteLexer.CONTENT_STATE_JAVA_PARAM_END);
                    return true;
            }
        }

        if (isBeginOf(position, '}')) {
            switch (lexer.getCurrentState()) {
                case JteLexer.CONTENT_STATE_JAVA_OUTPUT_BEGIN:
                case JteLexer.CONTENT_STATE_JAVA_STATEMENT_BEGIN:
                    return true;
            }
        }

        if (isBeginOf(position, '(')) {
            switch (lexer.getCurrentState()) {
                case JteLexer.CONTENT_STATE_JAVA_IF_BEGIN:
                case JteLexer.CONTENT_STATE_JAVA_ELSEIF_BEGIN:
                case JteLexer.CONTENT_STATE_JAVA_FOR_BEGIN:
                case JteLexer.CONTENT_STATE_JAVA_TAG_NAME_BEGIN:
                    return true;
                case JteLexer.CONTENT_STATE_JAVA_IF_CONDITION:
                case JteLexer.CONTENT_STATE_JAVA_ELSEIF_CONDITION:
                case JteLexer.CONTENT_STATE_JAVA_FOR_CONDITION:
                case JteLexer.CONTENT_STATE_JAVA_TAG_PARAMS:
                    lexer.incrementCurrentCount();
                    return false;
            }
        }

        if (isBeginOf(position, ')')) {
            switch (lexer.getCurrentState()) {
                case JteLexer.CONTENT_STATE_JAVA_IF_CONDITION:
                case JteLexer.CONTENT_STATE_JAVA_ELSEIF_CONDITION:
                case JteLexer.CONTENT_STATE_JAVA_FOR_CONDITION:
                case JteLexer.CONTENT_STATE_JAVA_TAG_PARAMS:
                    int count = lexer.getCurrentCount();
                    lexer.decrementCurrentCount();
                    return count <= 0;
            }
        }

        if (isBeginOf(position, '.')) {
            switch (lexer.getCurrentState()) {
                case JteLexer.CONTENT_STATE_JAVA_TAG_BEGIN:
                    return true;
            }
        }

        return false;
    }
}
