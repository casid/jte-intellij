package org.jusecase.jte.intellij.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.SharedPsiElementImplUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.k2.KteTemplateSignatureService;

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

        JtePsiTemplateName templateName = PsiTreeUtil.getPrevSiblingOfType(this, JtePsiTemplateName.class);
        if (templateName == null) {
            return null;
        }

        PsiFile templateFile = templateName.resolveFile();
        if (templateFile == null) {
            return null;
        }

        KteTemplateSignatureService.TemplateSignature signature = KteTemplateSignatureService.resolve(templateFile);
        KteTemplateSignatureService.Parameter parameter = signature.parameter(name);
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

    private PsiReference createReferenceFor(KteTemplateSignatureService.Parameter parameter) {
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
                return parameter.sourceElement();
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
                return element == parameter.sourceElement() ||
                        element.getManager().areElementsEquivalent(element, parameter.sourceElement());
            }

            @Override
            public boolean isSoft() {
                return false;
            }
        };
    }
}
