package org.jusecase.jte.intellij.language.format;

import com.intellij.psi.PsiFile;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;
import org.jusecase.jte.intellij.language.psi.JtePsiFile;

public class JteFormattingModelBuilder extends FormattingModelBuilderBase {

    protected JteFormattingModelBuilder() {
        super(JteTokenTypes.INSTANCE);
    }

    @Override
    protected boolean isTemplateFile(PsiFile file) {
        return file instanceof JtePsiFile;
    }
}
