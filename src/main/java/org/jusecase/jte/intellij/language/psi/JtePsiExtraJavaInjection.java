package org.jusecase.jte.intellij.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;

public class JtePsiExtraJavaInjection extends JtePsiElement implements PsiLanguageInjectionHost {
    public JtePsiExtraJavaInjection(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean isValidHost() {
        return true;
    }

    @Override
    public PsiLanguageInjectionHost updateText(@NotNull String text) {
        return this;
    }

    @NotNull
    @Override
    public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
        return new LiteralTextEscaper<PsiLanguageInjectionHost>(this) {
            @Override
            public boolean decode(@NotNull TextRange rangeInsideHost, @NotNull StringBuilder outChars) {
                outChars.append(myHost.getText(), rangeInsideHost.getStartOffset(), rangeInsideHost.getEndOffset());
                return true;
            }

            @Override
            public int getOffsetInHost(int offsetInDecoded, @NotNull TextRange rangeInsideHost) {
                int offset = offsetInDecoded + rangeInsideHost.getStartOffset();

                if (offset < rangeInsideHost.getStartOffset()) {
                    return -1;
                }

                if (offset > rangeInsideHost.getEndOffset()) {
                    return -1;
                }

                return offset;
            }

            @Override
            public boolean isOneLine() {
                return true;
            }
        };
    }
}
