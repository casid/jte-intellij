package org.jusecase.jte.intellij.language.format;


import com.intellij.formatting.Indent;
import com.intellij.formatting.templateLanguages.BlockWithParent;
import com.intellij.formatting.templateLanguages.DataLanguageBlockWrapper;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlock;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlockFactory;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.xml.SyntheticBlock;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

import java.util.List;

public class JteFormattingBlock extends TemplateLanguageBlock {

    protected JteFormattingBlock(@NotNull TemplateLanguageBlockFactory blockFactory, @NotNull CodeStyleSettings settings, @NotNull ASTNode node, @Nullable List<DataLanguageBlockWrapper> foreignChildren) {
        super(blockFactory, settings, node, foreignChildren);
    }

    @Override
    public Indent getIndent() {
        IElementType elementType = myNode.getElementType();
        if (elementType == JteTokenTypes.PARAM || elementType == JteTokenTypes.IMPORT) {
            return Indent.getAbsoluteNoneIndent();
        }

        if (elementType == JteTokenTypes.BLOCK) {
            return Indent.getNormalIndent();
        }

        DataLanguageBlockWrapper parent = getRealBlockParent();
        if (parent != null) {
            return parent.getChildAttributes(1).getChildIndent();
        }

        return Indent.getNoneIndent();
    }

    @Override
    @Nullable
    protected Indent getChildIndent() {
        IElementType elementType = myNode.getElementType();

        if (elementType == JteTokenTypes.IF || elementType == JteTokenTypes.FOR || elementType == JteTokenTypes.CONTENT_BEGIN) {
            return Indent.getNormalIndent();
        }

        return Indent.getNoneIndent();
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

    @Nullable
    private DataLanguageBlockWrapper getRealBlockParent() {
        // if we can follow the chain of synthetic parent blocks, and if we end up
        // at a real DataLanguage block (i.e. the synthetic blocks didn't lead to an HbBlock),
        // we're a child of a templated language node and need an indent
        BlockWithParent parent = getParent();
        while (parent instanceof DataLanguageBlockWrapper && ((DataLanguageBlockWrapper) parent).getOriginal() instanceof SyntheticBlock) {
            parent = parent.getParent();
        }

        if (parent instanceof DataLanguageBlockWrapper) {
            return (DataLanguageBlockWrapper)parent;
        }

        return null;
    }
}
