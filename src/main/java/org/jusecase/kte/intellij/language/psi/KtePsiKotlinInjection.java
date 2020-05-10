package org.jusecase.kte.intellij.language.psi;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class KtePsiKotlinInjection extends KtePsiElement {
    public KtePsiKotlinInjection(@NotNull ASTNode node) {
        super(node);
    }
}
