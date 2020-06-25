package org.jusecase.jte.intellij.language;

import com.intellij.codeInsight.daemon.impl.analysis.HighlightVisitorImpl;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;

public class JteHighlightVisitor extends HighlightVisitorImpl {
    @Override
    public boolean suitableForFile(@NotNull PsiFile file) {
        if (super.suitableForFile(file)) {
            return false;
        }

        return file.getName().endsWith(".jte") && file instanceof PsiJavaFile;
    }

    @NotNull
    @Override
    public HighlightVisitorImpl clone() {
        return new JteHighlightVisitor();
    }
}
