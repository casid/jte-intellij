package org.jusecase.jte.intellij.language.psi;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class JtePsiLayoutName extends JtePsiTagOrLayoutName {
    public JtePsiLayoutName(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getIdentifier() {
        return "layout";
    }
}
