package org.jusecase.kte.intellij.language.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jusecase.kte.intellij.language.psi.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class KteDefineCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        if (position.getParent() == null) {
            return;
        }

        if (!(position.getParent() instanceof KtePsiDefineName)) {
            return;
        }
        KtePsiDefineName nameElement = (KtePsiDefineName) position.getParent();

        Collection<KtePsiRenderName> renderNames = nameElement.getRenderNames();
        if (renderNames == null) {
            return;
        }

        Set<String> existingDefinitions = findExistingDefinitions(nameElement);

        for (KtePsiRenderName renderName : renderNames) {
            if (!existingDefinitions.contains(renderName.getName())) {
                result.addElement(LookupElementBuilder.create(renderName));
            }
        }
    }

    private Set<String> findExistingDefinitions(KtePsiDefineName nameElement) {
        KtePsiLayout layout = PsiTreeUtil.getParentOfType(nameElement, KtePsiLayout.class);
        if (layout == null) {
            return Collections.emptySet();
        }

        Set<String> result = new HashSet<>();
        findExistingDefinitions(layout, result);
        return result;
    }

    private void findExistingDefinitions(KtePsiElement element, Set<String> result) {
        for (PsiElement child : element.getChildren()) {
            if (child instanceof KtePsiElement) {
                if (child instanceof KtePsiDefineName) {
                    result.add(((KtePsiDefineName) child).getName());
                    continue;
                } else if (child instanceof KtePsiLayout) {
                    // Nested layout needs to be ignored...
                    continue;
                }

                findExistingDefinitions((KtePsiElement) child, result);
            }
        }
    }
}
