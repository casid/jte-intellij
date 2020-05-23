package org.jusecase.kte.intellij.language.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.patterns.PlatformPatterns;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class KteCompletionContributor extends CompletionContributor {
    public KteCompletionContributor() {
        extend(null, PlatformPatterns.psiElement(KteTokenTypes.TAG_NAME), new KteTagCompletionProvider());
    }
}
