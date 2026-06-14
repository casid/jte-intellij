package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtFile;

public final class KteSyntheticKotlinModel {
    private final KteSyntheticKotlinFile syntheticFile;
    private final KtFile ktFile;
    private final VirtualFile sourceRoot;

    public KteSyntheticKotlinModel(@NotNull KteSyntheticKotlinFile syntheticFile,
                                   @NotNull KtFile ktFile,
                                   @Nullable VirtualFile sourceRoot) {
        this.syntheticFile = syntheticFile;
        this.ktFile = ktFile;
        this.sourceRoot = sourceRoot;
    }

    @NotNull
    public KteSyntheticKotlinFile getSyntheticFile() {
        return syntheticFile;
    }

    @NotNull
    public KtFile getKtFile() {
        return ktFile;
    }

    @Nullable
    public VirtualFile getSourceRoot() {
        return sourceRoot;
    }
}
