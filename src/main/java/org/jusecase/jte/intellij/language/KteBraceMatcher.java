package org.jusecase.jte.intellij.language;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.parsing.KteTokenTypes;

public class KteBraceMatcher implements PairedBraceMatcher {

    private final BracePair[] pairs = new BracePair[]{
            new BracePair(KteTokenTypes.IF, KteTokenTypes.ENDIF, true),
            new BracePair(KteTokenTypes.FOR, KteTokenTypes.ENDFOR, true),
            new BracePair(KteTokenTypes.CONTENT_BEGIN, KteTokenTypes.CONTENT_END, true),
    };

    @NotNull
    @Override
    public BracePair @NotNull [] getPairs() {
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
