package org.jusecase.jte.intellij.language;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JteFileType extends LanguageFileType {

    public static final JteFileType INSTANCE = new JteFileType();

    private JteFileType() {
        super(JteLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Java Template Engine File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Java Template Engine File";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "jte";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return JteIcons.ICON;
    }
}
