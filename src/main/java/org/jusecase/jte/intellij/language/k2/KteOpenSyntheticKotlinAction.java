package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.KteLanguage;
import org.jusecase.jte.intellij.language.psi.KtePsiFile;

import java.io.IOException;

public class KteOpenSyntheticKotlinAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        PsiFile templateFile = getKteFile(e);
        if (project == null || templateFile == null) {
            return;
        }

        KteSyntheticKotlinModelService.PhysicalFile physicalFile;
        try {
            physicalFile = KteSyntheticKotlinModelService.getInstance(project).writePhysicalSyntheticFile(templateFile);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write synthetic Kotlin file for " + templateFile.getName(), exception);
        }

        if (physicalFile == null) {
            return;
        }

        KteSyntheticKotlinFile syntheticFile = physicalFile.model().getSyntheticFile();
        int kotlinOffset = resolveSyntheticCaretOffset(e, syntheticFile);
        new OpenFileDescriptor(project, physicalFile.virtualFile(), kotlinOffset).navigate(true);
    }

    private int resolveSyntheticCaretOffset(AnActionEvent e, KteSyntheticKotlinFile syntheticFile) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return 0;
        }

        int templateOffset = editor.getCaretModel().getOffset();
        Integer kotlinOffset = syntheticFile.mapTemplateOffsetToKotlin(templateOffset);
        if (kotlinOffset == null) {
            return 0;
        }

        return Math.max(0, Math.min(kotlinOffset, syntheticFile.getText().length()));
    }

    @Nullable
    private PsiFile getKteFile(@NotNull AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null) {
            return null;
        }

        PsiFile kteFile = psiFile.getViewProvider().getPsi(KteLanguage.INSTANCE);
        if (kteFile instanceof KtePsiFile) {
            return kteFile;
        }

        return psiFile instanceof KtePsiFile ? psiFile : null;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean isKteFile = getKteFile(e) != null;
        e.getPresentation().setEnabledAndVisible(isKteFile);
        e.getPresentation().setDescription("Open generated Kotlin text for the current .kte template");
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
