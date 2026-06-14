package org.jusecase.jte.intellij.language.refactoring;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public final class KteOptimizeImportsIntention implements IntentionAction {
    private static final String TEXT = "Optimize .kte imports";

    @Override
    public @NotNull String getText() {
        return TEXT;
    }

    @Override
    public @NotNull String getFamilyName() {
        return TEXT;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return file != null && KteNativeTemplateSourceEditUtil.computeOptimizedImportReplacement(file) != null;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
        if (file != null) {
            KteNativeTemplateSourceEditUtil.optimizeImports(file);
        }
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
