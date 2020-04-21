package org.jusecase.jte.intellij.language.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.psi.tree.IElementType;

public class JteParsing {
    private final PsiBuilder builder;

    public JteParsing(PsiBuilder builder) {
        this.builder = builder;
    }

    public void parse() {
        Marker begin = builder.mark();

        while (!builder.eof()) {
            processBlock();
        }

        begin.done(JteTokenTypes.JAVA_CONTENT);
    }

    private void processBlock() {
        IElementType tokenType = builder.getTokenType();

        if (tokenType == JteTokenTypes.HTML_CONTENT) {
            builder.advanceLexer();
        } else if (tokenType == JteTokenTypes.IMPORT) {
            processImport();
        } else if (tokenType == JteTokenTypes.PARAM) {
            processParam();
        } else if (tokenType == JteTokenTypes.OUTPUT_BEGIN) {
            processOutput();
        } else if (tokenType == JteTokenTypes.IF) {
            processIf();
        } else if (tokenType == JteTokenTypes.ELSE) {
            processElse();
        } else if (tokenType == JteTokenTypes.FOR) {
            processFor();
        } else {
            builder.advanceLexer();
        }
    }

    private void processOutput() {
        Marker outputMarker = builder.mark();

        Marker outputBeginMarker = builder.mark();
        builder.advanceLexer();
        outputBeginMarker.done(JteTokenTypes.OUTPUT_BEGIN);

        if (builder.getTokenType() == JteTokenTypes.JAVA_INJECTION) {
            Marker javaBeginMarker = builder.mark();
            builder.advanceLexer();
            javaBeginMarker.done(JteTokenTypes.JAVA_INJECTION);
        }

        if (builder.getTokenType() == JteTokenTypes.OUTPUT_END) {
            Marker outputEndMarker = builder.mark();
            builder.advanceLexer();
            outputEndMarker.done(JteTokenTypes.OUTPUT_END);
        }

        outputMarker.done(JteTokenTypes.OUTPUT);
    }

    private void processParam() {
        Marker paramMarker = builder.mark();
        builder.advanceLexer();

        if (builder.getTokenType() == JteTokenTypes.JAVA_INJECTION) {
            Marker javaBeginMarker = builder.mark();
            builder.advanceLexer();
            javaBeginMarker.done(JteTokenTypes.JAVA_INJECTION);
        }

        paramMarker.done(JteTokenTypes.PARAM);
    }

    private void processImport() {
        Marker importMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == JteTokenTypes.WHITESPACE) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == JteTokenTypes.JAVA_INJECTION) {
            Marker javaBeginMarker = builder.mark();
            builder.advanceLexer();
            javaBeginMarker.done(JteTokenTypes.JAVA_INJECTION);
        }

        importMarker.done(JteTokenTypes.IMPORT);
    }

    private void processIf() {
        Marker ifMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == JteTokenTypes.WHITESPACE) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == JteTokenTypes.CONDITION_BEGIN) {
            Marker conditionBeginMarker = builder.mark();
            builder.advanceLexer();
            conditionBeginMarker.done(JteTokenTypes.CONDITION_BEGIN);
        }

        if (builder.getTokenType() == JteTokenTypes.JAVA_INJECTION) {
            Marker javaBeginMarker = builder.mark();
            builder.advanceLexer();
            javaBeginMarker.done(JteTokenTypes.JAVA_INJECTION);
        }

        if (builder.getTokenType() == JteTokenTypes.CONDITION_END) {
            Marker conditionBeginMarker = builder.mark();
            builder.advanceLexer();
            conditionBeginMarker.done(JteTokenTypes.CONDITION_END);
        }

        do {
            processBlock();
        } while (builder.getTokenType() != JteTokenTypes.ENDIF && !builder.eof());
        processEndIf();

        ifMarker.done(JteTokenTypes.IF);
    }

    private void processElse() {
        Marker marker = builder.mark();
        builder.advanceLexer();
        marker.done(JteTokenTypes.ELSE);
    }

    private void processEndIf() {
        Marker marker = builder.mark();
        builder.advanceLexer();
        marker.done(JteTokenTypes.ENDIF);
    }

    private void processFor() {
        Marker forMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == JteTokenTypes.WHITESPACE) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == JteTokenTypes.CONDITION_BEGIN) {
            Marker conditionBeginMarker = builder.mark();
            builder.advanceLexer();
            conditionBeginMarker.done(JteTokenTypes.CONDITION_BEGIN);
        }

        if (builder.getTokenType() == JteTokenTypes.JAVA_INJECTION) {
            Marker javaBeginMarker = builder.mark();
            builder.advanceLexer();
            javaBeginMarker.done(JteTokenTypes.JAVA_INJECTION);
        }

        if (builder.getTokenType() == JteTokenTypes.CONDITION_END) {
            Marker conditionBeginMarker = builder.mark();
            builder.advanceLexer();
            conditionBeginMarker.done(JteTokenTypes.CONDITION_END);
        }

        do {
            processBlock();
        } while (builder.getTokenType() != JteTokenTypes.ENDFOR && !builder.eof());
        processEndFor();

        forMarker.done(JteTokenTypes.FOR);
    }

    private void processEndFor() {
        Marker marker = builder.mark();
        builder.advanceLexer();
        marker.done(JteTokenTypes.ENDFOR);
    }
}
