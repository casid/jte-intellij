package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class JteContext extends TemplateContextType {
    protected JteContext() {
        super("jte", "jte");
    }

    @Override
    public boolean isInContext(@NotNull PsiFile file, int offset) {
        return file.getName().endsWith(".jte");
    }
}
