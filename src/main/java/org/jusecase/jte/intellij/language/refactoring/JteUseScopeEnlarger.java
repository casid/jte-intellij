package org.jusecase.jte.intellij.language.refactoring;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.UseScopeEnlarger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JteUseScopeEnlarger extends UseScopeEnlarger {
    @Nullable
    @Override
    public SearchScope getAdditionalUseScope(@NotNull PsiElement element) {
        if (!(element instanceof PsiParameter)) {
            return null;
        }

        if (!element.getContainingFile().getName().endsWith(".jte")) {
            return null;
        }

        return new JteSearchScope(element.getProject());
    }
}
