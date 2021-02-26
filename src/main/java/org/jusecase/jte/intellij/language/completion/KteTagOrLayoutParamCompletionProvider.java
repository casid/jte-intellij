package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtParameter;
import org.jetbrains.kotlin.psi.KtParameterList;
import org.jusecase.jte.intellij.language.parsing.KteTokenTypes;
import org.jusecase.jte.intellij.language.psi.*;

import java.util.Set;
import java.util.stream.Collectors;

public class KteTagOrLayoutParamCompletionProvider extends CompletionProvider<CompletionParameters> {
    private final boolean kotlin;

    public KteTagOrLayoutParamCompletionProvider(boolean kotlin) {
        this.kotlin = kotlin;
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement jteElement = getPsiElement(parameters);
        if (jteElement == null) {
            return;
        }

        if (jteElement.getNode().getElementType() == KteTokenTypes.PARAM_NAME) {
            // The user already started typing, that's okay
            jteElement = jteElement.getParent();
        } else {
            PsiElement prevSibling = JtePsiUtil.getPrevSiblingIgnoring(jteElement, KteTokenTypes.WHITESPACE);
            if (prevSibling == null) {
                return;
            }
            IElementType elementType = prevSibling.getNode().getElementType();
            if (elementType != KteTokenTypes.PARAMS_BEGIN && elementType != KteTokenTypes.COMMA && elementType != KteTokenTypes.JAVA_INJECTION) {
                return;
            }
        }

        JtePsiTagName tagOrLayoutName = JtePsiUtil.getFirstSiblingOfType(jteElement, JtePsiTagName.class);
        if (tagOrLayoutName == null) {
            return;
        }

        PsiFile tagOrLayoutFile = tagOrLayoutName.resolveFile();
        if (tagOrLayoutFile == null) {
            return;
        }

        KtParameterList parameterList = KtePsiUtil.resolveParameterList(tagOrLayoutFile);
        if (parameterList == null) {
            return;
        }

        Set<String> usedNames = PsiTreeUtil.findChildrenOfType(tagOrLayoutName.getParent(), JtePsiParamName.class).stream().map(JtePsiParamName::getName).collect(Collectors.toSet());
        for (KtParameter parameter : parameterList.getParameters()) {
            if (parameter.isVarArg()) {
                continue;
            }
            if (!usedNames.contains(parameter.getName())) {
                result.addElement(LookupElementBuilder.create(parameter.getName() + " = "));
            }
        }
    }

    @Nullable
    private PsiElement getPsiElement(@NotNull CompletionParameters parameters) {
        if (kotlin) {
            return getKotlinPsiElement(parameters);
        } else {
            return getJtePsiElement(parameters);
        }
    }

    @Nullable
    private PsiElement getJtePsiElement(CompletionParameters parameters) {
        return parameters.getOriginalPosition();
    }

    @Nullable
    private PsiElement getKotlinPsiElement(@NotNull CompletionParameters parameters) {
        PsiElement fileContext = FileContextUtil.getFileContext(parameters.getOriginalFile());
        if (fileContext == null) {
            return null;
        }

        PsiElement originalPosition = parameters.getPosition();

        int injectionOffsetInMasterFile = InjectedLanguageManager.getInstance(fileContext.getProject()).injectedToHost(originalPosition, parameters.getOffset(), false);
        PsiElement result = fileContext.getContainingFile().findElementAt(injectionOffsetInMasterFile);

        if (result instanceof LeafPsiElement && result.getParent() instanceof JtePsiJavaInjection) {
            return result.getParent();
        }
        return result;
    }
}
