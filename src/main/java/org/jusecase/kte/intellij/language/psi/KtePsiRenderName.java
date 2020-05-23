package org.jusecase.kte.intellij.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.SharedPsiElementImplUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KtePsiRenderName extends KtePsiElement implements PsiNameIdentifierOwner {
    public KtePsiRenderName(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return this;
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        KtePsiUtil.rename(this, name);
        return this;
    }

    @Override
    public String getName() {
        return getText();
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return SharedPsiElementImplUtil.getReferences(this);
    }
}
