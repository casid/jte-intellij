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

@SuppressWarnings("DialogTitleCapitalization")
public class JteCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
    @Override
    public @Nullable CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings) {
        return new JteCodeStyleSettings(settings);
    }

    @Override
    public @Nullable String getConfigurableDisplayName() {
        return "jte";
    }

    @Override
    public @NotNull CodeStyleConfigurable createConfigurable(@NotNull CodeStyleSettings settings, @NotNull CodeStyleSettings modelSettings) {
        return new CodeStyleAbstractConfigurable(settings, modelSettings, getConfigurableDisplayName()) {
            @Override
            protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
                return new JteCodeStyleMainPanel(getCurrentSettings(), settings);
            }
        };
    }

    private static class JteCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {

        public JteCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
            super(JteLanguage.INSTANCE, currentSettings, settings);
        }

    }
}
