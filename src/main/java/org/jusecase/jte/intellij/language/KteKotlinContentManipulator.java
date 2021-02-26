package org.jusecase.jte.intellij.language;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.psi.KtePsiJavaContent;

public class KteKotlinContentManipulator extends AbstractElementManipulator<KtePsiJavaContent> {
    @Nullable
    @Override
    public KtePsiJavaContent handleContentChange(@NotNull KtePsiJavaContent element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        Document document = FileDocumentManager.getInstance().getDocument(element.getContainingFile().getVirtualFile());
        if (document != null) {
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(element.getProject());
            if (documentManager.isDocumentBlockedByPsi(document)) {
                documentManager.doPostponedOperationsAndUnblockDocument(document);
            }

            document.replaceString(range.getStartOffset(), range.getEndOffset(), newContent);
            documentManager.commitDocument(document);

            optimizeImportsAfterContentManipulation(document, element, newContent);
        }

        return element;
    }

    private void optimizeImportsAfterContentManipulation(@NotNull Document document, @NotNull KtePsiJavaContent element, String newContent) {
        if (isRequiredToOptimizeImports(newContent)) {
            ApplicationManager.getApplication().invokeLater(() -> {
                //noinspection DialogTitleCapitalization
                WriteCommandAction.runWriteCommandAction(element.getProject(), "Optimize jte Imports", null, () -> {
                    String optimizedText = document.getText().replace(";import ", "\n@import ");
                    document.setText(optimizedText);
                }, element.getContainingFile());
            });
        }
    }

    private boolean isRequiredToOptimizeImports(String newContent) {
        return false; // TODO
    }
}
