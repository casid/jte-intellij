package org.jusecase.kte.intellij.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.SharedPsiElementImplUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class KtePsiDefineName extends KtePsiElement {
    public KtePsiDefineName(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        Collection<KtePsiRenderName> renderNames = getRenderNames();
        if (renderNames == null) {
            return null;
        }

        for (KtePsiRenderName renderName : renderNames) {
            if (getText().equals(renderName.getName())) {
                return createReferenceFor(renderName);
            }
        }

        return super.getReference();
    }

    @Nullable
    public Collection<KtePsiRenderName> getRenderNames() {
        KtePsiLayout layout = PsiTreeUtil.getParentOfType(this, KtePsiLayout.class);
        if (layout == null) {
            return null;
        }

        KtePsiLayoutName firstLayoutName = PsiTreeUtil.getChildOfType(layout, KtePsiLayoutName.class);
        if (firstLayoutName == null) {
            return null;
        }

        KtePsiTagOrLayoutName layoutFileName = firstLayoutName.resolveFileElement(firstLayoutName);
        if (layoutFileName == null) {
            return null;
        }

        PsiFile psiFile = layoutFileName.resolveFile();
        if (psiFile == null) {
            return null;
        }

        return PsiTreeUtil.findChildrenOfType(psiFile, KtePsiRenderName.class);
    }

    private PsiReference createReferenceFor(KtePsiRenderName renderName) {
        return new PsiReference() {
            @NotNull
            @Override
            public PsiElement getElement() {
                return KtePsiDefineName.this;
            }

            @NotNull
            @Override
            public TextRange getRangeInElement() {
                return TextRange.from(0, getTextLength());
            }

            @Nullable
            @Override
            public PsiElement resolve() {
                return renderName;
            }

            @NotNull
            @Override
            public String getCanonicalText() {
                return getText();
            }

            @Override
            public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
                KtePsiUtil.rename(KtePsiDefineName.this, newElementName);
                return KtePsiDefineName.this;
            }

            @Override
            public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
                return null; // TODO ???
            }

            @Override
            public boolean isReferenceTo(@NotNull PsiElement element) {
                return element == renderName;
            }

            @Override
            public boolean isSoft() {
                return false;
            }
        };
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
}
