package org.jusecase.jte.intellij.language.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.JteFileType;
import org.jusecase.jte.intellij.language.JteLanguage;

public class JtePsiFile extends PsiFileBase {
    public JtePsiFile(@NotNull FileViewProvider viewProvider) {
        this(viewProvider, JteLanguage.INSTANCE);
    }

    public JtePsiFile(@NotNull FileViewProvider viewProvider, Language lang) {
        super(viewProvider, lang);
    }

    @Override
    @NotNull
    public FileType getFileType() {
        return JteFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "JteFile:" + getName();
    }
}
