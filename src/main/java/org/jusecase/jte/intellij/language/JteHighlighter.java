package org.jusecase.jte.intellij.language;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.parsing.JteLexer;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

import java.util.HashMap;
import java.util.Map;

public class JteHighlighter extends SyntaxHighlighterBase {

    private static final TextAttributesKey[] KEYWORD = {TextAttributesKey.createTextAttributesKey(
            "JAVA_TEMPLATE_ENGINE.KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
    )};

    private static final TextAttributesKey[] COMMENT = {TextAttributesKey.createTextAttributesKey(
            "JAVA_TEMPLATE_ENGINE.COMMENT",
            DefaultLanguageHighlighterColors.BLOCK_COMMENT
    )};

    private static final Map<IElementType, TextAttributesKey[]> MAPPING = new HashMap<>();
    static {
        MAPPING.put(JteTokenTypes.IMPORT, KEYWORD);
        MAPPING.put(JteTokenTypes.PARAM, KEYWORD);
        MAPPING.put(JteTokenTypes.IF, KEYWORD);
        MAPPING.put(JteTokenTypes.ELSE, KEYWORD);
        MAPPING.put(JteTokenTypes.ELSEIF, KEYWORD);
        MAPPING.put(JteTokenTypes.ENDIF, KEYWORD);
        MAPPING.put(JteTokenTypes.FOR, KEYWORD);
        MAPPING.put(JteTokenTypes.ENDFOR, KEYWORD);
        MAPPING.put(JteTokenTypes.TAG, KEYWORD);

        MAPPING.put(JteTokenTypes.COMMENT, COMMENT);

        MAPPING.put(JteTokenTypes.OUTPUT_BEGIN, KEYWORD);
        MAPPING.put(JteTokenTypes.OUTPUT_END, KEYWORD);
        MAPPING.put(JteTokenTypes.STATEMENT_BEGIN, KEYWORD);
        MAPPING.put(JteTokenTypes.STATEMENT_END, KEYWORD);
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new JteLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        TextAttributesKey[] attributes = MAPPING.get(tokenType);
        if (attributes != null) {
            return attributes;
        }
        return TextAttributesKey.EMPTY_ARRAY;
    }
}
