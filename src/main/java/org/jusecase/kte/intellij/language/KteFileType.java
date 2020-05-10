package org.jusecase.kte.intellij.language;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class KteFileType extends LanguageFileType {

    public static final KteFileType INSTANCE = new KteFileType();

    private KteFileType() {
        super(KteLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Kotlin Template Engine File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "A file of the Kotlin Template Engine.";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "kte";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return KteIcons.ICON;
    }
}
