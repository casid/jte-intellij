package org.jusecase.jte.intellij.language.format;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.xml.XmlFormattingPolicy;
import com.intellij.xml.template.formatter.AbstractXmlTemplateFormattingModelBuilder;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;
import org.jusecase.jte.intellij.language.psi.JtePsiFile;

public class JteFormattingModelBuilder extends AbstractXmlTemplateFormattingModelBuilder {

    @Override
    protected boolean isTemplateFile(PsiFile file) {
        return file instanceof JtePsiFile;
    }

    @Override
    public boolean isOuterLanguageElement(PsiElement element) {
        return element.getNode().getElementType() == JteTokenTypes.OUTER_ELEMENT_TYPE;
    }

    @Override
    public boolean isMarkupLanguageElement(PsiElement element) {
        return element.getNode().getElementType() == JteTokenTypes.HTML_CONTENT;
    }

    @Override
    protected Block createTemplateLanguageBlock(ASTNode node, CodeStyleSettings settings, XmlFormattingPolicy xmlFormattingPolicy, Indent indent, @Nullable Alignment alignment, @Nullable Wrap wrap) {
        return new JteFormattingBlock(this, node, wrap, alignment, settings, xmlFormattingPolicy, indent);
    }
}
