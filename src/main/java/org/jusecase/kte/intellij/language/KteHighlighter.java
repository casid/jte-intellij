package org.jusecase.kte.intellij.language;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jusecase.kte.intellij.language.parsing.KteLexer;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

import java.util.HashMap;
import java.util.Map;

public class KteHighlighter extends SyntaxHighlighterBase {

    private static final TextAttributesKey[] KEYWORD = {TextAttributesKey.createTextAttributesKey(
            "KOTLIN_TEMPLATE_ENGINE.KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
    )};

    private static final TextAttributesKey[] COMMENT = {TextAttributesKey.createTextAttributesKey(
            "KOTLIN_TEMPLATE_ENGINE.COMMENT",
            DefaultLanguageHighlighterColors.BLOCK_COMMENT
    )};

    private static final Map<IElementType, TextAttributesKey[]> MAPPING = new HashMap<>();
    static {
        MAPPING.put(KteTokenTypes.IMPORT, KEYWORD);
        MAPPING.put(KteTokenTypes.PARAM, KEYWORD);
        MAPPING.put(KteTokenTypes.IF, KEYWORD);
        MAPPING.put(KteTokenTypes.ELSE, KEYWORD);
        MAPPING.put(KteTokenTypes.ELSEIF, KEYWORD);
        MAPPING.put(KteTokenTypes.ENDIF, KEYWORD);
        MAPPING.put(KteTokenTypes.FOR, KEYWORD);
        MAPPING.put(KteTokenTypes.ENDFOR, KEYWORD);
        MAPPING.put(KteTokenTypes.TAG, KEYWORD);

        MAPPING.put(KteTokenTypes.COMMENT, COMMENT);

        MAPPING.put(KteTokenTypes.OUTPUT_BEGIN, KEYWORD);
        MAPPING.put(KteTokenTypes.OUTPUT_END, KEYWORD);
        MAPPING.put(KteTokenTypes.STATEMENT_BEGIN, KEYWORD);
        MAPPING.put(KteTokenTypes.STATEMENT_END, KEYWORD);
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new KteLexer();
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
