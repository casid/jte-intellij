package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.patterns.PlatformPatterns;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;


public class JteCompletionContributor extends CompletionContributor {
    public JteCompletionContributor() {
        extend(null, PlatformPatterns.psiElement(JteTokenTypes.TAG_NAME), new JteTagOrLayoutCompletionProvider());
        extend(null, PlatformPatterns.psiElement(JteTokenTypes.LAYOUT_NAME), new JteTagOrLayoutCompletionProvider());
        extend(null, PlatformPatterns.psiElement(JteTokenTypes.DEFINE_NAME), new JteDefineCompletionProvider());
    }
}