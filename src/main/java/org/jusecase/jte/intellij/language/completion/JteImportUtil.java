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
import org.jusecase.jte.intellij.language.psi.JtePsiImport;
import org.jusecase.jte.intellij.language.psi.JtePsiParam;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Shared logic for resolving unresolved references inside jte's injected Java fragments and
 * adding the corresponding @import statement directly to the host .jte document, bypassing the
 * broken shortenClassReferences/ImportHelper pipeline.
 */
final class JteImportUtil {

    private static final String IMPORT_KEYWORD = "@import";
    private static final String IMPORT_PREFIX = IMPORT_KEYWORD + " ";

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

        WriteCommandAction.runWriteCommandAction(project, "Add Import", null, () -> {
            insertImportSorted(jteFile, document, qualifiedName);
            PsiDocumentManager.getInstance(project).commitDocument(document);
        }, jteFile);
    }

    /**
     * Rewrites the leading block of {@code @import} statements in sorted order, including the
     * new import, and ensures a blank line separates it from a following {@code @param} block.
     */
    private static void insertImportSorted(@NotNull PsiFile jteFile, @NotNull Document document, @NotNull String qualifiedName) {
        List<JtePsiImport> imports = PsiTreeUtil.findChildrenOfType(jteFile, JtePsiImport.class).stream()
                .sorted(Comparator.comparingInt(element -> element.getTextRange().getStartOffset()))
                .toList();

        TreeSet<String> sortedNames = new TreeSet<>();
        for (JtePsiImport existingImport : imports) {
            sortedNames.add(extractQualifiedName(existingImport.getText()));
        }
        sortedNames.add(qualifiedName);

        boolean hasParams = PsiTreeUtil.findChildOfType(jteFile, JtePsiParam.class) != null;

        StringBuilder replacement = new StringBuilder();
        for (String name : sortedNames) {
            replacement.append(IMPORT_PREFIX).append(name).append('\n');
        }
        if (hasParams) {
            replacement.append('\n');
        }

        int startOffset = 0;
        int endOffset = 0;
        if (!imports.isEmpty()) {
            startOffset = imports.getFirst().getTextRange().getStartOffset();
            endOffset = skipLineBreaks(document, imports.getLast().getTextRange().getEndOffset());
        }

        document.replaceString(startOffset, endOffset, replacement);
    }

    private static String extractQualifiedName(@NotNull String importText) {
        return importText.substring(IMPORT_KEYWORD.length()).trim();
    }

    private static int skipLineBreaks(@NotNull Document document, int offset) {
        CharSequence text = document.getCharsSequence();
        while (offset < text.length() && (text.charAt(offset) == '\n' || text.charAt(offset) == '\r')) {
            offset++;
        }
        return offset;
    }
}
