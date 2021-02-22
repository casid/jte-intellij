package org.jusecase.jte.intellij.language.format;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.xml.XmlFormattingPolicy;
import com.intellij.xml.template.formatter.AbstractXmlTemplateFormattingModelBuilder;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.parsing.TokenTypes;

public abstract class FormattingModelBuilderBase extends AbstractXmlTemplateFormattingModelBuilder {

    private final TokenTypes tokenTypes;

    protected FormattingModelBuilderBase(TokenTypes tokenTypes) {
        this.tokenTypes = tokenTypes;
    }

    @Override
    public boolean isOuterLanguageElement(PsiElement element) {
        return element.getNode().getElementType() == tokenTypes.OUTER_ELEMENT_TYPE();
    }

    @Override
    public boolean isMarkupLanguageElement(PsiElement element) {
        return element.getNode().getElementType() == tokenTypes.HTML_CONTENT();
    }

    @Override
    protected Block createTemplateLanguageBlock(ASTNode node, CodeStyleSettings settings, XmlFormattingPolicy xmlFormattingPolicy, Indent indent, @Nullable Alignment alignment, @Nullable Wrap wrap) {
        return new JteFormattingBlock(this, node, wrap, alignment, settings, xmlFormattingPolicy, indent, tokenTypes);
    }
}
