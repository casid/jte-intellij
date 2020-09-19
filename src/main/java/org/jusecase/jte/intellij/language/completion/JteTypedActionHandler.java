package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.template.impl.editorActions.TypedActionHandlerBase;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class JteTypedActionHandler extends TypedActionHandlerBase {

    public JteTypedActionHandler(TypedActionHandler originalHandler) {
        super(originalHandler);
    }

    @Override
    public void execute(@NotNull Editor editor, char charTyped, @NotNull DataContext dataContext) {
        if (myOriginalHandler != null) {
            myOriginalHandler.execute(editor, charTyped, dataContext);
        }

        // Must be invoked after original handler, otherwise android xml handler overwrites the auto popup after we set it...
        if (charTyped == '@' || charTyped == '$' || charTyped == '!') {
            Project project = editor.getProject();
            if (project == null) {
                return;
            }

            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
            if (psiFile == null) {
                return;
            }

            if (psiFile.getFileElementType() != JteTokenTypes.FILE) {
                return;
            }

            invokeAutoPopup(project, editor, charTyped);
        }
    }

    private void invokeAutoPopup(@NotNull Project project, @NotNull Editor editor, char charTyped) {
        AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, file -> {
            int offset = editor.getCaretModel().getOffset();

            PsiElement lastElement = file.findElementAt(offset - 1);
            return lastElement != null && StringUtil.endsWithChar(lastElement.getText(), charTyped);
        });
    }
}
