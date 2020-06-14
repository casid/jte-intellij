package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;
import org.jusecase.jte.intellij.language.psi.JtePsiParamName;
import org.jusecase.jte.intellij.language.psi.JtePsiTagOrLayoutName;
import org.jusecase.jte.intellij.language.psi.JtePsiUtil;

import java.util.Set;
import java.util.stream.Collectors;

public class JteTagOrLayoutParamCompletionProvider extends CompletionProvider<CompletionParameters> {
    private final boolean java;

    public JteTagOrLayoutParamCompletionProvider(boolean java) {
        this.java = java;
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement jteElement = getPsiElement(parameters);
        if (jteElement == null) {
            return;
        }

        if (jteElement.getNode().getElementType() == JteTokenTypes.PARAM_NAME) {
            // The user already started typing, that's okay
            jteElement = jteElement.getParent();
        } else {
            PsiElement prevSibling = JtePsiUtil.getPrevSiblingIgnoring(jteElement, JteTokenTypes.WHITESPACE);
            if (prevSibling == null) {
                return;
            }
            IElementType elementType = prevSibling.getNode().getElementType();
            // !(prevSibling instanceof JtePsiParamsBegin) && !(prevSibling instanceof JtePsiComma) && !(prevSibling instanceof JtePsiJavaInjection)
            if (elementType != JteTokenTypes.PARAMS_BEGIN && elementType != JteTokenTypes.COMMA && elementType != JteTokenTypes.JAVA_INJECTION) {
                return;
            }
        }

        JtePsiTagOrLayoutName tagOrLayoutName = JtePsiUtil.getFirstSiblingOfType(jteElement, JtePsiTagOrLayoutName.class);
        if (tagOrLayoutName == null) {
            return;
        }

        PsiFile tagOrLayoutFile = tagOrLayoutName.resolveFile();
        if (tagOrLayoutFile == null) {
            return;
        }

        PsiParameterList parameterList = JtePsiUtil.resolveParameterList(tagOrLayoutFile);
        if (parameterList == null) {
            return;
        }

        Set<String> usedNames = PsiTreeUtil.findChildrenOfType(tagOrLayoutName.getParent(), JtePsiParamName.class).stream().map(JtePsiParamName::getName).collect(Collectors.toSet());
        for (PsiParameter parameter : parameterList.getParameters()) {
            if (!usedNames.contains(parameter.getName())) {
                result.addElement(LookupElementBuilder.create(parameter.getName() + " = "));
            }
        }
    }

    @Nullable
    private PsiElement getPsiElement(@NotNull CompletionParameters parameters) {
        if (java) {
            return getJavaPsiElement(parameters);
        } else {
            return getJtePsiElement(parameters);
        }
    }

    @Nullable
    private PsiElement getJtePsiElement(CompletionParameters parameters) {
        return parameters.getOriginalPosition();
    }

    @Nullable
    private PsiElement getJavaPsiElement(@NotNull CompletionParameters parameters) {
        PsiElement fileContext = FileContextUtil.getFileContext(parameters.getOriginalFile());
        if (fileContext == null) {
            return null;
        }

        PsiElement originalPosition = parameters.getOriginalPosition();
        if (originalPosition == null) {
            return null;
        }

        int injectionOffsetInMasterFile = InjectedLanguageManager.getInstance(fileContext.getProject()).injectedToHost(originalPosition, parameters.getOffset());
        return fileContext.getContainingFile().findElementAt(injectionOffsetInMasterFile);
    }
}
