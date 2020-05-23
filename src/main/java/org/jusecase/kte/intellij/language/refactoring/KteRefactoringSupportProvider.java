package org.jusecase.kte.intellij.language.refactoring;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.kte.intellij.language.psi.KtePsiDefineName;
import org.jusecase.kte.intellij.language.psi.KtePsiRenderName;

public class KteRefactoringSupportProvider extends RefactoringSupportProvider {
    @Override
    public boolean isMemberInplaceRenameAvailable(@NotNull PsiElement element, @Nullable PsiElement context) {
        return element instanceof KtePsiDefineName || element instanceof KtePsiRenderName;
    }
}
