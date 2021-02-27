package org.jusecase.jte.intellij.language.format;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleConfigurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.JteLanguage;
import org.jusecase.jte.intellij.language.KteLanguage;

@SuppressWarnings("DialogTitleCapitalization")
public class KteCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
    @Override
    public @Nullable CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings) {
        return new KteCodeStyleSettings(settings);
    }

    @Override
    public @Nullable String getConfigurableDisplayName() {
        return "kte";
    }

    @Override
    public @NotNull CodeStyleConfigurable createConfigurable(@NotNull CodeStyleSettings settings, @NotNull CodeStyleSettings modelSettings) {
        return new CodeStyleAbstractConfigurable(settings, modelSettings, getConfigurableDisplayName()) {
            @Override
            protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
                return new KteCodeStyleMainPanel(getCurrentSettings(), settings);
            }
        };
    }

    private static class KteCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {

        public KteCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
            super(KteLanguage.INSTANCE, currentSettings, settings);
        }

    }
}
