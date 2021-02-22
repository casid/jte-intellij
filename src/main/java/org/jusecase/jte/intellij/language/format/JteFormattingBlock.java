package org.jusecase.jte.intellij.language.format;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.formatter.xml.XmlFormattingPolicy;
import com.intellij.psi.tree.IElementType;
import com.intellij.xml.template.formatter.AbstractXmlTemplateFormattingModelBuilder;
import com.intellij.xml.template.formatter.TemplateLanguageBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.parsing.TokenTypes;

import java.util.List;

public class JteFormattingBlock extends TemplateLanguageBlock {
    private final TokenTypes tokenTypes;

    protected JteFormattingBlock(AbstractXmlTemplateFormattingModelBuilder builder, @NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, CodeStyleSettings settings, XmlFormattingPolicy xmlFormattingPolicy, @Nullable Indent indent, TokenTypes tokenTypes) {
        super(builder, node, wrap, alignment, settings, xmlFormattingPolicy, indent);
        this.tokenTypes = tokenTypes;
    }

    @Override
    protected @NotNull Indent getChildIndent(@NotNull ASTNode node) {
        if (myNode.getElementType() == tokenTypes.BLOCK()) {
            return Indent.getNormalIndent();
        }
        return Indent.getNoneIndent();
    }

    @Override
    protected Spacing getSpacing(TemplateLanguageBlock adjacentBlock) {
        return null;
    }

    @Override
    public @Nullable Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return null;
    }

    @Override
    public @NotNull ChildAttributes getChildAttributes(int newChildIndex) {
        AbstractBlock previousBlock = getPreviousBlock(newChildIndex);

        if (previousBlock != null) {
            IElementType elementType = previousBlock.getNode().getElementType();
            if (elementType == tokenTypes.IMPORT() || elementType == tokenTypes.PARAM()) {
                return new ChildAttributes(Indent.getNoneIndent(), null);
            }
        }

        return super.getChildAttributes(newChildIndex);
    }

    private AbstractBlock getPreviousBlock(int newChildIndex) {
        List<Block> subBlocks = getSubBlocks();
        if (newChildIndex > subBlocks.size()) {
            return null;
        }

        Block previousBlock = subBlocks.get(newChildIndex - 1);
        if (!(previousBlock instanceof AbstractBlock)) {
            return null;
        }

        return (AbstractBlock)previousBlock;
    }
}
