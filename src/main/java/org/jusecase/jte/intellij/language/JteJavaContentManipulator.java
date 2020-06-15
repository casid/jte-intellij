package org.jusecase.jte.intellij.language;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.psi.JtePsiJavaContent;

public class JteJavaContentManipulator extends AbstractElementManipulator<JtePsiJavaContent> {
    @Nullable
    @Override
    public JtePsiJavaContent handleContentChange(@NotNull JtePsiJavaContent element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        Document document = FileDocumentManager.getInstance().getDocument(element.getContainingFile().getVirtualFile());
        if (document != null) {
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(element.getProject());
            if (documentManager.isDocumentBlockedByPsi(document)) {
                documentManager.doPostponedOperationsAndUnblockDocument(document);
            }
            document.replaceString(range.getStartOffset(), range.getEndOffset(), newContent);
            documentManager.commitDocument(document);
        }

        return element;
    }
}
