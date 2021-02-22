package org.jusecase.jte.intellij.language.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.psi.tree.IElementType;

import java.util.Objects;

public class Parsing {
    private final PsiBuilder builder;
    private final TokenTypes tokens;

    public Parsing(PsiBuilder builder, TokenTypes tokens) {
        this.builder = builder;
        this.tokens = tokens;
    }

    public void parse() {
        Marker begin = builder.mark();

        while (!builder.eof()) {
            processBlock(null);
        }

        // For some reason this helps with code completion with @param when the template is empty
        Marker endContentMarker = builder.mark();
        endContentMarker.done(tokens.HTML_CONTENT());

        begin.done(tokens.JAVA_CONTENT());
    }

    private Marker processBlock(Marker currentBlock) {
        IElementType tokenType = builder.getTokenType();

        if (tokenType == tokens.HTML_CONTENT()) {
            builder.advanceLexer();
        } else if (tokenType == tokens.IMPORT()) {
            processImport();
        } else if (tokenType == tokens.PARAM()) {
            processParam();
        } else if (tokenType == tokens.OUTPUT_BEGIN()) {
            processOutput();
        } else if (tokenType == tokens.STATEMENT_BEGIN()) {
            processStatement();
        } else if (tokenType == tokens.IF()) {
            processIf();
        } else if (tokenType == tokens.ELSEIF()) {
            currentBlock = processElseIf(currentBlock);
        } else if (tokenType == tokens.ELSE()) {
            currentBlock = processElse(currentBlock);
        } else if (tokenType == tokens.ENDIF()) {
            processEndIf();
        } else if (tokenType == tokens.FOR()) {
            processFor();
        } else if (tokenType == tokens.ENDFOR()) {
            processEndFor();
        } else if (tokenType == tokens.TAG()) {
            processTag();
        } else if (tokenType == tokens.LAYOUT()) {
            processLayout();
        } else if (tokenType == tokens.CONTENT_BEGIN()) {
            processContent();
        } else if (tokenType == tokens.CONTENT_END()) {
            processEndContent();
        } else {
            builder.advanceLexer();
        }

        return currentBlock;
    }

    private void processOutput() {
        Marker outputMarker = builder.mark();

        Marker outputBeginMarker = builder.mark();
        builder.advanceLexer();
        outputBeginMarker.done(tokens.OUTPUT_BEGIN());

        while (builder.getTokenType() != tokens.OUTPUT_END() && !builder.eof()) {
            if (builder.getTokenType() == tokens.JAVA_INJECTION()) {
                Marker marker = builder.mark();
                builder.advanceLexer();
                marker.done(tokens.JAVA_INJECTION());
            } else if (builder.getTokenType() == tokens.CONTENT_BEGIN()) {
                processContent();
            } else {
                builder.advanceLexer();
            }
        }

        if (builder.getTokenType() == tokens.OUTPUT_END()) {
            Marker outputEndMarker = builder.mark();
            builder.advanceLexer();
            outputEndMarker.done(tokens.OUTPUT_END());
        }

        outputMarker.done(tokens.OUTPUT());
    }

    private void processStatement() {
        Marker statementMarker = builder.mark();

        Marker statementBeginMarker = builder.mark();
        builder.advanceLexer();
        statementBeginMarker.done(tokens.STATEMENT_BEGIN());

        while (builder.getTokenType() != tokens.STATEMENT_END() && !builder.eof()) {
            if (builder.getTokenType() == tokens.JAVA_INJECTION()) {
                Marker marker = builder.mark();
                builder.advanceLexer();
                marker.done(tokens.JAVA_INJECTION());
            } else if (builder.getTokenType() == tokens.CONTENT_BEGIN()) {
                processContent();
            } else {
                builder.advanceLexer();
            }
        }

        if (builder.getTokenType() == tokens.STATEMENT_END()) {
            Marker outputEndMarker = builder.mark();
            builder.advanceLexer();
            outputEndMarker.done(tokens.STATEMENT_END());
        }

        statementMarker.done(tokens.STATEMENT());
    }

    private void processParam() {
        Marker paramMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == tokens.WHITESPACE() && !builder.eof()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == tokens.JAVA_INJECTION()) {
            Marker javaBeginMarker = builder.mark();
            builder.advanceLexer();
            javaBeginMarker.done(tokens.JAVA_INJECTION());
        }

        while (builder.getTokenType() == tokens.WHITESPACE() && !builder.eof()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == tokens.EQUALS()) {
            builder.advanceLexer();

            while (builder.getTokenType() == tokens.WHITESPACE() && !builder.eof()) {
                builder.advanceLexer();
            }

            if (builder.getTokenType() == tokens.EXTRA_JAVA_INJECTION()) {
                Marker javaBeginMarker = builder.mark();
                builder.advanceLexer();
                javaBeginMarker.done(tokens.EXTRA_JAVA_INJECTION());
            } else if (builder.getTokenType() == tokens.CONTENT_BEGIN()) {
                Marker javaBeginMarker = builder.mark();
                processContent();
                javaBeginMarker.done(tokens.EXTRA_JAVA_INJECTION());
            }
        }

