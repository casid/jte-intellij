package org.jusecase.kte.intellij.language.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;
import org.jusecase.kte.intellij.language.KteFileType;
import org.jusecase.kte.intellij.language.KteLanguage;

public class KtePsiFile extends PsiFileBase {
    public KtePsiFile(@NotNull FileViewProvider viewProvider) {
        this(viewProvider, KteLanguage.INSTANCE);
    }

    public KtePsiFile(@NotNull FileViewProvider viewProvider, Language lang) {
        super(viewProvider, lang);
    }

    @Override
    @NotNull
    public FileType getFileType() {
        return KteFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "KteFile:" + getName();
    }
}
