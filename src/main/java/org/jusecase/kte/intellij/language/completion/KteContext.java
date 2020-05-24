package org.jusecase.kte.intellij.language.completion;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class KteContext extends TemplateContextType {
    protected KteContext() {
        super("kte", "kte");
    }

    @Override
    public boolean isInContext(@NotNull PsiFile file, int offset) {
        return file.getName().endsWith(".kte");
    }
}
