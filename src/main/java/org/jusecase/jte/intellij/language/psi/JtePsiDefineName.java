package org.jusecase.jte.intellij.language.psi;

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
import org.jusecase.jte.intellij.language.psi.JtePsiElement;

import java.util.Collection;

public class JtePsiDefineName extends JtePsiElement {
    public JtePsiDefineName(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        Collection<JtePsiRenderName> renderNames = getRenderNames();
        if (renderNames == null) {
            return null;
        }

        for (JtePsiRenderName renderName : renderNames) {
            if (getText().equals(renderName.getName())) {
                return createReferenceFor(renderName);
            }
        }

        return super.getReference();
    }

    @Nullable
    public Collection<JtePsiRenderName> getRenderNames() {
        JtePsiLayout layout = PsiTreeUtil.getParentOfType(this, JtePsiLayout.class);
        if (layout == null) {
            return null;
        }

        JtePsiLayoutName firstLayoutName = PsiTreeUtil.getChildOfType(layout, JtePsiLayoutName.class);
        if (firstLayoutName == null) {
            return null;
        }

        JtePsiTagOrLayoutName layoutFileName = firstLayoutName.resolveFileElement(firstLayoutName);
        if (layoutFileName == null) {
            return null;
        }

        PsiFile psiFile = layoutFileName.resolveFile();
        if (psiFile == null) {
            return null;
        }

        return PsiTreeUtil.findChildrenOfType(psiFile, JtePsiRenderName.class);
    }

    private PsiReference createReferenceFor(JtePsiRenderName renderName) {
        return new PsiReference() {
            @NotNull
            @Override
            public PsiElement getElement() {
                return JtePsiDefineName.this;
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
                JtePsiUtil.rename(JtePsiDefineName.this, newElementName);
                return JtePsiDefineName.this;
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
