package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import org.jusecase.jte.intellij.language.parsing.KteTokenTypes;

import static com.intellij.patterns.PlatformPatterns.psiElement;


public class KteCompletionContributor extends CompletionContributor {
    public KteCompletionContributor() {
        extend(null, psiElement(KteTokenTypes.TEMPLATE_NAME), new KteTemplateCompletionProvider());
        extend(null, psiElement(), new KteTemplateParamCompletionProvider(false));
    }
}