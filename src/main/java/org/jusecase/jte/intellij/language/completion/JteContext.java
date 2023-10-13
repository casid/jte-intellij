package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import org.jetbrains.annotations.NotNull;

public class JteContext extends TemplateContextType {
    @SuppressWarnings("DialogTitleCapitalization") // jte is lower case by default
    protected JteContext() {
        super( "jte");
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
        String name = templateActionContext.getFile().getName();
        return name.endsWith(".jte") || name.endsWith(".kte");
    }
}
