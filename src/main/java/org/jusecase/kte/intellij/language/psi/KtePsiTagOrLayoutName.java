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

public abstract class KtePsiTagOrLayoutName extends KtePsiElement implements PsiNamedElement {
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
        KtePsiTagOrLayoutName fileElement = resolveFileElement(this);
        if (fileElement == null) {
            return null;
        }

        PsiFile file = resolveFile(fileElement);
        if (file == null) {
            return null;
        }

        KtePsiTagOrLayoutName prevSibling = PsiTreeUtil.getPrevSiblingOfType(fileElement, KtePsiTagOrLayoutName.class);
        PsiDirectory prevDirectory = file.getParent();

        while (prevSibling != null && prevDirectory != null) {
            if (prevSibling == this) {
                if (getText().equals(prevDirectory.getName())) {
                    return createDirectoryReference(prevDirectory);
                } else {
                    return null;
                }
            }

            prevSibling = PsiTreeUtil.getPrevSiblingOfType(prevSibling, KtePsiTagOrLayoutName.class);
            prevDirectory = prevDirectory.getParent();
        }


        return null;
    }

    private PsiReference createDirectoryReference(PsiDirectory directory) {
        return new PsiReference() {
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

            @Nullable
            @Override
            public PsiElement resolve() {
                return directory;
            }

            @NotNull
            @Override
            public String getCanonicalText() {
                return getText();
            }

            @Override
            public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
                setName(newElementName);
                return KtePsiTagOrLayoutName.this;
            }

            @Override
            public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
                return null; // TODO ???
            }

            @Override
            public boolean isReferenceTo(@NotNull PsiElement element) {
                return element == directory;
            }

            @Override
            public boolean isSoft() {
                return false;
            }
        };
    }

    @Nullable
    private PsiReference resolveFileReference() {
        PsiFile file = resolveFile(this);
        if (file != null) {
            return createFileReference(file);
        }

        return null;
    }

    @NotNull
    private PsiReference createFileReference(PsiFile file) {
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
                return file;
            }

            @NotNull
            @Override
            public String getCanonicalText() {
                return getText();
            }

            @Override
            public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
                return setName(getFileNameWithoutExtension(newElementName));
            }

            @Override
            public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
                return null; // TODO ???
            }

            @Override
            public boolean isReferenceTo(@NotNull PsiElement element) {
                return element == file;
            }

            @Override
            public boolean isSoft() {
                return false;
            }

            @NotNull
            @Override
            public ResolveResult[] multiResolve(boolean incompleteCode) {
                return new ResolveResult[0]; // TODO ???
            }
        };
    }

    public PsiFile resolveFile() {
        return resolveFile(this);
    }

    private PsiFile resolveFile(KtePsiTagOrLayoutName fileElement) {
        KtePsiTagOrLayoutName prevName = PsiTreeUtil.getPrevSiblingOfType(fileElement, getClass());

        PsiFile[] filesByName = FilenameIndex.getFilesByName(getProject(), fileElement.getText() + ".kte", GlobalSearchScope.allScope(getProject()));
        for (PsiFile psiFile : filesByName) {
            if (matchesParent(psiFile.getParent(), prevName)) {
                return psiFile;
            }
        }

        return null;
    }

    private boolean isDirectory() {
        return getNextSibling() instanceof KtePsiNameSeparator;
    }

    public KtePsiTagOrLayoutName resolveFileElement(KtePsiTagOrLayoutName element) {
        KtePsiTagOrLayoutName sibling = PsiTreeUtil.getNextSiblingOfType(element, KtePsiTagOrLayoutName.class);
        if (sibling == null) {
            return element;
        } else {
            return resolveFileElement(sibling);
        }
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return SharedPsiElementImplUtil.getReferences(this);
    }

    private String getFileNameWithoutExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return fileName;
        }

        return fileName.substring(0, index);
    }

    @Override
    public String getName() {
        return getText();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        KtePsiUtil.rename(this, name);
        return this;
    }
}
