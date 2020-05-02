package org.jusecase.jte.intellij.language;

import com.intellij.lang.Language;
import com.intellij.psi.templateLanguages.TemplateDataElementType;

import static org.jusecase.jte.intellij.language.parsing.JteTokenTypes.HTML_CONTENT;
import static org.jusecase.jte.intellij.language.parsing.JteTokenTypes.OUTER_ELEMENT_TYPE;

public class JteTemplateDataElementType extends TemplateDataElementType {
    public JteTemplateDataElementType(Language language) {
        super("JTE_TEMPLATE_DATA_HTML", language, HTML_CONTENT, OUTER_ELEMENT_TYPE);
    }
}
