package org.jusecase.jte.intellij.language.refactoring;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

public class JteSearchScope extends GlobalSearchScope {

    private final GlobalSearchScope projectScope;

    public JteSearchScope(Project project) {
        projectScope = GlobalSearchScope.projectScope(project);
    }

    @Override
    public boolean isSearchInModuleContent(@NotNull Module aModule) {
        return true;
    }

    @Override
    public boolean isSearchInLibraries() {
        return false;
    }

    @Override
    public boolean contains(@NotNull VirtualFile file) {
        if (file.getName().endsWith(".jte")) {
            return projectScope.contains(file);
        }

        return false;
    }
}
