package org.jusecase.kte.intellij.language;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jusecase.kte.intellij.language.psi.KtePsiTagOrLayoutName;

public class KteAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof KtePsiTagOrLayoutName) {
            doAnnotate((KtePsiTagOrLayoutName) element, holder);
        }
    }

    private void doAnnotate(KtePsiTagOrLayoutName element, AnnotationHolder holder) {
        if (element.getReference() == null) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved " + element.getIdentifier()).create();
        }
    }
}
