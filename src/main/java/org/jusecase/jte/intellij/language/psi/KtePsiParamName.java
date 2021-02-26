package org.jusecase.jte.intellij.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.SharedPsiElementImplUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtParameter;
import org.jetbrains.kotlin.psi.KtParameterList;

public class KtePsiParamName extends JtePsiElement {
    public KtePsiParamName(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        String name = getName();
        if (name == null) {
            return null;
        }

        JtePsiTagName tagOrLayoutName = PsiTreeUtil.getPrevSiblingOfType(this, JtePsiTagName.class);
        if (tagOrLayoutName == null) {
            return null;
        }

        PsiFile tagOrLayoutFile = tagOrLayoutName.resolveFile();
        if (tagOrLayoutFile == null) {
            return null;
        }

        KtParameterList parameterList = KtePsiUtil.resolveParameterList(tagOrLayoutFile);
        if (parameterList == null) {
            return null;
        }

        KtParameter parameter = getParameterWithSameName(name, parameterList);
        if (parameter == null) {
            return null;
        }

        return createReferenceFor(parameter);
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return SharedPsiElementImplUtil.getReferences(this);
    }

    @Override
    public String getName() {
        return getText();
    }

    private KtParameter getParameterWithSameName(String name, KtParameterList parameterList) {
        for (KtParameter parameter : parameterList.getParameters()) {
            if (name.equals(parameter.getName())) {
                return parameter;
            }
        }
        return null;
    }

    private PsiReference createReferenceFor(KtParameter parameter) {
        return new PsiReference() {
            @NotNull
            @Override
            public PsiElement getElement() {
                return KtePsiParamName.this;
            }

            @NotNull
            @Override
            public TextRange getRangeInElement() {
                return TextRange.from(0, getTextLength());
            }

            @Nullable
            @Override
            public PsiElement resolve() {
                return parameter;
            }

            @NotNull
            @Override
            public String getCanonicalText() {
                return getText();
            }

            @Override
            public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
                JtePsiUtil.rename(KtePsiParamName.this, newElementName);
                return KtePsiParamName.this;
            }

            @Override
            public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
                return null; // TODO ???
            }

            @Override
            public boolean isReferenceTo(@NotNull PsiElement element) {
                return element == parameter;
            }

            @Override
            public boolean isSoft() {
                return false;
            }
        };
    }
}
