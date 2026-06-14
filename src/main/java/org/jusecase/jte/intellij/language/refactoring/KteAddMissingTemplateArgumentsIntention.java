package org.jusecase.jte.intellij.language.refactoring;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class KteAddMissingTemplateArgumentsIntention implements IntentionAction {
    private final TextRange templateRange;
    private final List<String> parameterNames;

    public KteAddMissingTemplateArgumentsIntention(@NotNull TextRange templateRange,
                                                   @NotNull List<String> parameterNames) {
        this.templateRange = templateRange;
        this.parameterNames = List.copyOf(parameterNames);
    }

    @Override
    public @NotNull String getText() {
        return "Add missing template parameters";
    }

    @Override
    public @NotNull String getFamilyName() {
        return "Add missing template parameters";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return file != null &&
                !parameterNames.isEmpty() &&
                KteNativeTemplateSourceEditUtil.isKteFile(file) &&
                KteNativeTemplateSourceEditUtil.isValidRange(file, templateRange) &&
                insertionOffset(file) != -1;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
        if (file == null) {
            return;
        }

        int insertOffset = insertionOffset(file);
        if (insertOffset == -1) {
            return;
        }

        String insertion = insertionPrefix(file, insertOffset) + missingArgumentsText();
        KteNativeTemplateSourceEditUtil.insert(file, insertOffset, insertion);
        if (editor != null) {
            int caretOffset = insertOffset + insertion.indexOf(parameterNames.getFirst()) +
                    parameterNames.getFirst().length() + " = ".length();
            editor.getCaretModel().moveToOffset(caretOffset);
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

    private int insertionOffset(@NotNull PsiFile file) {
        String text = file.getText();
        int endOffset = Math.min(templateRange.getEndOffset(), text.length());
        int offset = endOffset - 1;
        while (offset >= templateRange.getStartOffset() && Character.isWhitespace(text.charAt(offset))) {
            offset--;
        }

        return offset >= templateRange.getStartOffset() && text.charAt(offset) == ')' ? offset : -1;
    }

    @NotNull
    private String insertionPrefix(@NotNull PsiFile file, int insertionOffset) {
        String text = file.getText();
        int paramsStart = text.indexOf('(', templateRange.getStartOffset());
        if (paramsStart == -1 || paramsStart >= insertionOffset) {
            return "";
        }

        return text.substring(paramsStart + 1, insertionOffset).trim().isEmpty() ? "" : ", ";
    }

    @NotNull
    private String missingArgumentsText() {
        return String.join(", ", parameterNames.stream()
                .map(parameterName -> parameterName + " = ")
                .toList());
    }
}
