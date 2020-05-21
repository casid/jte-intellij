package org.jusecase.kte.intellij.language.psi;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class KtePsiTagName extends KtePsiTagOrLayoutName {
    public KtePsiTagName(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    String getIdentifier() {
        return "tag";
    }
}
