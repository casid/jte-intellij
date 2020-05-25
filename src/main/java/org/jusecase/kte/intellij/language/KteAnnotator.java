package org.jusecase.kte.intellij.language;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jusecase.kte.intellij.language.psi.*;

public class KteAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof KtePsiTagOrLayoutName) {
            doAnnotate((KtePsiTagOrLayoutName) element, holder);
        } else if (element instanceof KtePsiIf) {
            doAnnotate((KtePsiIf) element, holder);
        } else if (element instanceof KtePsiFor) {
            doAnnotate((KtePsiFor) element, holder);
        } else if (element instanceof KtePsiLayout) {
            doAnnotate((KtePsiLayout) element, holder);
        } else if (element instanceof KtePsiDefine) {
            doAnnotate((KtePsiDefine) element, holder);
        }
    }

    private void doAnnotate(KtePsiTagOrLayoutName element, AnnotationHolder holder) {
        if (element.getReference() == null) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved " + element.getIdentifier()).create();
        }
    }

    private void doAnnotate(KtePsiIf element, AnnotationHolder holder) {
        if (!(element.getLastChild() instanceof KtePsiEndIf)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @endif").create();
        }
    }

    private void doAnnotate(KtePsiFor element, AnnotationHolder holder) {
        if (!(element.getLastChild() instanceof KtePsiEndFor)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @endfor").create();
        }
    }

    private void doAnnotate(KtePsiLayout element, AnnotationHolder holder) {
        if (!(element.getLastChild() instanceof KtePsiEndLayout)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @endlayout").create();
        }
    }

    private void doAnnotate(KtePsiDefine element, AnnotationHolder holder) {
        if (!(element.getLastChild() instanceof KtePsiEndDefine)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @enddefine").create();
        }
    }
}
