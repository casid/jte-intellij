package org.jusecase.kte.intellij.language;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
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
        } else if (element instanceof KtePsiElseIf) {
            doAnnotate((KtePsiElseIf)element, holder);
        } else if (element instanceof KtePsiElse) {
            doAnnotate((KtePsiElse)element, holder);
        } else if (element instanceof KtePsiEndIf) {
            doAnnotate((KtePsiEndIf)element, holder);
        } else if (element instanceof KtePsiEndDefine) {
            doAnnotate((KtePsiEndDefine)element, holder);
        } else if (element instanceof KtePsiEndLayout) {
            doAnnotate((KtePsiEndLayout)element, holder);
        } else if (element instanceof KtePsiEndFor) {
            doAnnotate((KtePsiEndFor)element, holder);
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

    private void doAnnotate(KtePsiElseIf element, AnnotationHolder holder) {
        checkParentIsIf(element, holder);
    }

    private void doAnnotate(KtePsiElse element, AnnotationHolder holder) {
        checkParentIsIf(element, holder);
        if (PsiTreeUtil.getPrevSiblingOfType(element, KtePsiElse.class) != null) {
            holder.newAnnotation(HighlightSeverity.ERROR, "More than one @else").create();
        }
    }

    private void doAnnotate(KtePsiEndIf element, AnnotationHolder holder) {
        checkParentIsIf(element, holder);
    }

    private void doAnnotate(KtePsiEndDefine element, AnnotationHolder holder) {
        if (!(element.getParent() instanceof KtePsiDefine)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @define").create();
        }
    }

    private void doAnnotate(KtePsiEndLayout element, AnnotationHolder holder) {
        if (!(element.getParent() instanceof KtePsiLayout)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @layout").create();
        }
    }

    private void doAnnotate(KtePsiEndFor element, AnnotationHolder holder) {
        if (!(element.getParent() instanceof KtePsiFor)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @for").create();
        }
    }

    private void checkParentIsIf(KtePsiElement element, AnnotationHolder holder) {
        if (!(element.getParent() instanceof KtePsiIf)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @if").create();
        }
    }
}
