package org.jusecase.kte.intellij.language;

import com.intellij.lang.Language;
import com.intellij.psi.templateLanguages.TemplateDataElementType;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class KteTemplateDataElementType extends TemplateDataElementType {
    public KteTemplateDataElementType(Language language) {
        super("KTE_TEMPLATE_DATA_HTML", language, KteTokenTypes.HTML_CONTENT, KteTokenTypes.OUTER_ELEMENT_TYPE);
    }
}
