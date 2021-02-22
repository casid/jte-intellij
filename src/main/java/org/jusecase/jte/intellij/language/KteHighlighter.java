package org.jusecase.jte.intellij.language;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.parsing.KteLexer;
import org.jusecase.jte.intellij.language.parsing.KteTokenTypes;
import org.jusecase.jte.intellij.language.parsing.Lexer;

import java.util.Map;

public class KteHighlighter extends SyntaxHighlighterBase {

    private static final Map<IElementType, TextAttributesKey[]> MAPPING = HighlighterMapping.create(KteTokenTypes.INSTANCE);

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new KteLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        TextAttributesKey[] attributes = MAPPING.get(tokenType);
        if (attributes != null) {
            return attributes;
        }
        return TextAttributesKey.EMPTY_ARRAY;
    }
}
