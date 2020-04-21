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
            } else if (tokenType == JteTokenTypes.ENDIF) {
                processEndIf();
            } else {
                builder.advanceLexer();
            }
        }

        begin.done(JteTokenTypes.JAVA_CONTENT);
    }

    private void processOutput() {
        Marker outputMarker = builder.mark();

        Marker outputBeginMarker = builder.mark();
        builder.advanceLexer();
        outputBeginMarker.done(JteTokenTypes.OUTPUT_BEGIN);

        if (builder.getTokenType() == JteTokenTypes.JAVA_CONTENT) {
            Marker javaBeginMarker = builder.mark();
            builder.advanceLexer();
            javaBeginMarker.done(JteTokenTypes.JAVA_OUTPUT);
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

        if (builder.getTokenType() == JteTokenTypes.JAVA_CONTENT) {
            Marker javaBeginMarker = builder.mark();
            builder.advanceLexer();
            javaBeginMarker.done(JteTokenTypes.JAVA_PARAM);
        }

        paramMarker.done(JteTokenTypes.PARAM);
    }

    private void processImport() {
        Marker importMarker = builder.mark();
        builder.advanceLexer();

        if (builder.getTokenType() == JteTokenTypes.WHITESPACE) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == JteTokenTypes.JAVA_CONTENT) {
            Marker javaBeginMarker = builder.mark();
            builder.advanceLexer();
            javaBeginMarker.done(JteTokenTypes.JAVA_IMPORT);
        }

        importMarker.done(JteTokenTypes.IMPORT);
    }

    private void processIf() {
        Marker paramMaker = builder.mark();
        paramMaker.done(JteTokenTypes.IF);
        builder.advanceLexer();
    }

    private void processEndIf() {
        Marker paramMaker = builder.mark();
        paramMaker.done(JteTokenTypes.ENDIF);
        builder.advanceLexer();
    }
}
