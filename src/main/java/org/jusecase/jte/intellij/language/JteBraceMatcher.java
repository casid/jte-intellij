package org.jusecase.jte.intellij.language;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class JteBraceMatcher implements PairedBraceMatcher {

    private final BracePair[] pairs = new BracePair[]{
            new BracePair(JteTokenTypes.IF, JteTokenTypes.ENDIF, true),
            new BracePair(JteTokenTypes.FOR, JteTokenTypes.ENDFOR, true),
            new BracePair(JteTokenTypes.RAW, JteTokenTypes.ENDRAW, true),
            new BracePair(JteTokenTypes.CONTENT_BEGIN, JteTokenTypes.CONTENT_END, true),
            new BracePair(JteTokenTypes.OUTPUT_BEGIN, JteTokenTypes.OUTPUT_END, false),
            new BracePair(JteTokenTypes.STATEMENT_BEGIN, JteTokenTypes.STATEMENT_END, false),
    };

    @NotNull
    @Override
    public BracePair[] getPairs() {
        return pairs;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        return false;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
