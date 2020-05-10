package org.jusecase.kte.intellij.language.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.psi.tree.IElementType;

public class KteParsing {
    private final PsiBuilder builder;

    public KteParsing(PsiBuilder builder) {
        this.builder = builder;
    }

    public void parse() {
        Marker begin = builder.mark();

        while (!builder.eof()) {
            processBlock();
        }

        begin.done(KteTokenTypes.KOTLIN_CONTENT);
    }

    private void processBlock() {
        IElementType tokenType = builder.getTokenType();

        if (tokenType == KteTokenTypes.HTML_CONTENT) {
            builder.advanceLexer();
        } else if (tokenType == KteTokenTypes.IMPORT) {
            processImport();
        } else if (tokenType == KteTokenTypes.PARAM) {
            processParam();
        } else if (tokenType == KteTokenTypes.OUTPUT_BEGIN) {
            processOutput();
        } else if (tokenType == KteTokenTypes.STATEMENT_BEGIN) {
            processStatement();
        } else if (tokenType == KteTokenTypes.IF) {
            processIf();
        } else if (tokenType == KteTokenTypes.ELSEIF) {
            processElseIf();
        } else if (tokenType == KteTokenTypes.ELSE) {
            processElse();
        } else if (tokenType == KteTokenTypes.FOR) {
            processFor();
        } else if (tokenType == KteTokenTypes.TAG) {
            processTag();
        } else {
            builder.advanceLexer();
        }
    }

    private void processOutput() {
        Marker outputMarker = builder.mark();

        Marker outputBeginMarker = builder.mark();
        builder.advanceLexer();
        outputBeginMarker.done(KteTokenTypes.OUTPUT_BEGIN);

        if (builder.getTokenType() == KteTokenTypes.KOTLIN_INJECTION) {
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(KteTokenTypes.KOTLIN_INJECTION);
        }

        if (builder.getTokenType() == KteTokenTypes.OUTPUT_END) {
            Marker outputEndMarker = builder.mark();
            builder.advanceLexer();
            outputEndMarker.done(KteTokenTypes.OUTPUT_END);
        }

        outputMarker.done(KteTokenTypes.OUTPUT);
    }

    private void processStatement() {
        Marker statementMarker = builder.mark();

        Marker statementBeginMarker = builder.mark();
        builder.advanceLexer();
        statementBeginMarker.done(KteTokenTypes.STATEMENT_BEGIN);

        if (builder.getTokenType() == KteTokenTypes.KOTLIN_INJECTION) {
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(KteTokenTypes.KOTLIN_INJECTION);
        }

        if (builder.getTokenType() == KteTokenTypes.STATEMENT_END) {
            Marker outputEndMarker = builder.mark();
            builder.advanceLexer();
            outputEndMarker.done(KteTokenTypes.STATEMENT_END);
        }

        statementMarker.done(KteTokenTypes.STATEMENT);
    }

