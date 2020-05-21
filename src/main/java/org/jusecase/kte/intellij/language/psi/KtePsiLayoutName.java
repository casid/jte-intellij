package org.jusecase.kte.intellij.language.psi;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class KtePsiLayoutName extends KtePsiTagOrLayoutName {
    public KtePsiLayoutName(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    String getIdentifier() {
        return "layout";
    }
}
