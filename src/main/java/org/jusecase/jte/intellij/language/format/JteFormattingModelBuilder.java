package org.jusecase.jte.intellij.language.format;

import com.intellij.formatting.*;
import com.intellij.formatting.templateLanguages.DataLanguageBlockWrapper;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlock;
import com.intellij.formatting.templateLanguages.TemplateLanguageFormattingModelBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.templateLanguages.SimpleTemplateLanguageFormattingModelBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

import java.util.List;

/**
 * Big thanks to https://github.com/dmarcotte/idea-handlebars/pull/27/files
 */
public class JteFormattingModelBuilder extends TemplateLanguageFormattingModelBuilder {
    @Override
    public TemplateLanguageBlock createTemplateLanguageBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, @Nullable List<DataLanguageBlockWrapper> foreignChildren, @NotNull CodeStyleSettings codeStyleSettings) {
        return new JteBlock(this, codeStyleSettings, node, foreignChildren);
    }

    /**
     * We have to override {@link com.intellij.formatting.templateLanguages.TemplateLanguageFormattingModelBuilder#createModel}
     * since after we delegate to some templated languages, those languages (xml/html for sure, potentially others)
     * delegate right back to us to format the OUTER_ELEMENT_TYPE token we tell them to ignore,
     * causing an stack-overflowing loop of polite format-delegation.
     */
    @Override
    public @NotNull FormattingModel createModel(@NotNull FormattingContext context) {

        PsiElement element = context.getPsiElement();
        ASTNode node = element.getNode();

        if (node.getElementType() == JteTokenTypes.OUTER_ELEMENT_TYPE) {
            // If we're looking at a OUTER_ELEMENT_TYPE element, then we've been invoked by our templated
            // language. Make a dummy block to allow that formatter to continue
            return new SimpleTemplateLanguageFormattingModelBuilder().createModel(context);
        } else {
            return super.createModel(context);
        }
    }

    @Override
    public boolean dontFormatMyModel() {
        return false;
    }
}
