package org.jusecase.jte.intellij.language.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.psi.tree.IElementType;

import java.util.Objects;

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
        } else if (tokenType == JteTokenTypes.STATEMENT_BEGIN) {
            processStatement();
        } else if (tokenType == JteTokenTypes.IF) {
            processIf();
        } else if (tokenType == JteTokenTypes.ELSEIF) {
            processElseIf();
        } else if (tokenType == JteTokenTypes.ELSE) {
            processElse();
        } else if (tokenType == JteTokenTypes.ENDIF) {
            processEndIf();
        } else if (tokenType == JteTokenTypes.FOR) {
            processFor();
        } else if (tokenType == JteTokenTypes.ENDFOR) {
            processEndFor();
        } else if (tokenType == JteTokenTypes.TAG) {
            processTag();
        } else if (tokenType == JteTokenTypes.LAYOUT) {
            processLayout();
        } else if (tokenType == JteTokenTypes.ENDLAYOUT) {
            processEndLayout();
        } else if (tokenType == JteTokenTypes.DEFINE) {
            processDefine();
        } else if (tokenType == JteTokenTypes.ENDDEFINE) {
            processEndDefine();
        } else if (tokenType == JteTokenTypes.RENDER) {
            processRender();
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
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(JteTokenTypes.JAVA_INJECTION);
        }

        if (builder.getTokenType() == JteTokenTypes.OUTPUT_END) {
            Marker outputEndMarker = builder.mark();
            builder.advanceLexer();
            outputEndMarker.done(JteTokenTypes.OUTPUT_END);
        }

        outputMarker.done(JteTokenTypes.OUTPUT);
    }

    private void processStatement() {
        Marker statementMarker = builder.mark();

        Marker statementBeginMarker = builder.mark();
        builder.advanceLexer();
        statementBeginMarker.done(JteTokenTypes.STATEMENT_BEGIN);

        if (builder.getTokenType() == JteTokenTypes.JAVA_INJECTION) {
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(JteTokenTypes.JAVA_INJECTION);
        }

        if (builder.getTokenType() == JteTokenTypes.STATEMENT_END) {
            Marker outputEndMarker = builder.mark();
            builder.advanceLexer();
            outputEndMarker.done(JteTokenTypes.STATEMENT_END);
        }

        statementMarker.done(JteTokenTypes.STATEMENT);
    }

    private void processParam() {
        Marker paramMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == JteTokenTypes.WHITESPACE && !builder.eof()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == JteTokenTypes.JAVA_INJECTION) {
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(JteTokenTypes.JAVA_INJECTION);
        }

        paramMarker.done(JteTokenTypes.PARAM);
    }

    private void processImport() {
        Marker importMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == JteTokenTypes.WHITESPACE && !builder.eof()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == JteTokenTypes.JAVA_INJECTION) {
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(JteTokenTypes.JAVA_INJECTION);
        }

        importMarker.done(JteTokenTypes.IMPORT);
    }

    private void processIf() {
        Marker ifMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == JteTokenTypes.WHITESPACE && !builder.eof()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == JteTokenTypes.CONDITION_BEGIN) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == JteTokenTypes.JAVA_INJECTION) {
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(JteTokenTypes.JAVA_INJECTION);
        }

        if (builder.getTokenType() == JteTokenTypes.CONDITION_END) {
            builder.advanceLexer();
        }

        processEnd(JteTokenTypes.ENDIF);

        ifMarker.done(JteTokenTypes.IF);
    }

    private void processElseIf() {
        Marker elseIfMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == JteTokenTypes.WHITESPACE && !builder.eof()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == JteTokenTypes.CONDITION_BEGIN) {
            Marker conditionBeginMarker = builder.mark();
            builder.advanceLexer();
            conditionBeginMarker.done(JteTokenTypes.CONDITION_BEGIN);
        }

        if (builder.getTokenType() == JteTokenTypes.JAVA_INJECTION) {
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(JteTokenTypes.JAVA_INJECTION);
        }

        if (builder.getTokenType() == JteTokenTypes.CONDITION_END) {
            Marker conditionBeginMarker = builder.mark();
            builder.advanceLexer();
            conditionBeginMarker.done(JteTokenTypes.CONDITION_END);
        }

        elseIfMarker.done(JteTokenTypes.ELSEIF);
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

        while (builder.getTokenType() == JteTokenTypes.WHITESPACE && !builder.eof()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == JteTokenTypes.CONDITION_BEGIN) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == JteTokenTypes.JAVA_INJECTION) {
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(JteTokenTypes.JAVA_INJECTION);
        }

        if (builder.getTokenType() == JteTokenTypes.CONDITION_END) {
            builder.advanceLexer();
        }

        processEnd(JteTokenTypes.ENDFOR);

        forMarker.done(JteTokenTypes.FOR);
    }

    private void processEndFor() {
        Marker marker = builder.mark();
        builder.advanceLexer();
        marker.done(JteTokenTypes.ENDFOR);
    }

    private void processTag() {
        Marker tagMarker = builder.mark();
        builder.advanceLexer();

        processTagOrLayoutName(JteTokenTypes.TAG_NAME);

        if (builder.getTokenType() == JteTokenTypes.PARAMS_BEGIN) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == JteTokenTypes.JAVA_INJECTION) {
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(JteTokenTypes.JAVA_INJECTION);
        }

        if (builder.getTokenType() == JteTokenTypes.PARAMS_END) {
            builder.advanceLexer();
        }

        tagMarker.done(JteTokenTypes.TAG);
    }

    private void processTagOrLayoutName(IElementType type) {
        while (builder.getTokenType() == type || builder.getTokenType() == JteTokenTypes.NAME_SEPARATOR) {
            IElementType currentType = Objects.requireNonNull(builder.getTokenType());
            Marker marker = builder.mark();
            builder.advanceLexer();
            marker.done(currentType);
        }
    }

    private void processLayout() {
        Marker layoutMarker = builder.mark();
        builder.advanceLexer();

        processTagOrLayoutName(JteTokenTypes.LAYOUT_NAME);

        if (builder.getTokenType() == JteTokenTypes.PARAMS_BEGIN) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == JteTokenTypes.JAVA_INJECTION) {
            Marker kotlinBeginMarker = builder.mark();
            builder.advanceLexer();
            kotlinBeginMarker.done(JteTokenTypes.JAVA_INJECTION);
        }

        if (builder.getTokenType() == JteTokenTypes.PARAMS_END) {
            builder.advanceLexer();
        }

        processEnd(JteTokenTypes.ENDLAYOUT);

        layoutMarker.done(JteTokenTypes.LAYOUT);
    }

    private void processEndLayout() {
        Marker marker = builder.mark();
        builder.advanceLexer();
        marker.done(JteTokenTypes.ENDLAYOUT);
    }

    private void processDefine() {
        Marker defineMarker = builder.mark();
        builder.advanceLexer();

        if (builder.getTokenType() == JteTokenTypes.PARAMS_BEGIN) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == JteTokenTypes.DEFINE_NAME) {
            Marker defineNameMarker = builder.mark();
            builder.advanceLexer();
            defineNameMarker.done(JteTokenTypes.DEFINE_NAME);
        }

        if (builder.getTokenType() == JteTokenTypes.PARAMS_END) {
            builder.advanceLexer();
        }

        processEnd(JteTokenTypes.ENDDEFINE);

        defineMarker.done(JteTokenTypes.DEFINE);
    }

    private void processEndDefine() {
        Marker marker = builder.mark();
        builder.advanceLexer();
        marker.done(JteTokenTypes.ENDDEFINE);
    }

    private void processRender() {
        Marker renderMarker = builder.mark();
        builder.advanceLexer();

        if (builder.getTokenType() == JteTokenTypes.PARAMS_BEGIN) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == JteTokenTypes.RENDER_NAME) {
            Marker defineNameMarker = builder.mark();
            builder.advanceLexer();
            defineNameMarker.done(JteTokenTypes.RENDER_NAME);
        }

        if (builder.getTokenType() == JteTokenTypes.PARAMS_END) {
            builder.advanceLexer();
        }

        renderMarker.done(JteTokenTypes.RENDER);
    }

    private void processEnd(IElementType tokenType) {
        while (builder.getTokenType() != tokenType && !builder.eof()) {
            processBlock();
        }

        if (builder.getTokenType() == tokenType) {
            processBlock();
        }
    }
}