    private void processParam() {
        Marker paramMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == KteTokenTypes.WHITESPACE) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == KteTokenTypes.KOTLIN_INJECTION) {
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(KteTokenTypes.KOTLIN_INJECTION);
        }

        paramMarker.done(KteTokenTypes.PARAM);
    }

    private void processImport() {
        Marker importMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == KteTokenTypes.WHITESPACE && !builder.eof()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == KteTokenTypes.KOTLIN_INJECTION) {
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(KteTokenTypes.KOTLIN_INJECTION);
        }

        importMarker.done(KteTokenTypes.IMPORT);
    }

    private void processIf() {
        Marker ifMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == KteTokenTypes.WHITESPACE && !builder.eof()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == KteTokenTypes.CONDITION_BEGIN) {
            Marker conditionBeginMarker = builder.mark();
            builder.advanceLexer();
            conditionBeginMarker.done(KteTokenTypes.CONDITION_BEGIN);
        }

        if (builder.getTokenType() == KteTokenTypes.KOTLIN_INJECTION) {
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(KteTokenTypes.KOTLIN_INJECTION);
        }

        if (builder.getTokenType() == KteTokenTypes.CONDITION_END) {
            Marker conditionBeginMarker = builder.mark();
            builder.advanceLexer();
            conditionBeginMarker.done(KteTokenTypes.CONDITION_END);
        }

        do {
            processBlock();
        } while (builder.getTokenType() != KteTokenTypes.ENDIF && !builder.eof());
        processEndIf();

        ifMarker.done(KteTokenTypes.IF);
    }

    private void processElseIf() {
        Marker elseIfMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == KteTokenTypes.WHITESPACE && !builder.eof()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == KteTokenTypes.CONDITION_BEGIN) {
            Marker conditionBeginMarker = builder.mark();
            builder.advanceLexer();
            conditionBeginMarker.done(KteTokenTypes.CONDITION_BEGIN);
        }

        if (builder.getTokenType() == KteTokenTypes.KOTLIN_INJECTION) {
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(KteTokenTypes.KOTLIN_INJECTION);
        }

        if (builder.getTokenType() == KteTokenTypes.CONDITION_END) {
            Marker conditionBeginMarker = builder.mark();
            builder.advanceLexer();
            conditionBeginMarker.done(KteTokenTypes.CONDITION_END);
        }

        elseIfMarker.done(KteTokenTypes.ELSEIF);
    }

    private void processElse() {
        Marker marker = builder.mark();
        builder.advanceLexer();
        marker.done(KteTokenTypes.ELSE);
    }

    private void processEndIf() {
        Marker marker = builder.mark();
        builder.advanceLexer();
        marker.done(KteTokenTypes.ENDIF);
    }

    private void processFor() {
        Marker forMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == KteTokenTypes.WHITESPACE && !builder.eof()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == KteTokenTypes.CONDITION_BEGIN) {
            Marker conditionBeginMarker = builder.mark();
            builder.advanceLexer();
            conditionBeginMarker.done(KteTokenTypes.CONDITION_BEGIN);
        }

        if (builder.getTokenType() == KteTokenTypes.KOTLIN_INJECTION) {
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(KteTokenTypes.KOTLIN_INJECTION);
        }

        if (builder.getTokenType() == KteTokenTypes.CONDITION_END) {
            Marker conditionBeginMarker = builder.mark();
            builder.advanceLexer();
            conditionBeginMarker.done(KteTokenTypes.CONDITION_END);
        }

        do {
            processBlock();
        } while (builder.getTokenType() != KteTokenTypes.ENDFOR && !builder.eof());
        processEndFor();

        forMarker.done(KteTokenTypes.FOR);
    }

    private void processEndFor() {
        Marker marker = builder.mark();
        builder.advanceLexer();
        marker.done(KteTokenTypes.ENDFOR);
    }

    private void processTag() {
        Marker tagMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() != KteTokenTypes.TAG_NAME && !builder.eof()) {
            builder.advanceLexer();
        }

        Marker tagNameMarker = builder.mark();
        builder.advanceLexer();
        tagNameMarker.done(KteTokenTypes.TAG_NAME);

        if (builder.getTokenType() == KteTokenTypes.PARAMS_BEGIN) {
            Marker paramsBeginMarker = builder.mark();
            builder.advanceLexer();
            paramsBeginMarker.done(KteTokenTypes.PARAMS_BEGIN);
        }

        if (builder.getTokenType() == KteTokenTypes.KOTLIN_INJECTION) {
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(KteTokenTypes.KOTLIN_INJECTION);
        }

        if (builder.getTokenType() == KteTokenTypes.PARAMS_END) {
            Marker paramsBeginMarker = builder.mark();
            builder.advanceLexer();
            paramsBeginMarker.done(KteTokenTypes.PARAMS_END);
        }

        tagMarker.done(KteTokenTypes.TAG);
    }
}
