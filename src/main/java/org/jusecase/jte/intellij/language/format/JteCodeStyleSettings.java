package org.jusecase.jte.intellij.language.format;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

public class JteCodeStyleSettings extends CustomCodeStyleSettings {
    protected JteCodeStyleSettings(CodeStyleSettings container) {
        super("JteCodeStyleSettings", container);
    }
}