        paramMarker.done(tokens.PARAM());
    }

    private void processImport() {
        Marker importMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == tokens.WHITESPACE() && !builder.eof()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == tokens.JAVA_INJECTION()) {
            Marker javaBeginMarker = builder.mark();
            builder.advanceLexer();
            javaBeginMarker.done(tokens.JAVA_INJECTION());
        }

        importMarker.done(tokens.IMPORT());
    }

    private void processIf() {
        Marker ifMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == tokens.WHITESPACE() && !builder.eof()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == tokens.CONDITION_BEGIN()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == tokens.JAVA_INJECTION()) {
            Marker javaBeginMarker = builder.mark();
            builder.advanceLexer();
            javaBeginMarker.done(tokens.JAVA_INJECTION());
        }

        if (builder.getTokenType() == tokens.CONDITION_END()) {
            builder.advanceLexer();
        }

        processEnd(tokens.ENDIF());

        ifMarker.done(tokens.IF());
    }

    private Marker processElseIf(Marker currentBlock) {
        if (currentBlock != null) {
            currentBlock.done(tokens.BLOCK());
        }

        Marker elseIfMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == tokens.WHITESPACE() && !builder.eof()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == tokens.CONDITION_BEGIN()) {
            Marker conditionBeginMarker = builder.mark();
            builder.advanceLexer();
            conditionBeginMarker.done(tokens.CONDITION_BEGIN());
        }

        if (builder.getTokenType() == tokens.JAVA_INJECTION()) {
            Marker javaBeginMarker = builder.mark();
            builder.advanceLexer();
            javaBeginMarker.done(tokens.JAVA_INJECTION());
        }

        if (builder.getTokenType() == tokens.CONDITION_END()) {
            Marker conditionBeginMarker = builder.mark();
            builder.advanceLexer();
            conditionBeginMarker.done(tokens.CONDITION_END());
        }

        elseIfMarker.done(tokens.ELSEIF());

        if (currentBlock == null) {
            return null;
        }

        return builder.mark();
    }

    private Marker processElse(Marker currentBlock) {
        if (currentBlock != null) {
            currentBlock.done(tokens.BLOCK());
        }

        Marker marker = builder.mark();
        builder.advanceLexer();
        marker.done(tokens.ELSE());

        if (currentBlock == null) {
            return null;
        }

        return builder.mark();
    }

    private void processEndIf() {
        Marker marker = builder.mark();
        builder.advanceLexer();
        marker.done(tokens.ENDIF());
    }

    private void processFor() {
        Marker forMarker = builder.mark();
        builder.advanceLexer();

        while (builder.getTokenType() == tokens.WHITESPACE() && !builder.eof()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == tokens.CONDITION_BEGIN()) {
            builder.advanceLexer();
        }

        if (builder.getTokenType() == tokens.JAVA_INJECTION()) {
            Marker javaBeginMarker = builder.mark();
            builder.advanceLexer();
            javaBeginMarker.done(tokens.JAVA_INJECTION());
        }

        if (builder.getTokenType() == tokens.CONDITION_END()) {
            builder.advanceLexer();
        }

        processEnd(tokens.ENDFOR());

        forMarker.done(tokens.FOR());
    }

    private void processEndFor() {
        Marker marker = builder.mark();
        builder.advanceLexer();
        marker.done(tokens.ENDFOR());
    }

    private void processTag() {
        Marker tagMarker = builder.mark();
        builder.advanceLexer();

        processTagOrLayoutName();
        processTagOrLayoutParams();

        tagMarker.done(tokens.TAG());
    }

    private void processTagOrLayoutName() {
        while (builder.getTokenType() == tokens.TAG_NAME() || builder.getTokenType() == tokens.NAME_SEPARATOR()) {
            IElementType currentType = Objects.requireNonNull(builder.getTokenType());
            Marker marker = builder.mark();
            builder.advanceLexer();
            marker.done(currentType);
        }
    }

    private void processTagOrLayoutParams() {
        if (builder.getTokenType() == tokens.PARAMS_BEGIN()) {
            builder.advanceLexer();
        } else {
            return;
        }

        while (builder.getTokenType() != tokens.PARAMS_END() && !builder.eof()) {
            if (builder.getTokenType() == tokens.JAVA_INJECTION()) {
                Marker marker = builder.mark();
                builder.advanceLexer();
                marker.done(tokens.JAVA_INJECTION());
            } else if (builder.getTokenType() == tokens.PARAM_NAME()) {
                Marker marker = builder.mark();
                builder.advanceLexer();
                marker.done(tokens.PARAM_NAME());
            } else if (builder.getTokenType() == tokens.COMMA()) {
                Marker marker = builder.mark();
                builder.advanceLexer();
                marker.done(tokens.COMMA());
            } else if (builder.getTokenType() == tokens.CONTENT_BEGIN()) {
                processContent();
            } else {
                builder.advanceLexer();
            }
        }

        if (builder.getTokenType() == tokens.PARAMS_END()) {
            builder.advanceLexer();
        }
    }

    private void processLayout() {
        Marker layoutMarker = builder.mark();
        builder.advanceLexer();

        processTagOrLayoutName();
        processTagOrLayoutParams();

        layoutMarker.done(tokens.LAYOUT());
    }

    private void processContent() {
        Marker contentMarker = builder.mark();
        builder.advanceLexer();

        processEnd(tokens.CONTENT_END());

        contentMarker.done(tokens.CONTENT_BEGIN());
    }

    private void processEndContent() {
        Marker marker = builder.mark();
        builder.advanceLexer();
        marker.done(tokens.CONTENT_END());
    }

    private void processEnd(IElementType tokenType) {
        Marker blockMarker = builder.mark();

        while (builder.getTokenType() != tokenType && !builder.eof()) {
            blockMarker = processBlock(blockMarker);
        }

        blockMarker.done(tokens.BLOCK());

        if (builder.getTokenType() == tokenType) {
            processBlock(null);
        }
    }
}
