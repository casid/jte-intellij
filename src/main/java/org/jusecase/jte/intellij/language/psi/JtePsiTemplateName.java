package org.jusecase.jte.intellij.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.SharedPsiElementImplUtil;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiFileReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JtePsiTemplateName extends JtePsiElement implements PsiNamedElement {

    public static final String JTE_ROOT = ".jteroot";

    private final String extension;

    public JtePsiTemplateName(@NotNull ASTNode node, String extension) {
        super(node);
        this.extension = extension;
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
        PsiDirectory rootDirectory = findRootDirectory();
        if (rootDirectory == null) {
            return null;
        }

        JtePsiTemplateName nextSibling = JtePsiUtil.getFirstSiblingOfSameType(this, JtePsiTemplateName.class);
        if (nextSibling.getName() == null) {
            return null;
        }

        PsiDirectory nextDirectory = rootDirectory.findSubdirectory(nextSibling.getName());

        while (nextDirectory != null) {
            if (nextSibling == this) {
                if (getText().equals(nextDirectory.getName())) {
                    return createDirectoryReference(nextDirectory);
                } else {
                    return null;
                }
            }

            nextSibling = PsiTreeUtil.getNextSiblingOfType(nextSibling, JtePsiTemplateName.class);
            if (nextSibling == null || nextSibling.getName() == null) {
                return null;
            }
            nextDirectory = nextDirectory.findSubdirectory(nextSibling.getName());
        }

        return null;
    }

    private PsiReference createDirectoryReference(PsiDirectory directory) {
        return new PsiReference() {
            @NotNull
            @Override
            public PsiElement getElement() {
                return JtePsiTemplateName.this;
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
                return JtePsiTemplateName.this;
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
        PsiFile file = resolveFile();
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
                return JtePsiTemplateName.this;
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
                return ResolveResult.EMPTY_ARRAY; // TODO ???
            }
        };
    }

    public PsiFile resolveFile() {
        PsiDirectory rootDirectory = findRootDirectory();
        if (rootDirectory == null) {
            return null;
        }

        VirtualFile virtualFile = resolveVirtualFile(rootDirectory);
        if (virtualFile == null) {
            return null;
        }

        PsiManager psiManager = PsiManager.getInstance(getProject());
        return psiManager.findFile(virtualFile);
    }

    private VirtualFile resolveVirtualFile(PsiDirectory rootDirectory) {
        VirtualFile virtualFile = resolveVirtualFile(rootDirectory, extension);
        if (virtualFile != null) {
            return virtualFile;
        }

        return resolveVirtualFile(rootDirectory, ".jte".equals(extension) ? ".kte" : ".jte");
    }

    private VirtualFile resolveVirtualFile(PsiDirectory rootDirectory, String extension) {
        String relativePath = getRelativePath(extension);

        return rootDirectory.getVirtualFile().findFileByRelativePath(relativePath);
    }

    @NotNull
    private String getRelativePath(String extension) {
        List<JtePsiTemplateName> names = new ArrayList<>();

        names.add(this);

        JtePsiTemplateName prevName = this;
        while ((prevName = PsiTreeUtil.getPrevSiblingOfType(prevName, getClass())) != null) {
            names.add(prevName);
        }

        Collections.reverse(names);

        String path = names.stream().map(JtePsiTemplateName::getName).collect(Collectors.joining("/"));
        if (!isDirectory()) {
            path += extension;
        }

        return path;
    }

    public boolean isDirectory() {
        return getNextSibling() instanceof JtePsiNameSeparator;
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
        JtePsiUtil.rename(this, name);
        return this;
    }

    @Nullable
    public PsiDirectory findRootDirectory() {
        return findRootDirectory(getContainingFile().getOriginalFile().getParent());
    }

    @Nullable
    private PsiDirectory findRootDirectory(@Nullable PsiDirectory parent) {
        if (parent == null) {
            return null;
        }

        if (parent.findFile(JTE_ROOT) != null) {
            return parent;
        }

        return findRootDirectory(parent.getParent());
    }
}

