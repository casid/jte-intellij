package org.jusecase.kte.intellij.language;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.kte.intellij.language.psi.KtePsiKotlinContent;

public class KteKotlinContentManipulator extends AbstractElementManipulator<KtePsiKotlinContent> {
    @Nullable
    @Override
    public KtePsiKotlinContent handleContentChange(@NotNull KtePsiKotlinContent element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        Document document = FileDocumentManager.getInstance().getDocument(element.getContainingFile().getVirtualFile());
        if (document != null) {
            document.replaceString(range.getStartOffset(), range.getEndOffset(), newContent);
            PsiDocumentManager.getInstance(element.getProject()).commitDocument(document);
        }

        return element;
    }
}
