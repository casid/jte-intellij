package org.jusecase.jte.intellij.language;

import com.intellij.codeInsight.Nullability;
import com.intellij.codeInsight.NullableNotNullManager;
import com.intellij.codeInspection.dataFlow.NullabilityUtil;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.k2.KteTemplateSignatureService;
import org.jusecase.jte.intellij.language.psi.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class JteAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        switch (element) {
            case JtePsiTemplateName jtePsiTemplateName -> doAnnotate(jtePsiTemplateName, holder);
            case JtePsiIf jtePsiIf -> doAnnotate(jtePsiIf, holder);
            case JtePsiFor jtePsiFor -> doAnnotate(jtePsiFor, holder);
            case JtePsiContent jtePsiContent -> doAnnotate(jtePsiContent, holder);
            case JtePsiElseIf jtePsiElseIf -> doAnnotate(jtePsiElseIf, holder);
            case JtePsiElse jtePsiElse -> doAnnotate(jtePsiElse, holder);
            case JtePsiEndIf jtePsiEndIf -> doAnnotate(jtePsiEndIf, holder);
            case JtePsiEndContent jtePsiEndContent -> doAnnotate(jtePsiEndContent, holder);
            case JtePsiEndFor jtePsiEndFor -> doAnnotate(jtePsiEndFor, holder);
            case JtePsiJavaInjection jtePsiJavaInjection -> doAnnotate(jtePsiJavaInjection, holder);
            case JtePsiTemplate ignored -> doAnnotateMissingTemplateParams(element, holder);
            case JtePsiParamName jtePsiParamName -> doAnnotate(jtePsiParamName, holder);
            default -> {}
        }
    }

    private void doAnnotate(JtePsiTemplateName element, AnnotationHolder holder) {
        if (element.getReference() == null) {
            if (element.findRootDirectory() == null) {
                addError(holder, "Please add a '" + JtePsiTemplateName.JTE_ROOT + "' file to the root source directory of your jte sources, so that IntelliJ knows how to reference templates.");
            } else {
                if (element.isDirectory()) {
                    addError(holder, "Unresolved directory " + element.getName());
                } else {
                    addError(holder, "Unresolved template");
                }
            }
        }
    }

    private void doAnnotate(JtePsiIf element, AnnotationHolder holder) {
        if (!(element.getLastChild() instanceof JtePsiEndIf)) {
            addError(holder, "Missing @endif");
        }
    }

    private void doAnnotate(JtePsiFor element, AnnotationHolder holder) {
        if (!(element.getLastChild() instanceof JtePsiEndFor)) {
            addError(holder, "Missing @endfor");
        }
    }

    private void doAnnotate(JtePsiContent element, AnnotationHolder holder) {
        if (!(element.getLastChild() instanceof JtePsiEndContent)) {
            addError(holder, "Missing `");
        }

        doAnnotateContentParam(element, holder);
    }

    private void doAnnotate(JtePsiElseIf element, AnnotationHolder holder) {
        checkParentIsIf(element, holder);
    }

    private void doAnnotate(JtePsiElse element, AnnotationHolder holder) {
        checkParentIsIfOrFor(element, holder);
        if (PsiTreeUtil.getPrevSiblingOfType(element, JtePsiElse.class) != null) {
            addError(holder, "More than one @else");
        }
    }

    private void doAnnotate(JtePsiEndIf element, AnnotationHolder holder) {
        checkParentIsIf(element, holder);
    }

    private void doAnnotate(JtePsiEndContent element, AnnotationHolder holder) {
        if (!(element.getParent() instanceof JtePsiContent)) {
            addError(holder, "Missing @`");
        }
    }

    private void doAnnotate(JtePsiEndFor element, AnnotationHolder holder) {
        if (!(element.getParent() instanceof JtePsiFor)) {
            addError(holder, "Missing @for");
        }
    }

    private void checkParentIsIf(JtePsiElement element, AnnotationHolder holder) {
        if (!(element.getParent() instanceof JtePsiIf)) {
            addError(holder, "Missing @if");
        }
    }

    private void checkParentIsIfOrFor(JtePsiElse element, AnnotationHolder holder) {
        if (!(element.getParent() instanceof JtePsiIf) && !(element.getParent() instanceof JtePsiFor)) {
            addError(holder, "Missing @if or @for");
        }
    }

    private void doAnnotate(@NotNull JtePsiJavaInjection element, @NotNull AnnotationHolder holder) {
        // This only works for jte, not kte templates! If needed for kte, this would require an additional implementation using Kotlin PSI elements.
        if (!JteLanguage.INSTANCE.equals(element.getLanguage())) {
            return;
        }

        PsiElement parent = element.getParent();
        if (!(parent instanceof JtePsiTemplate)) {
            return;
        }

        JtePsiTemplateName templateName = JtePsiUtil.getLastChildOfType(parent, JtePsiTemplateName.class);
        if (templateName == null) {
            return;
        }

        PsiFile templateFile = templateName.resolveFile();
        if (templateFile == null) {
            return;
        }

        JtePsiParamName paramName = JtePsiUtil.getPrevSiblingIfBefore(element, JtePsiParamName.class, JtePsiJavaInjection.class);
        if (paramName == null) {
            return;
        }

        PsiParameterList psiParameterList = JtePsiUtil.resolveParameterList(templateFile);
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
            addError(holder, injectedExpression.getType().getCanonicalText() + " cannot be cast to " + javaParameter.getType().getCanonicalText());
        }

        doAnnotateNullability(javaParameter, injectedExpression, holder);
    }

    private void doAnnotateNullability(@NotNull PsiParameter javaParameter, @NotNull PsiExpression injectedExpression, @NotNull AnnotationHolder holder) {
        Nullability parameterNullability = NullableNotNullManager.getNullability(javaParameter);

        if (parameterNullability != Nullability.NOT_NULL) {
            return;
        }

        Nullability expressionNullability = NullabilityUtil.getExpressionNullability(injectedExpression, true);

        if (expressionNullability == Nullability.NULLABLE) {
            addWarning(holder, "Argument '" + injectedExpression.getText() + "' is nullable for non-null parameter '" + javaParameter.getName() + "'");
        }

        if (expressionNullability == Nullability.UNKNOWN) {
            addWeakWarning(holder, "Argument '" + injectedExpression.getText() + "' might be null for non-null parameter '" + javaParameter.getName() + "'");
        }
    }

    private void doAnnotateContentParam(@NotNull JtePsiContent element, @NotNull AnnotationHolder holder) {
        // This only works for jte, not kte templates! If needed for kte, this would require an additional implementation using Kotlin PSI elements.
        if (!JteLanguage.INSTANCE.equals(element.getLanguage())) {
            return;
        }

        PsiElement parent = element.getParent();
        if (!(parent instanceof JtePsiTemplate)) {
            return;
        }

        JtePsiTemplateName templateName = JtePsiUtil.getLastChildOfType(parent, JtePsiTemplateName.class);
        if (templateName == null) {
            return;
        }

        PsiFile templateFile = templateName.resolveFile();
        if (templateFile == null) {
            return;
        }

        JtePsiParamName paramName = JtePsiUtil.getPrevSiblingIfBefore(element, JtePsiParamName.class, JtePsiJavaInjection.class);
        if (paramName == null) {
            return;
        }

        PsiParameterList psiParameterList = JtePsiUtil.resolveParameterList(templateFile);
        if (psiParameterList == null) {
            return;
        }

        PsiParameter javaParameter = paramName.getParameterWithSameName(psiParameterList);
        if (javaParameter == null) {
            return;
        }

        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(element.getProject());
        PsiClass contentClass = javaPsiFacade.findClass("gg.jte.Content", GlobalSearchScope.everythingScope(element.getProject()));
        if (contentClass == null) {
            return;
        }

        PsiClassType contentType = javaPsiFacade.getElementFactory().createType(contentClass);

        if (!javaParameter.getType().isAssignableFrom(contentType)) {
            addError(holder, contentType.getCanonicalText() + " cannot be cast to " + javaParameter.getType().getCanonicalText());
        }
    }

    private void doAnnotateMissingTemplateParams(PsiElement element, AnnotationHolder holder) {
        JtePsiTemplateName templateName = JtePsiUtil.getLastChildOfType(element, JtePsiTemplateName.class);
        if (templateName == null) {
            return;
        }

        PsiFile templateFile = templateName.resolveFile();
        if (templateFile == null) {
            return;
        }

        if (KteTemplateSignatureService.isKteTemplate(templateFile)) {
            doAnnotateMissingKteTemplateParams(element, templateFile, holder);
            return;
        }

        List<PsiParameter> requiredParameters = JtePsiUtil.resolveRequiredParameters(templateFile);
        if (requiredParameters.isEmpty()) {
            return;
        }

        Set<String> missingParameters = new LinkedHashSet<>(requiredParameters.size());
        for (PsiParameter requiredParameter : requiredParameters) {
            if (!requiredParameter.isVarArgs()) {
                missingParameters.add(requiredParameter.getName());
            }
        }

        if (element.getLanguage() == KteLanguage.INSTANCE) {
            for (KtePsiParamName paramName = PsiTreeUtil.getChildOfType(element, KtePsiParamName.class);
                 paramName != null;
                 paramName = PsiTreeUtil.getNextSiblingOfType(paramName, KtePsiParamName.class)) {
                missingParameters.remove(paramName.getName());
            }
        } else {
            for (JtePsiParamName paramName = PsiTreeUtil.getChildOfType(element, JtePsiParamName.class);
                 paramName != null;
                 paramName = PsiTreeUtil.getNextSiblingOfType(paramName, JtePsiParamName.class)) {
                missingParameters.remove(paramName.getName());
            }
        }

        if (!missingParameters.isEmpty()) {
            addError(holder, "Missing required parameters: " + String.join(", ", missingParameters));
        }
    }

    private void doAnnotateMissingKteTemplateParams(@NotNull PsiElement element,
                                                    @NotNull PsiFile templateFile,
                                                    @NotNull AnnotationHolder holder) {
        // KTE callers are handled by KteSyntheticKotlinDiagnosticAnnotator. This bridge only covers .jte callers
        // that reference .kte children.
        if (!JteLanguage.INSTANCE.equals(element.getLanguage())) {
            return;
        }

        KteTemplateSignatureService.TemplateSignature signature = KteTemplateSignatureService.resolve(templateFile);
        if (signature.requiredParameters().isEmpty()) {
            return;
        }

        Set<String> missingParameters = new LinkedHashSet<>(signature.requiredParameters().size());
        for (KteTemplateSignatureService.Parameter parameter : signature.requiredParameters()) {
            missingParameters.add(parameter.name());
        }

        for (JtePsiParamName paramName = PsiTreeUtil.getChildOfType(element, JtePsiParamName.class);
             paramName != null;
             paramName = PsiTreeUtil.getNextSiblingOfType(paramName, JtePsiParamName.class)) {
            missingParameters.remove(paramName.getName());
        }

        if (!missingParameters.isEmpty()) {
            addError(holder, "Missing required parameters: " + String.join(", ", missingParameters));
        }
    }

    private void doAnnotate(@NotNull JtePsiParamName element, @NotNull AnnotationHolder holder) {
        doAnnotateMissingParameterAssignment(element, holder);
        doAnnotateUnknownParameters(element, holder);
    }

    private void doAnnotateMissingParameterAssignment(@NotNull JtePsiParamName element, @NotNull AnnotationHolder holder) {
        JtePsiJavaInjection injection = JtePsiUtil.getNextSiblingIfBefore(element, JtePsiJavaInjection.class, JtePsiComma.class);
        if (injection == null) {
            JtePsiContent content = JtePsiUtil.getNextSiblingIfBefore(element, JtePsiContent.class, JtePsiComma.class);
            if (content == null) {
                addError(holder, "Missing parameter assignment");
            }
        }
    }

    private void doAnnotateUnknownParameters(@NotNull JtePsiParamName element, @NotNull AnnotationHolder holder) {
        PsiElement parent = element.getParent();
        if (!(parent instanceof JtePsiTemplate)) {
            return;
        }

        JtePsiTemplateName templateName = JtePsiUtil.getLastChildOfType(parent, JtePsiTemplateName.class);
        if (templateName == null) {
            return;
        }

        PsiFile templateFile = templateName.resolveFile();
        if (templateFile == null) {
            return;
        }

        if (KteTemplateSignatureService.isKteTemplate(templateFile)) {
            if (!JteLanguage.INSTANCE.equals(element.getLanguage())) {
                return;
            }

            KteTemplateSignatureService.TemplateSignature signature = KteTemplateSignatureService.resolve(templateFile);
            if (signature.parameter(element.getName()) == null) {
                addError(holder, "Unknown parameter " + element.getName());
            }
            return;
        }

        Set<String> availableParameterNames = JtePsiUtil.resolveAvailableParameterNames(templateFile);
        if (!availableParameterNames.contains(element.getName())) {
            addError(holder, "Unknown parameter " + element.getName());
        }
    }

    private void addError(@NotNull AnnotationHolder holder, String message) {
        holder.newAnnotation(HighlightSeverity.ERROR, message).create();
    }

    private void addWarning(@NotNull AnnotationHolder holder, String message) {
        holder.newAnnotation(HighlightSeverity.WARNING, message).create();
    }

    private void addWeakWarning(@NotNull AnnotationHolder holder, String message) {
        holder.newAnnotation(HighlightSeverity.WEAK_WARNING, message).create();
    }
}
