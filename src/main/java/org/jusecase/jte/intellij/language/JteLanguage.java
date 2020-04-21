package org.jusecase.jte.intellij.language;

import com.intellij.lang.Language;
import com.intellij.psi.templateLanguages.TemplateLanguage;

public class JteLanguage extends Language implements TemplateLanguage {
    public static final JteLanguage INSTANCE = new JteLanguage();

    private JteLanguage() {
        super("JavaTemplateEngine");
    }
}
