package org.jusecase.jte.intellij.language.refactoring;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.UseScopeEnlarger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtNamedDeclaration;
import org.jetbrains.kotlin.psi.KtParameter;
import org.jusecase.jte.intellij.language.psi.JtePsiParam;

public class JteUseScopeEnlarger extends UseScopeEnlarger {
    @Nullable
    @Override
    public SearchScope getAdditionalUseScope(@NotNull PsiElement element) {
        if (element instanceof JtePsiParam) {
            return GlobalSearchScope.projectScope(element.getProject());
        } else if (element instanceof PsiParameter) {
            PsiFile containingFile = element.getContainingFile();
            if (containingFile == null) {
                return null;
            }

            if (!containingFile.getName().endsWith(".jte")) {
                return null;
            }

            return new JteSearchScope(element.getProject());
        } else if (element instanceof KtParameter) {
            // .kte Kotlin references are source-mapped from template PSI; this only broadens the
            // search space for Kotlin declarations that can be referenced from .kte files.
            PsiFile containingFile = element.getContainingFile();
            if (!containingFile.getName().endsWith(".kte")) {
                return null;
            }

            return new KteSearchScope(element.getProject());
        } else if (element instanceof KtNamedDeclaration) {
            // Normal .kt declarations can have .kte usages even though .kte no longer uses an
            // injected Kotlin file as the semantic source.
            PsiFile containingFile = element.getContainingFile();
            if (containingFile == null || !containingFile.getName().endsWith(".kt")) {
                return null;
            }

            return new KteSearchScope(element.getProject());
        }

        return null;
    }
}
