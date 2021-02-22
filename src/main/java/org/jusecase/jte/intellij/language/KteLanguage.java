package org.jusecase.jte.intellij.language;

import com.intellij.lang.Language;
import com.intellij.psi.templateLanguages.TemplateLanguage;

public class KteLanguage extends Language implements TemplateLanguage {
    public static final KteLanguage INSTANCE = new KteLanguage();

    private KteLanguage() {
        super("KotlinTemplateEngine");
    }
}
