package org.jusecase.jte.intellij.language;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.psi.*;

public class JteAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof JtePsiTagName) {
            doAnnotate((JtePsiTagName) element, holder);
        } else if (element instanceof JtePsiIf) {
            doAnnotate((JtePsiIf) element, holder);
        } else if (element instanceof JtePsiFor) {
            doAnnotate((JtePsiFor) element, holder);
        } else if (element instanceof JtePsiContent) {
            doAnnotate((JtePsiContent) element, holder);
        } else if (element instanceof JtePsiElseIf) {
            doAnnotate((JtePsiElseIf)element, holder);
        } else if (element instanceof JtePsiElse) {
            doAnnotate((JtePsiElse)element, holder);
        } else if (element instanceof JtePsiEndIf) {
            doAnnotate((JtePsiEndIf)element, holder);
        } else if (element instanceof JtePsiEndContent) {
            doAnnotate((JtePsiEndContent)element, holder);
        } else if (element instanceof JtePsiEndFor) {
            doAnnotate((JtePsiEndFor)element, holder);
        } else if (element instanceof JtePsiJavaInjection) {
            doAnnotate((JtePsiJavaInjection)element, holder);
        }
    }

    private void doAnnotate(JtePsiTagName element, AnnotationHolder holder) {
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

    private void doAnnotate(JtePsiContent element, AnnotationHolder holder) {
        if (!(element.getLastChild() instanceof JtePsiEndContent)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing `").create();
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

    private void doAnnotate(JtePsiEndContent element, AnnotationHolder holder) {
        if (!(element.getParent() instanceof JtePsiContent)) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Missing @`").create();
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

    private void doAnnotate(@NotNull JtePsiJavaInjection element, @NotNull AnnotationHolder holder) {
        // This only works for jte, not kte templates! If needed for kte, this would require an additional implementation using Kotlin PSI elements.
        if (!JteLanguage.INSTANCE.equals(element.getLanguage())) {
            return;
        }

        PsiElement parent = element.getParent();
        if (!(parent instanceof JtePsiTag || parent instanceof JtePsiLayout)) {
            return;
        }

        JtePsiTagName tagOrLayoutName = PsiTreeUtil.getChildOfType(parent, JtePsiTagName.class);
        if (tagOrLayoutName == null) {
            return;
        }

        PsiFile tagOrLayoutFile = tagOrLayoutName.resolveFile();
        if (tagOrLayoutFile == null) {
            return;
        }

        JtePsiParamName paramName = PsiTreeUtil.getPrevSiblingOfType(element, JtePsiParamName.class);
        if (paramName == null) {
            return;
        }

        PsiParameterList psiParameterList = JtePsiUtil.resolveParameterList(tagOrLayoutFile);
        if (psiParameterList == null) {
            return;
        }

        PsiParameter javaParameter = paramName.getParameterWithSameName(psiParameterList);
        if (javaParameter == null) {
            return;
        }

        PsiElement injectedElementAt = InjectedLanguageManager.getInstance(parent.getProject()).findInjectedElementAt(parent.getContainingFile(), element.getTextOffset());
        if (injectedElementAt == null) {
            return;
        }

        PsiExpression injectedExpression = JtePsiUtil.getTopMostParentOfType(injectedElementAt, PsiExpression.class, injectedElementAt.getTextOffset());
        if (injectedExpression == null) {
            return;
        }

        if (injectedExpression.getType() == null) {
            return;
        }

        if (!javaParameter.getType().isAssignableFrom(injectedExpression.getType())) {
            holder.newAnnotation(HighlightSeverity.ERROR, injectedExpression.getType().getCanonicalText() + " cannot be cast to " + javaParameter.getType().getCanonicalText()).create();
        }
    }
}
