package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

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

    private final KteLexer lexer;

    public ContentTokenParser(KteLexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public boolean hasToken(int position) {
        if (shouldSkipWhitespaces() && isWhitespace(position)) {
            return skipWhitespaces(position);
        }

        if (isBeginOfKteKeyword(position)) {
            return false;
        }

        if (lexer.isInKotlinEndState()) {
            lexer.setCurrentState(KteLexer.CONTENT_STATE_HTML);
        }

        int currentState = lexer.getCurrentState();
        int start = position;
        position++;

        while (position < myEndOffset && !isBeginOfKteKeyword(position)) {
            position++;
        }

        if (currentState == KteLexer.CONTENT_STATE_TAG_NAME_BEGIN) {
            myTokenInfo.updateData(start, position, KteTokenTypes.TAG_NAME);
        } else if (currentState == KteLexer.CONTENT_STATE_HTML) {
            myTokenInfo.updateData(start, position, KteTokenTypes.HTML_CONTENT);
        } else {
            myTokenInfo.updateData(start, position, KteTokenTypes.KOTLIN_INJECTION);
        }
        return true;
    }

    private boolean shouldSkipWhitespaces() {
        int state = lexer.getCurrentState();
        return
                state == KteLexer.CONTENT_STATE_IMPORT_BEGIN ||
                state == KteLexer.CONTENT_STATE_PARAM_BEGIN ||
                state == KteLexer.CONTENT_STATE_FOR_BEGIN ||
                state == KteLexer.CONTENT_STATE_ELSEIF_BEGIN ||
                state == KteLexer.CONTENT_STATE_IF_BEGIN;
    }

    private boolean isBeginOfKteKeyword(int position) {
        for (String keyword : KEYWORDS) {
            if (isBeginOf(position, keyword)) {
                return true;
            }
        }

        if (isBeginOf(position, '=')) {
            if (lexer.getCurrentState() == KteLexer.CONTENT_STATE_PARAM_BEGIN) {
                lexer.setCurrentState(KteLexer.CONTENT_STATE_PARAM_END);
                return true;
            }
        }

        if (isBeginOf(position, '\n')) {
            switch (lexer.getCurrentState()) {
                case KteLexer.CONTENT_STATE_IMPORT_BEGIN:
                    lexer.setCurrentState(KteLexer.CONTENT_STATE_IMPORT_END);
                    return true;
                case KteLexer.CONTENT_STATE_PARAM_BEGIN:
                    lexer.setCurrentState(KteLexer.CONTENT_STATE_PARAM_END);
                    return true;
            }
        }

        if (isBeginOf(position, '}')) {
            switch (lexer.getCurrentState()) {
                case KteLexer.CONTENT_STATE_OUTPUT_BEGIN:
                case KteLexer.CONTENT_STATE_STATEMENT_BEGIN:
                    return true;
            }
        }

        if (isBeginOf(position, '(')) {
            switch (lexer.getCurrentState()) {
                case KteLexer.CONTENT_STATE_IF_BEGIN:
                case KteLexer.CONTENT_STATE_ELSEIF_BEGIN:
                case KteLexer.CONTENT_STATE_FOR_BEGIN:
                case KteLexer.CONTENT_STATE_TAG_NAME_BEGIN:
                    return true;
                case KteLexer.CONTENT_STATE_IF_CONDITION:
                case KteLexer.CONTENT_STATE_ELSEIF_CONDITION:
                case KteLexer.CONTENT_STATE_FOR_CONDITION:
                case KteLexer.CONTENT_STATE_TAG_PARAMS:
                    lexer.incrementCurrentCount();
                    return false;
            }
        }

        if (isBeginOf(position, ')')) {
            switch (lexer.getCurrentState()) {
                case KteLexer.CONTENT_STATE_IF_CONDITION:
                case KteLexer.CONTENT_STATE_ELSEIF_CONDITION:
                case KteLexer.CONTENT_STATE_FOR_CONDITION:
                case KteLexer.CONTENT_STATE_TAG_PARAMS:
                    int count = lexer.getCurrentCount();
                    lexer.decrementCurrentCount();
                    return count <= 0;
            }
        }

        if (isBeginOf(position, '.')) {
            switch (lexer.getCurrentState()) {
                case KteLexer.CONTENT_STATE_TAG_BEGIN:
                    return true;
            }
        }

        return false;
    }
}
