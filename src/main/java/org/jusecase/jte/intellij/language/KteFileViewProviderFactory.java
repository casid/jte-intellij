package org.jusecase.jte.intellij.language;

import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.parsing.KteTokenTypes;

public class KteFileViewProviderFactory implements FileViewProviderFactory {
    @NotNull
    @Override
    public FileViewProvider createFileViewProvider(@NotNull VirtualFile file, Language language, @NotNull PsiManager manager, boolean eventSystemEnabled) {
        return new JteFileViewProvider(manager, file, eventSystemEnabled, KteLanguage.INSTANCE, KteTokenTypes.INSTANCE);
    }
}
