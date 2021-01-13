package org.jusecase.jte.intellij.language.format;


import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.templateLanguages.DataLanguageBlockWrapper;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlock;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlockFactory;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

import java.util.List;

public class JteBlock extends TemplateLanguageBlock {

    protected JteBlock(@NotNull TemplateLanguageBlockFactory blockFactory, @NotNull CodeStyleSettings settings, @NotNull ASTNode node, @Nullable List<DataLanguageBlockWrapper> foreignChildren) {
        super(blockFactory, settings, node, foreignChildren);
    }

    @Override
    public Indent getIndent() {
        IElementType elementType = myNode.getElementType();
        if (elementType == JteTokenTypes.PARAM || elementType == JteTokenTypes.IMPORT) {
            return Indent.getAbsoluteNoneIndent();
        }

        if (elementType == JteTokenTypes.WHITESPACE) {
            return Indent.getNoneIndent();
        }

        return null;
    }

    @Override
    public @NotNull ChildAttributes getChildAttributes(int newChildIndex) {
        return super.getChildAttributes(newChildIndex); // TODO
    }

    @Override
    protected IElementType getTemplateTextElementType() {
        return JteTokenTypes.HTML_CONTENT;
    }

    @Override
    public boolean isRequiredRange(TextRange range) {
        // seems our approach doesn't require us to insert any custom DataLanguageBlockFragmentWrapper blocks
        return false;
    }
}
