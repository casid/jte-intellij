package org.jusecase.jte.intellij.language;

import com.intellij.lang.Language;
import com.intellij.psi.templateLanguages.TemplateDataElementType;
import org.jusecase.jte.intellij.language.parsing.TokenTypes;

public class JteTemplateDataElementType extends TemplateDataElementType {
    public JteTemplateDataElementType(Language language, TokenTypes tokenTypes) {
        super("JTE_TEMPLATE_DATA_HTML", language, tokenTypes.HTML_CONTENT(), tokenTypes.OUTER_ELEMENT_TYPE());
    }
}
