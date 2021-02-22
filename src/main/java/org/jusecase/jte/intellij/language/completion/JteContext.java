package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class JteContext extends TemplateContextType {
    protected JteContext() {
        super("jte", "jte");
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
        String fileName = templateActionContext.getFile().getName();
        return fileName.endsWith(".jte") || fileName.endsWith(".kte");
    }
}
