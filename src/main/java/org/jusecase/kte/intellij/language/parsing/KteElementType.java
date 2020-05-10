package org.jusecase.kte.intellij.language.parsing;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jusecase.kte.intellij.language.KteLanguage;

public class KteElementType extends IElementType {
    public KteElementType(@NotNull @NonNls String debugName) {
        super(debugName, KteLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "Kte " + super.toString();
    }
}