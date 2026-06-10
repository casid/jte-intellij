package org.jusecase.jte.intellij.language.refactoring;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public final class KteAddImportIntention implements IntentionAction {
    private final String qualifiedName;

    public KteAddImportIntention(@NotNull String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    @Override
    public @NotNull String getText() {
        return "Import '" + qualifiedName + "'";
    }

    @Override
    public @NotNull String getFamilyName() {
        return "Import .kte symbol";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return file != null && KteNativeTemplateSourceEditUtil.isKteFile(file);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
        if (file != null) {
            KteNativeTemplateSourceEditUtil.addImport(file, qualifiedName);
        }
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project,
                                                         @NotNull Editor editor,
                                                         @NotNull PsiFile file) {
        return IntentionPreviewInfo.EMPTY;
    }
}
