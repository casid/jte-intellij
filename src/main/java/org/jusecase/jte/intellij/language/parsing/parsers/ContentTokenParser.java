package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class ContentTokenParser extends AbstractTokenParser {
    private static final String[] KEYWORDS = {
            "${",
            "$unsafe{",
            "!{",
            "<%--",
            "@if",
            "@else",
            "@elseif",
            "@endif",
            "@for",
            "@endfor",
            "@raw",
            "@endraw",
            "@import",
            "@param",
            "@template"
    };

    private boolean insideString;

    public ContentTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        insideString = false;

        if (shouldSkipWhitespaces(position) && isWhitespace(position)) {
            return skipWhitespaces(position);
        }

        if (lexer.isInJavaEndState()) {
            lexer.setCurrentState(Lexer.CONTENT_STATE_HTML);
        }

        if (isBeginOfJteKeyword(position)) {
            return false;
        }

        int currentState = lexer.getCurrentState();
        int start = position;
        position++;

        while (position < myEndOffset && !isBeginOfJteKeyword(position)) {
            if (lexer.isInHtmlState() && !lexer.isImportOrParamIgnored() && !isWhitespace(position)) {
                lexer.setImportOrParamIgnored(true);
            }
            position++;
        }

        if (currentState == Lexer.CONTENT_STATE_TEMPLATE_NAME_BEGIN) {
            myTokenInfo.updateData(start, position, lexer.tokens.TEMPLATE_NAME());
        } else if (lexer.isInHtmlState()) {
            if (isBlank(start, position)) {
                myTokenInfo.updateData(start, position, lexer.tokens.WHITESPACE());
            } else {
                myTokenInfo.updateData(start, position, lexer.tokens.HTML_CONTENT());
            }
        } else if (currentState == Lexer.CONTENT_STATE_PARAM_DEFAULT_VALUE) {
            myTokenInfo.updateData(start, position, lexer.tokens.EXTRA_JAVA_INJECTION());
        } else if (currentState == Lexer.CONTENT_STATE_PARAM_NAME) {
            myTokenInfo.updateData(start, position, lexer.tokens.PARAM_NAME());
        } else if (currentState == Lexer.CONTENT_STATE_RAW) {
            myTokenInfo.updateData(start, position, lexer.tokens.HTML_CONTENT());
        } else {
            myTokenInfo.updateData(start, position, lexer.tokens.JAVA_INJECTION());
        }
        return true;
    }

    private boolean shouldSkipWhitespaces(int position) {
        int state = lexer.getCurrentState();
        if (state == Lexer.CONTENT_STATE_PARAM_DEFAULT_VALUE && isBeginOf(position, '\n')) {
            return false;
        }

        return
                state == Lexer.CONTENT_STATE_IMPORT_BEGIN ||
                        state == Lexer.CONTENT_STATE_PARAM_BEGIN ||
                        state == Lexer.CONTENT_STATE_FOR_BEGIN ||
                        state == Lexer.CONTENT_STATE_ELSEIF_BEGIN ||
                        state == Lexer.CONTENT_STATE_IF_BEGIN ||
                        state == Lexer.CONTENT_STATE_PARAM_DEFAULT_VALUE ||
                        state == Lexer.CONTENT_STATE_PARAM_NAME ||
                        state == Lexer.CONTENT_STATE_TEMPLATE_PARAMS;
    }

    private boolean isBeginOfJteKeyword(int position) {
        if (lexer.getCurrentState() == Lexer.CONTENT_STATE_RAW && !isBeginOf(position, "@endraw")) {
            return false;
        }

        if (insideString) {
            if (isBeginOf(position, '\"') && myBuffer.charAt(position - 1) != '\\') {
                insideString = false;
            }
            return false;
        }

        for (String keyword : KEYWORDS) {
            if (("@import".equals(keyword) || "@param".equals(keyword)) && lexer.isImportOrParamIgnored()) {
                continue;
            }

            if (isBeginOf(position, keyword)) {
                return true;
            }
        }

        if (isBeginOf(position, '\n')) {
            switch (lexer.getCurrentState()) {
                case Lexer.CONTENT_STATE_IMPORT_BEGIN:
                    lexer.setCurrentState(Lexer.CONTENT_STATE_IMPORT_END);
                    return true;
                case Lexer.CONTENT_STATE_PARAM_BEGIN:
                case Lexer.CONTENT_STATE_PARAM_DEFAULT_VALUE:
                    lexer.setCurrentState(Lexer.CONTENT_STATE_PARAM_END);
                    return true;
            }
        }

        if (lexer.getCurrentState() == Lexer.CONTENT_STATE_PARAM_BEGIN && lexer.isExtraParamInjectionRequired()) {
            for (int index = position; index < myEndOffset; ++index) {
                if (myBuffer.charAt(index) == '=') {
                    lexer.setCurrentState(Lexer.CONTENT_STATE_PARAM_DEFAULT_VALUE);
                    return true;
                }

                if (!isWhitespace(index)) {
                    return false;
                }
            }
        }

        if (lexer.getCurrentState() == Lexer.CONTENT_STATE_PARAM_DEFAULT_VALUE && isBeginOf(position, '=')) {
            return true;
        }

        if (isBeginOf(position, "@`") && !lexer.isInHtmlState()) {
            return true;
        }

        if (isBeginOf(position, "`") && lexer.isInHtmlState() && lexer.isInContentBlock()) {
            return true;
        }

        if (lexer.getCurrentState() == Lexer.CONTENT_STATE_TEMPLATE_PARAMS) {
            for (int index = position; index < myEndOffset; ++index) {
                if (isBeginOf(index, "@`")) {
                    break;
                }

                char currentChar = myBuffer.charAt(index);
                if (currentChar == ')' && (isBeginOf(position, '\n') || isBeginOf(position, '\r'))) {
                    return true;
                }

                if ((currentChar == ',' && index > position) || currentChar == ')' || currentChar == '\"') {
                    break;
                }

                if (currentChar == '=' && index + 1 < myEndOffset && myBuffer.charAt(index + 1) != '=' && myBuffer.charAt(index - 1) != '=' && myBuffer.charAt(index - 1) != '!') {
                    lexer.setCurrentState(Lexer.CONTENT_STATE_PARAM_NAME);
                    lexer.setCurrentCount(Lexer.CONTENT_COUNT_PARAM_NAME_TEMPLATE);
                    return isBeginOf(position, ',');
                }
            }
        }

        if (lexer.getCurrentState() == Lexer.CONTENT_STATE_PARAM_NAME) {
            if (isBeginOf(position, '=') || isWhitespace(position) || isBeginOf(position, ',')) {
                return true;
            }

            if (lexer.getCurrentCount() == Lexer.CONTENT_COUNT_PARAM_NAME_TEMPLATE_DONE) {
                lexer.setCurrentState(Lexer.CONTENT_STATE_TEMPLATE_PARAMS);
            }
        }

        if (isBeginOf(position, '{')) {
            switch (lexer.getCurrentState()) {
                case Lexer.CONTENT_STATE_OUTPUT_BEGIN:
                case Lexer.CONTENT_STATE_STATEMENT_BEGIN:
                    lexer.incrementCurrentCount();
                    return false;
            }
        }

        if (isBeginOf(position, '}')) {
            switch (lexer.getCurrentState()) {
                case Lexer.CONTENT_STATE_OUTPUT_BEGIN:
                case Lexer.CONTENT_STATE_STATEMENT_BEGIN:
                    int count = lexer.getCurrentCount();
                    lexer.decrementCurrentCount();
                    return count <= 0;
            }
        }

        if (isBeginOf(position, '(')) {
            switch (lexer.getCurrentState()) {
                case Lexer.CONTENT_STATE_IF_BEGIN:
                case Lexer.CONTENT_STATE_ELSEIF_BEGIN:
                case Lexer.CONTENT_STATE_FOR_BEGIN:
                case Lexer.CONTENT_STATE_TEMPLATE_NAME_BEGIN:
                    return true;
                case Lexer.CONTENT_STATE_IF_CONDITION:
                case Lexer.CONTENT_STATE_ELSEIF_CONDITION:
                case Lexer.CONTENT_STATE_FOR_CONDITION:
                case Lexer.CONTENT_STATE_TEMPLATE_PARAMS:
                    lexer.incrementCurrentCount();
                    return false;
            }
        }

        if (isBeginOf(position, ')')) {
            switch (lexer.getCurrentState()) {
                case Lexer.CONTENT_STATE_IF_CONDITION:
                case Lexer.CONTENT_STATE_ELSEIF_CONDITION:
                case Lexer.CONTENT_STATE_FOR_CONDITION:
                case Lexer.CONTENT_STATE_TEMPLATE_PARAMS:
                    int count = lexer.getCurrentCount();
                    lexer.decrementCurrentCount();
                    return count <= 0;
            }
        }

        if (isBeginOf(position, '.')) {
            switch (lexer.getCurrentState()) {
                case Lexer.CONTENT_STATE_TEMPLATE_BEGIN:
                case Lexer.CONTENT_STATE_TEMPLATE_NAME_BEGIN:
                    return true;
            }
        }

        if (!lexer.isInHtmlState() && isBeginOf(position, '\"')) {
            insideString = true;
            return false;
        }

        return false;
    }
}
