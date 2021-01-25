package org.jusecase.jte.intellij.language.format;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.JteLanguage;

public class JteLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
    @Override
    public @Nullable String getCodeSample(@NotNull SettingsType settingsType) {
        return "@import foo.Page\n" +
                "\n" +
                "@param Page page\n" +
                "@param verbose = false\n" +
                "\n" +
                "<div>\n" +
                "@if(verbose)\n" +
                "${page.getVerboseTitle()}\n" +
                "@else\n" +
                "${page.getTitle()}\n" +
                "@endif\n" +
                "</div>\n";
    }

    @Override
    public @NotNull Language getLanguage() {
        return JteLanguage.INSTANCE;
    }

    @Override
    public @Nullable IndentOptionsEditor getIndentOptionsEditor() {
        return new IndentOptionsEditor();
    }
}
