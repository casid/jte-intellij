package org.jusecase.kte.intellij.language;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.kte.intellij.language.psi.KtePsiKotlinContent;

public class KteKotlinContentManipulator extends AbstractElementManipulator<KtePsiKotlinContent> {
    @Nullable
    @Override
    public KtePsiKotlinContent handleContentChange(@NotNull KtePsiKotlinContent element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        String oldText = element.getText();
        String newText = oldText.substring(0, range.getStartOffset()) + newContent + oldText.substring(range.getEndOffset());
        FileType type = element.getContainingFile().getFileType();
        PsiFile fromText = PsiFileFactory.getInstance(element.getProject()).createFileFromText("__." + type.getDefaultExtension(), type, newText);
        KtePsiKotlinContent newElement = PsiTreeUtil.getParentOfType(fromText.findElementAt(0), element.getClass(), false);
        assert newElement != null : type + " " + type.getDefaultExtension() + " " + newText;
        return (KtePsiKotlinContent)element.replace(newElement);
    }
}
