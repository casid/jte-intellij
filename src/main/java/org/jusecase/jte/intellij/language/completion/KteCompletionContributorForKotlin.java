package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.patterns.PatternCondition;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;


public class KteCompletionContributorForKotlin extends CompletionContributor {
    public KteCompletionContributorForKotlin() {
        extend(null, psiElement().with(new PatternCondition<PsiElement>("KteFile") {
            @Override
            public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
                return element.getContainingFile() != null && element.getContainingFile().getName().endsWith(".kte");
            }
        }), new KteTagOrLayoutParamCompletionProvider(true));
    }
}