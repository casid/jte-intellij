package org.jusecase.jte.intellij.language.refactoring;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public final class KteInsertMissingAssignmentIntention implements IntentionAction {
    private static final String TEXT = "Insert missing parameter assignment";

    private final TextRange nameRange;
    private final TextRange replacementRange;
    private final String name;

    public KteInsertMissingAssignmentIntention(@NotNull TextRange nameRange,
                                               @NotNull TextRange replacementRange,
                                               @NotNull String name) {
        this.nameRange = nameRange;
        this.replacementRange = replacementRange;
        this.name = name;
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
                KteNativeTemplateSourceEditUtil.isValidRange(file, nameRange) &&
                KteNativeTemplateSourceEditUtil.isValidRange(file, replacementRange) &&
                name.equals(file.getText().substring(nameRange.getStartOffset(), nameRange.getEndOffset()));
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
        if (file == null) {
            return;
        }

        String replacement = name + " = ";
        KteNativeTemplateSourceEditUtil.replace(file, effectiveReplacementRange(file), replacement);
        if (editor != null) {
            editor.getCaretModel().moveToOffset(nameRange.getStartOffset() + replacement.length());
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

    @NotNull
    private TextRange effectiveReplacementRange(@NotNull PsiFile file) {
        String text = file.getText();
        int equalsOffset = skipWhitespace(text, nameRange.getEndOffset());
        if (equalsOffset < text.length() && text.charAt(equalsOffset) == '=') {
            return TextRange.create(nameRange.getStartOffset(), skipWhitespace(text, equalsOffset + 1));
        }

        return replacementRange;
    }

    private int skipWhitespace(@NotNull String text, int offset) {
        int result = offset;
        while (result < text.length() && Character.isWhitespace(text.charAt(result))) {
            result++;
        }
        return result;
    }
}
