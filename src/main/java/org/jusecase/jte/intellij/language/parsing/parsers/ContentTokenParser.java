package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class ContentTokenParser extends AbstractTokenParser {
    private static final String[] KEYWORDS = {
            "${",
            "$safe{",
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
            "@layout",
            "@define",
            "@enddefine",
            "@render",
            "@endlayout"
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

        if (lexer.isInJavaEndState()) {
            lexer.setCurrentState(JteLexer.CONTENT_STATE_HTML);
        }

        if (isBeginOfJteKeyword(position)) {
            return false;
        }

        int currentState = lexer.getCurrentState();
        int start = position;
        position++;

        while (position < myEndOffset && !isBeginOfJteKeyword(position)) {
            position++;
        }

        if (currentState == JteLexer.CONTENT_STATE_TAG_NAME_BEGIN) {
            myTokenInfo.updateData(start, position, JteTokenTypes.TAG_NAME);
        } else if (currentState == JteLexer.CONTENT_STATE_LAYOUT_NAME_BEGIN) {
            myTokenInfo.updateData(start, position, JteTokenTypes.LAYOUT_NAME);
        } else if (currentState == JteLexer.CONTENT_STATE_DEFINE_NAME) {
            myTokenInfo.updateData(start, position, JteTokenTypes.DEFINE_NAME);
        } else if (currentState == JteLexer.CONTENT_STATE_RENDER_NAME) {
            myTokenInfo.updateData(start, position, JteTokenTypes.RENDER_NAME);
        } else if (currentState == JteLexer.CONTENT_STATE_HTML) {
            myTokenInfo.updateData(start, position, JteTokenTypes.HTML_CONTENT);
        } else if (currentState == JteLexer.CONTENT_STATE_PARAM_DEFAULT_VALUE) {
            myTokenInfo.updateData(start, position, JteTokenTypes.EXTRA_JAVA_INJECTION);
        } else {
            myTokenInfo.updateData(start, position, JteTokenTypes.JAVA_INJECTION);
        }
        return true;
    }

    private boolean shouldSkipWhitespaces() {
        int state = lexer.getCurrentState();
        return
                state == JteLexer.CONTENT_STATE_IMPORT_BEGIN ||
                        state == JteLexer.CONTENT_STATE_PARAM_BEGIN ||
                        state == JteLexer.CONTENT_STATE_FOR_BEGIN ||
                        state == JteLexer.CONTENT_STATE_ELSEIF_BEGIN ||
                        state == JteLexer.CONTENT_STATE_IF_BEGIN ||
                        state == JteLexer.CONTENT_STATE_DEFINE_NAME ||
                        state == JteLexer.CONTENT_STATE_RENDER_NAME ||
                        state == JteLexer.CONTENT_STATE_PARAM_DEFAULT_VALUE;
    }

    private boolean isBeginOfJteKeyword(int position) {
        if (lexer.getCurrentState() == JteLexer.CONTENT_STATE_HTML || lexer.isInJavaEndState()) {
            for (String keyword : KEYWORDS) {
                if (isBeginOf(position, keyword)) {
                    return true;
                }
            }
        }

        if (isBeginOf(position, '\n')) {
            switch (lexer.getCurrentState()) {
                case JteLexer.CONTENT_STATE_IMPORT_BEGIN:
                    lexer.setCurrentState(JteLexer.CONTENT_STATE_IMPORT_END);
                    return true;
                case JteLexer.CONTENT_STATE_PARAM_BEGIN:
                case JteLexer.CONTENT_STATE_PARAM_DEFAULT_VALUE:
                    lexer.setCurrentState(JteLexer.CONTENT_STATE_PARAM_END);
                    return true;
            }
        }

        if (lexer.getCurrentState() == JteLexer.CONTENT_STATE_PARAM_BEGIN) {
            for (int index = position; index < myEndOffset; ++index) {
                if (myBuffer.charAt(index) == '=') {
                    lexer.setCurrentState(JteLexer.CONTENT_STATE_PARAM_DEFAULT_VALUE);
                    return true;
                }

                if (!isWhitespace(index)) {
                    return false;
                }
            }
        }

        if (lexer.getCurrentState() == JteLexer.CONTENT_STATE_PARAM_DEFAULT_VALUE && isBeginOf(position, '=')) {
            return true;
        }

        if (isBeginOf(position, '{')) {
            switch (lexer.getCurrentState()) {
                case JteLexer.CONTENT_STATE_OUTPUT_BEGIN:
                case JteLexer.CONTENT_STATE_STATEMENT_BEGIN:
                    lexer.incrementCurrentCount();
                    return false;
            }
        }

        if (isBeginOf(position, '}')) {
            switch (lexer.getCurrentState()) {
                case JteLexer.CONTENT_STATE_OUTPUT_BEGIN:
                case JteLexer.CONTENT_STATE_STATEMENT_BEGIN:
                    int count = lexer.getCurrentCount();
                    lexer.decrementCurrentCount();
                    return count <= 0;
            }
        }

        if (isBeginOf(position, '(')) {
            switch (lexer.getCurrentState()) {
                case JteLexer.CONTENT_STATE_IF_BEGIN:
                case JteLexer.CONTENT_STATE_ELSEIF_BEGIN:
                case JteLexer.CONTENT_STATE_FOR_BEGIN:
                case JteLexer.CONTENT_STATE_TAG_NAME_BEGIN:
                case JteLexer.CONTENT_STATE_LAYOUT_NAME_BEGIN:
                case JteLexer.CONTENT_STATE_DEFINE_BEGIN:
                case JteLexer.CONTENT_STATE_RENDER_BEGIN:
                    return true;
                case JteLexer.CONTENT_STATE_IF_CONDITION:
                case JteLexer.CONTENT_STATE_ELSEIF_CONDITION:
                case JteLexer.CONTENT_STATE_FOR_CONDITION:
                case JteLexer.CONTENT_STATE_TAG_PARAMS:
                case JteLexer.CONTENT_STATE_LAYOUT_PARAMS:
                    lexer.incrementCurrentCount();
                    return false;
            }
        }

        if (isBeginOf(position, ')')) {
            switch (lexer.getCurrentState()) {
                case JteLexer.CONTENT_STATE_IF_CONDITION:
                case JteLexer.CONTENT_STATE_ELSEIF_CONDITION:
                case JteLexer.CONTENT_STATE_FOR_CONDITION:
                case JteLexer.CONTENT_STATE_TAG_PARAMS:
                case JteLexer.CONTENT_STATE_LAYOUT_PARAMS:
                case JteLexer.CONTENT_STATE_DEFINE_NAME:
                case JteLexer.CONTENT_STATE_RENDER_NAME:
                    int count = lexer.getCurrentCount();
                    lexer.decrementCurrentCount();
                    return count <= 0;
            }
        }

        if (isBeginOf(position, '.')) {
            switch (lexer.getCurrentState()) {
                case JteLexer.CONTENT_STATE_TAG_BEGIN:
                case JteLexer.CONTENT_STATE_LAYOUT_BEGIN:
                case JteLexer.CONTENT_STATE_TAG_NAME_BEGIN:
                case JteLexer.CONTENT_STATE_LAYOUT_NAME_BEGIN:
                    return true;
            }
        }

        return false;
    }
}
