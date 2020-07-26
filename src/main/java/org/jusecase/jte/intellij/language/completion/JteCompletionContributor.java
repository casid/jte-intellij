package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

import static com.intellij.patterns.PlatformPatterns.psiElement;


public class JteCompletionContributor extends CompletionContributor {
    public JteCompletionContributor() {
        extend(null, psiElement(JteTokenTypes.TAG_NAME), new JteTagOrLayoutCompletionProvider());
        extend(null, psiElement(), new JteTagOrLayoutParamCompletionProvider(false));
    }
}