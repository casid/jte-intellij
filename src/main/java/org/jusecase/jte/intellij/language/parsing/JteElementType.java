package org.jusecase.jte.intellij.language.parsing;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.JteLanguage;

public class JteElementType extends IElementType {
    public JteElementType(@NotNull @NonNls String debugName) {
        super(debugName, JteLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "Jte " + super.toString();
    }
}