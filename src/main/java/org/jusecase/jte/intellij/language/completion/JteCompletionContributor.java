package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

import static com.intellij.patterns.PlatformPatterns.psiElement;


public class JteCompletionContributor extends CompletionContributor {
    public JteCompletionContributor() {
        extend(null, psiElement(JteTokenTypes.TEMPLATE_NAME), new JteTemplateCompletionProvider());
        extend(null, psiElement(), new JteTemplateParamCompletionProvider(false));
    }
}