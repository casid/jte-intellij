package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import java.io.IOException;
import java.util.List;

public final class KteSyntheticKotlinAnalysisContextService {
    private static final String SYNTHETIC_DIRECTORY_NAME = ".jte-synthetic";

    private final Project project;

    public KteSyntheticKotlinAnalysisContextService(@NotNull Project project) {
        this.project = project;
    }

    @NotNull
    public static KteSyntheticKotlinAnalysisContextService getInstance(@NotNull Project project) {
        return project.getService(KteSyntheticKotlinAnalysisContextService.class);
    }

    @NotNull
    public GlobalSearchScope resolveSearchScope(@NotNull PsiFile templateFile) {
        VirtualFile virtualFile = templateFile.getVirtualFile();
        if (virtualFile != null) {
            Module module = ProjectRootManager.getInstance(project)
                    .getFileIndex()
                    .getModuleForFile(virtualFile);
            if (module != null) {
                return GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
            }
        }

        return GlobalSearchScope.allScope(project);
    }

    @Nullable
    public VirtualFile findModuleSourceRoot(@NotNull PsiFile templateFile) {
        VirtualFile templateVirtualFile = templateFile.getVirtualFile();
        if (templateVirtualFile == null) {
            return null;
        }

        Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(templateVirtualFile);
        if (module == null) {
            return null;
        }

        List<VirtualFile> sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots(JavaSourceRootType.SOURCE);
        if (sourceRoots.isEmpty()) {
            return null;
        }

        return sourceRoots.stream()
                .filter(sourceRoot -> sourceRoot.getPath().endsWith("/kotlin"))
                .findFirst()
                .or(() -> sourceRoots.stream()
                        .filter(sourceRoot -> sourceRoot.getPath().endsWith("/java"))
                        .findFirst())
                .or(() -> sourceRoots.stream()
                        .filter(sourceRoot -> !VfsUtilCore.isAncestor(sourceRoot, templateVirtualFile, false))
                        .findFirst())
                .orElse(sourceRoots.getFirst());
    }

    @NotNull
    public PsiElement findAnalysisContext(@NotNull PsiFile templateFile, @Nullable VirtualFile sourceRoot) {
        if (sourceRoot == null) {
            return templateFile;
        }

        PsiDirectory sourceDirectory = PsiManager.getInstance(project).findDirectory(sourceRoot);
        return sourceDirectory == null ? templateFile : sourceDirectory;
    }

    @NotNull
    public VirtualFile resolveSyntheticDirectory(@NotNull VirtualFile sourceRoot) throws IOException {
        VirtualFile directory = sourceRoot.findChild(SYNTHETIC_DIRECTORY_NAME);
        if (directory != null && directory.isDirectory()) {
            return directory;
        } else if (directory != null) {
            throw new IOException("Synthetic Kotlin path exists but is not a directory: " + directory.getPath());
        }

        return VfsUtil.createDirectoryIfMissing(sourceRoot, SYNTHETIC_DIRECTORY_NAME);
    }
}
