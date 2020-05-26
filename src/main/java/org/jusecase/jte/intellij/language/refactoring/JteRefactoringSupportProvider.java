package org.jusecase.jte.intellij.language.refactoring;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.psi.JtePsiDefineName;
import org.jusecase.jte.intellij.language.psi.JtePsiRenderName;

public class JteRefactoringSupportProvider extends RefactoringSupportProvider {
    @Override
    public boolean isMemberInplaceRenameAvailable(@NotNull PsiElement element, @Nullable PsiElement context) {
        return element instanceof JtePsiDefineName || element instanceof JtePsiRenderName;
    }
}
