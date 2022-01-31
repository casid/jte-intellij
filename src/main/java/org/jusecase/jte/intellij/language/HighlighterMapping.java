package org.jusecase.jte.intellij.language;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.tree.IElementType;
import org.jusecase.jte.intellij.language.parsing.TokenTypes;

import java.util.HashMap;
import java.util.Map;

public class HighlighterMapping {
    private static final TextAttributesKey[] KEYWORD = {TextAttributesKey.createTextAttributesKey(
            "JAVA_TEMPLATE_ENGINE.KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
    )};

    private static final TextAttributesKey[] COMMENT = {TextAttributesKey.createTextAttributesKey(
            "JAVA_TEMPLATE_ENGINE.COMMENT",
            DefaultLanguageHighlighterColors.BLOCK_COMMENT
    )};

    private static final TextAttributesKey[] STRING = {TextAttributesKey.createTextAttributesKey(
            "JAVA_TEMPLATE_ENGINE.STRING",
            DefaultLanguageHighlighterColors.STRING
    )};

    public static Map<IElementType, TextAttributesKey[]> create(TokenTypes tokens) {
        Map<IElementType, TextAttributesKey[]> mapping = new HashMap<>();

        mapping.put(tokens.IMPORT(), KEYWORD);
        mapping.put(tokens.PARAM(), KEYWORD);
        mapping.put(tokens.IF(), KEYWORD);
        mapping.put(tokens.ELSE(), KEYWORD);
        mapping.put(tokens.ELSEIF(), KEYWORD);
        mapping.put(tokens.ENDIF(), KEYWORD);
        mapping.put(tokens.FOR(), KEYWORD);
        mapping.put(tokens.ENDFOR(), KEYWORD);
        mapping.put(tokens.RAW(), KEYWORD);
        mapping.put(tokens.ENDRAW(), KEYWORD);
        mapping.put(tokens.TEMPLATE(), KEYWORD);
        mapping.put(tokens.CONTENT_BEGIN(), STRING);
        mapping.put(tokens.CONTENT_END(), STRING);

        mapping.put(tokens.COMMENT(), COMMENT);

        mapping.put(tokens.OUTPUT_BEGIN(), KEYWORD);
        mapping.put(tokens.OUTPUT_END(), KEYWORD);
        mapping.put(tokens.STATEMENT_BEGIN(), KEYWORD);
        mapping.put(tokens.STATEMENT_END(), KEYWORD);

        return mapping;
    }
}
