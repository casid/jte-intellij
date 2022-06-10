package org.jusecase.jte.intellij.language.file;

import com.intellij.ide.actions.CreateFileAction;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.JteIcons;

public class CreateJteFileAction extends CreateFileAction {
    public CreateJteFileAction() {
        super("JTE Template", "Create JTE Template", JteIcons.ICON);
    }

    @Override
    protected @Nullable String getDefaultExtension() {
        return "jte";
    }
}
