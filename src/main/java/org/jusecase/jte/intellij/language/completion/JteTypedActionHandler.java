package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;
import org.jusecase.jte.intellij.language.parsing.KteTokenTypes;

public class JteTypedActionHandler extends TypedHandlerDelegate {

    @Override
    public @NotNull Result charTyped(char charTyped, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (charTyped == '@' || charTyped == '$' || charTyped == '!') {
            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
            if (psiFile == null) {
                return Result.CONTINUE;
            }

            if (psiFile.getFileElementType() != JteTokenTypes.FILE && psiFile.getFileElementType() != KteTokenTypes.FILE) {
                return Result.CONTINUE;
            }

            invokeAutoPopup(project, editor, charTyped);
            return Result.STOP;
        }

        return Result.CONTINUE;
    }

    private void invokeAutoPopup(@NotNull Project project, @NotNull Editor editor, char charTyped) {
        AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, file -> {
            int offset = editor.getCaretModel().getOffset();

            PsiElement lastElement = file.findElementAt(offset - 1);
            return lastElement != null && StringUtil.endsWithChar(lastElement.getText(), charTyped);
        });
    }
}
