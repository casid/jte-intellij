package org.jusecase.jte.intellij.language;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.psi.JtePsiTagOrLayoutName;
import org.jusecase.jte.intellij.language.psi.*;

public class JteAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof JtePsiTagOrLayoutName) {
            doAnnotate((JtePsiTagOrLayoutName) element, holder);
        } else if (element instanceof JtePsiIf) {
            doAnnotate((JtePsiIf) element, holder);
        } else if (element instanceof JtePsiFor) {
            doAnnotate((JtePsiFor) element, holder);
        } else if (element instanceof JtePsiLayout) {
            doAnnotate((JtePsiLayout) element, holder);
        } else if (element instanceof JtePsiDefine) {
            doAnnotate((JtePsiDefine) element, holder);
        } else if (element instanceof JtePsiElseIf) {
            doAnnotate((JtePsiElseIf)element, holder);
        } else if (element instanceof JtePsiElse) {
            doAnnotate((JtePsiElse)element, holder);
        } else if (element instanceof JtePsiEndIf) {
            doAnnotate((JtePsiEndIf)element, holder);
        } else if (element instanceof JtePsiEndDefine) {
            doAnnotate((JtePsiEndDefine)element, holder);
        } else if (element instanceof JtePsiEndLayout) {
            doAnnotate((JtePsiEndLayout)element, holder);
        } else if (element instanceof JtePsiEndFor) {
            doAnnotate((JtePsiEndFor)element, holder);
        }
    }

    private void doAnnotate(JtePsiTagOrLayoutName element, AnnotationHolder holder) {
        if (element.getReference() == null) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved " + element.getIdentifier()).create();
        }
    }

    private void doAnnotate(JtePsiIf element, AnnotationHolder holder) {
        if (!(element.getLastChild() instanceof JtePsiEndIf)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @endif").create();
        }
    }

    private void doAnnotate(JtePsiFor element, AnnotationHolder holder) {
        if (!(element.getLastChild() instanceof JtePsiEndFor)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @endfor").create();
        }
    }

    private void doAnnotate(JtePsiLayout element, AnnotationHolder holder) {
        if (!(element.getLastChild() instanceof JtePsiEndLayout)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @endlayout").create();
        }
    }

    private void doAnnotate(JtePsiDefine element, AnnotationHolder holder) {
        if (!(element.getLastChild() instanceof JtePsiEndDefine)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @enddefine").create();
        }
    }

    private void doAnnotate(JtePsiElseIf element, AnnotationHolder holder) {
        checkParentIsIf(element, holder);
    }

    private void doAnnotate(JtePsiElse element, AnnotationHolder holder) {
        checkParentIsIf(element, holder);
        if (PsiTreeUtil.getPrevSiblingOfType(element, JtePsiElse.class) != null) {
            holder.newAnnotation(HighlightSeverity.ERROR, "More than one @else").create();
        }
    }

    private void doAnnotate(JtePsiEndIf element, AnnotationHolder holder) {
        checkParentIsIf(element, holder);
    }

    private void doAnnotate(JtePsiEndDefine element, AnnotationHolder holder) {
        if (!(element.getParent() instanceof JtePsiDefine)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @define").create();
        }
    }

    private void doAnnotate(JtePsiEndLayout element, AnnotationHolder holder) {
        if (!(element.getParent() instanceof JtePsiLayout)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @layout").create();
        }
    }

    private void doAnnotate(JtePsiEndFor element, AnnotationHolder holder) {
        if (!(element.getParent() instanceof JtePsiFor)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @for").create();
        }
    }

    private void checkParentIsIf(JtePsiElement element, AnnotationHolder holder) {
        if (!(element.getParent() instanceof JtePsiIf)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @if").create();
        }
    }
}
