package org.jusecase.jte.intellij.language.format;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.xml.XmlFormattingPolicy;
import com.intellij.xml.template.formatter.AbstractXmlTemplateFormattingModelBuilder;
import com.intellij.xml.template.formatter.TemplateLanguageBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class JteFormattingBlock extends TemplateLanguageBlock {
    protected JteFormattingBlock(AbstractXmlTemplateFormattingModelBuilder builder, @NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, CodeStyleSettings settings, XmlFormattingPolicy xmlFormattingPolicy, @Nullable Indent indent) {
        super(builder, node, wrap, alignment, settings, xmlFormattingPolicy, indent);
    }

    @Override
    protected @NotNull Indent getChildIndent(@NotNull ASTNode node) {
        if (myNode.getElementType() == JteTokenTypes.BLOCK) {
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
}
