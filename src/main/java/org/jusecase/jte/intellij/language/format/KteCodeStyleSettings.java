package org.jusecase.jte.intellij.language.format;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

public class KteCodeStyleSettings extends CustomCodeStyleSettings {
    protected KteCodeStyleSettings(CodeStyleSettings container) {
        super("KteCodeStyleSettings", container);
    }
}
