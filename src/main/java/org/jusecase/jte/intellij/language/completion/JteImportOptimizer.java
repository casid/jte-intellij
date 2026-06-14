package org.jusecase.jte.intellij.language.completion;

import com.intellij.lang.ImportOptimizer;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.psi.JtePsiFile;

/**
 * Removes {@code @import} statements that are no longer referenced from the injected Java
 * fragments and keeps the remaining imports sorted. Backs the "Optimize Imports" action, the
 * "Optimize imports" option in the Reformat dialog, and "Optimize imports on the fly".
 */
public class JteImportOptimizer implements ImportOptimizer {

    @Override
    public boolean supports(@NotNull PsiFile file) {
        return file instanceof JtePsiFile;
    }

    @Override
    public @NotNull Runnable processFile(@NotNull PsiFile file) {
        return () -> JteImportUtil.removeUnusedImports(file);
    }
}
