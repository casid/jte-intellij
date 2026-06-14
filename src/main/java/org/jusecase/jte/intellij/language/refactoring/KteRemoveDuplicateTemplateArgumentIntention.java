package org.jusecase.jte.intellij.language.refactoring;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public final class KteRemoveDuplicateTemplateArgumentIntention implements IntentionAction {
    private static final String TEXT = "Remove duplicate template parameter";

    private final TextRange argumentRange;

    public KteRemoveDuplicateTemplateArgumentIntention(@NotNull TextRange argumentRange) {
        this.argumentRange = argumentRange;
    }

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
        return file != null &&
                KteNativeTemplateSourceEditUtil.isKteFile(file) &&
                KteNativeTemplateSourceEditUtil.isValidRange(file, argumentRange);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
        if (file != null) {
            KteNativeTemplateSourceEditUtil.delete(file,
                    KteNativeTemplateSourceEditUtil.argumentRemovalRange(file, argumentRange));
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
