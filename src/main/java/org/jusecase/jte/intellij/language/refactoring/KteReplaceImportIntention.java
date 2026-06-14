package org.jusecase.jte.intellij.language.refactoring;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public final class KteReplaceImportIntention implements IntentionAction {
    private final TextRange importRange;
    private final String qualifiedName;

    public KteReplaceImportIntention(@NotNull TextRange importRange, @NotNull String qualifiedName) {
        this.importRange = importRange;
        this.qualifiedName = qualifiedName;
    }

    @Override
    public @NotNull String getText() {
        return "Replace import with '" + qualifiedName + "'";
    }

    @Override
    public @NotNull String getFamilyName() {
        return "Replace .kte import";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return file != null &&
                KteNativeTemplateSourceEditUtil.isKteFile(file) &&
                KteNativeTemplateSourceEditUtil.isValidRange(file, importRange);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
        if (file != null) {
            KteNativeTemplateSourceEditUtil.replaceImport(file, importRange, qualifiedName);
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
