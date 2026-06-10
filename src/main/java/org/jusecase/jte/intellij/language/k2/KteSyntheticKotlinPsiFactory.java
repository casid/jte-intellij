package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtPsiFactory;

public final class KteSyntheticKotlinPsiFactory {
    private KteSyntheticKotlinPsiFactory() {
    }

    @NotNull
    public static KtFile createKtFile(@NotNull Project project, @NotNull KteSyntheticKotlinFile syntheticFile) {
        return createKtFile(project, syntheticFile, null);
    }

    @NotNull
    public static KtFile createKtFile(@NotNull Project project,
                                      @NotNull KteSyntheticKotlinFile syntheticFile,
                                      @Nullable PsiElement analysisContext) {
        PsiFile psiFile;
        if (analysisContext == null) {
            psiFile = PsiFileFactory.getInstance(project).createFileFromText(
                    syntheticFile.getFileName(),
                    KotlinLanguage.INSTANCE,
                    syntheticFile.getText()
            );
        } else {
            psiFile = KtPsiFactory.Companion.contextual(analysisContext, false, false)
                    .createFile(syntheticFile.getFileName(), syntheticFile.getText());
        }

        if (psiFile instanceof KtFile ktFile) {
            configureAnalysisContext(project, ktFile, analysisContext);
            return ktFile;
        }

        throw new IllegalStateException("Synthetic Kotlin text did not produce a KtFile: " + psiFile.getClass().getName());
    }

    static void configureAnalysisContext(@NotNull Project project,
                                         @NotNull KtFile ktFile,
                                         @Nullable PsiElement analysisContext) {
        KteSyntheticKotlinModuleContext.configure(project, ktFile, analysisContext);
    }
}
