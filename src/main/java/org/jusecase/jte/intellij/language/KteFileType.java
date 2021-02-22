package org.jusecase.jte.intellij.language;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class KteFileType extends LanguageFileType {

    public static final KteFileType INSTANCE = new KteFileType();

    private KteFileType() {
        super(JteLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Kotlin Template Engine File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Kotlin Template Engine File";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "kte";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return JteIcons.ICON;
    }
}
