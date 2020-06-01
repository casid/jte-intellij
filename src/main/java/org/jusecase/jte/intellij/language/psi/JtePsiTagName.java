package org.jusecase.jte.intellij.language.psi;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class JtePsiTagName extends JtePsiTagOrLayoutName {
    public JtePsiTagName(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getIdentifier() {
        return "tag";
    }
}