package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import org.jusecase.jte.intellij.language.parsing.KteTokenTypes;

import static com.intellij.patterns.PlatformPatterns.psiElement;


public class KteCompletionContributor extends CompletionContributor {
    public KteCompletionContributor() {
        extend(null, psiElement(KteTokenTypes.TAG_NAME), new KteTagOrLayoutCompletionProvider());
        extend(null, psiElement(), new KteTagOrLayoutParamCompletionProvider(false));
    }
}