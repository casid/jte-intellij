package org.jusecase.kte.intellij.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.SharedPsiElementImplUtil;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiFileReference;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class KtePsiTagOrLayoutName extends KtePsiElement {
    public KtePsiTagOrLayoutName(@NotNull ASTNode node) {
        super(node);
    }

    abstract String getIdentifier();

    private boolean matchesParent(PsiDirectory parent, KtePsiTagOrLayoutName prevName) {
        if (parent == null) {
            return false;
        }

        if (prevName == null) {
            return getIdentifier().equals(parent.getName());
        }

        if (!prevName.getText().equals(parent.getName())) {
            return false;
        }

        return matchesParent(parent.getParent(), PsiTreeUtil.getPrevSiblingOfType(prevName, getClass()));
    }

    @Override
    public PsiReference getReference() {
        if (isDirectory()) {
            return resolveDirectoryReference();
        } else {
            return resolveFileReference();
        }
    }

    @Nullable
    private PsiReference resolveDirectoryReference() {
        return null; // TODO
    }

    @Nullable
    private PsiReference resolveFileReference() {
        KtePsiTagOrLayoutName prevName = PsiTreeUtil.getPrevSiblingOfType(this, getClass());

        PsiFile[] filesByName = FilenameIndex.getFilesByName(getProject(), getText() + ".kte", GlobalSearchScope.allScope(getProject()));
        for (PsiFile psiFile : filesByName) {
            if (matchesParent(psiFile.getParent(), prevName)) {
                return new PsiFileReference() {

                    @NotNull
                    @Override
                    public PsiElement getElement() {
                        return KtePsiTagOrLayoutName.this;
                    }

                    @NotNull
                    @Override
                    public TextRange getRangeInElement() {
                        return TextRange.from(0, getTextLength());
                    }

                    @Override
                    public PsiElement resolve() {
                        return psiFile;
                    }

                    @NotNull
                    @Override
                    public String getCanonicalText() {
                        return getText();
                    }

                    @Override
                    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
                        return null; // TODO ???
                    }

                    @Override
                    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
                        return null; // TODO ???
                    }

                    @Override
                    public boolean isReferenceTo(@NotNull PsiElement element) {
                        return element == psiFile;
                    }

                    @Override
                    public boolean isSoft() {
                        return false; // TODO ???
                    }

                    @NotNull
                    @Override
                    public ResolveResult[] multiResolve(boolean incompleteCode) {
                        return new ResolveResult[0]; // TODO ???
                    }
                };
            }
        }

        return null;
    }

    private boolean isDirectory() {
        return getNextSibling() instanceof KtePsiNameSeparator;
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return SharedPsiElementImplUtil.getReferences(this);
    }
}
