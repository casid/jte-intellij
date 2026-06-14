package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.daemon.ReferenceImporter;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.psi.JtePsiFile;

import java.util.function.BooleanSupplier;

/**
 * Implements IntelliJ's "Add unambiguous imports on the fly" for jte's injected Java fragments,
 * using the same host-document insertion as JteAddImportIntention.
 */
public class JteReferenceImporter implements ReferenceImporter {

    @Override
    public boolean isAddUnambiguousImportsOnTheFlyEnabled(@NotNull PsiFile file) {
        if (!(file instanceof JtePsiFile)) {
            return false;
        }

        return CodeInsightSettings.getInstance().ADD_UNAMBIGIOUS_IMPORTS_ON_THE_FLY;
    }

    @Override
    public @Nullable BooleanSupplier computeAutoImportAtOffset(@NotNull Editor editor, @NotNull PsiFile file, int offset, boolean allowCaretNearReference) {
        if (!(file instanceof JtePsiFile)) {
            return null;
        }

        Project project = file.getProject();
        PsiElement injectedElement = InjectedLanguageManager.getInstance(project).findInjectedElementAt(file, offset);

        PsiJavaCodeReferenceElement reference = JteImportUtil.findUnresolvedReference(project, injectedElement);
        if (reference == null) {
            return null;
        }

        PsiClass[] candidates = JteImportUtil.resolveCandidates(project, reference);
        if (candidates.length != 1) {
            return null;
        }

        String qualifiedName = candidates[0].getQualifiedName();
        if (qualifiedName == null) {
            return null;
        }

        PsiFile injectedFile = reference.getContainingFile();

        return () -> {
            JteImportUtil.insertImport(project, injectedFile, qualifiedName);
            return true;
        };
    }
}
