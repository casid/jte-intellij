package org.jusecase.jte.intellij.language.completion;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.psi.JtePsiFile;

/**
 * Shared logic for resolving unresolved references inside jte's injected Java fragments and
 * adding the corresponding @import statement directly to the host .jte document, bypassing the
 * broken shortenClassReferences/ImportHelper pipeline.
 */
final class JteImportUtil {

    private JteImportUtil() {
        // no instances
    }

    @Nullable
    static PsiJavaCodeReferenceElement findUnresolvedReference(@NotNull Project project, @Nullable PsiElement element) {
        if (element == null) {
            return null;
        }

        PsiFile file = element.getContainingFile();
        if (file == null) {
            return null;
        }

        InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(project);
        if (!injectedLanguageManager.isInjectedFragment(file)) {
            return null;
        }

        if (!(injectedLanguageManager.getTopLevelFile(file) instanceof JtePsiFile)) {
            return null;
        }

        PsiJavaCodeReferenceElement reference = PsiTreeUtil.getParentOfType(element, PsiJavaCodeReferenceElement.class, false);
        if (reference == null || reference.getQualifier() != null || reference.resolve() != null) {
            return null;
        }

        return reference;
    }

    static PsiClass @NotNull [] resolveCandidates(@NotNull Project project, @NotNull PsiJavaCodeReferenceElement reference) {
        String referenceName = reference.getReferenceName();
        if (referenceName == null) {
            return PsiClass.EMPTY_ARRAY;
        }

        return PsiShortNamesCache.getInstance(project).getClassesByName(referenceName, reference.getResolveScope());
    }

    static void insertImport(@NotNull Project project, @NotNull PsiFile injectedFile, @NotNull String qualifiedName) {
        PsiFile jteFile = InjectedLanguageManager.getInstance(project).getTopLevelFile(injectedFile);
        if (jteFile == null) {
            return;
        }

        Document document = PsiDocumentManager.getInstance(project).getDocument(jteFile);
        if (document == null) {
            return;
        }

        String importStatement = "@import " + qualifiedName + "\n";

        WriteCommandAction.runWriteCommandAction(project, "Add Import", null, () -> {
            document.insertString(0, importStatement);
            PsiDocumentManager.getInstance(project).commitDocument(document);
        }, jteFile);
    }
}
