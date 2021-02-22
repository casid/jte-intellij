package org.jusecase.jte.intellij.language.format;

import com.intellij.psi.PsiFile;
import org.jusecase.jte.intellij.language.parsing.KteTokenTypes;
import org.jusecase.jte.intellij.language.psi.KtePsiFile;

public class KteFormattingModelBuilder extends FormattingModelBuilderBase {

    protected KteFormattingModelBuilder() {
        super(KteTokenTypes.INSTANCE);
    }

    @Override
    protected boolean isTemplateFile(PsiFile file) {
        return file instanceof KtePsiFile;
    }
}
