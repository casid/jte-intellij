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
import java.util.concurrent.atomic.AtomicBoolean;

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
            TreeSet<String> names = collectImportNames(jteFile);
            names.add(qualifiedName);
            rewriteImports(jteFile, document, names);
            PsiDocumentManager.getInstance(project).commitDocument(document);
        }, jteFile);
    }

    /**
     * Removes {@code @import} statements that are no longer referenced from any injected Java
     * fragment, keeping the remaining imports sorted. Wildcard and static imports are kept as-is
     * since usage cannot be determined reliably.
     */
    static void removeUnusedImports(@NotNull PsiFile jteFile) {
        Project project = jteFile.getProject();
        Document document = PsiDocumentManager.getInstance(project).getDocument(jteFile);
        if (document == null) {
            return;
        }

        TreeSet<String> names = collectImportNames(jteFile);
        names.removeIf(name -> !isImportUsed(jteFile, name));

        if (rewriteImports(jteFile, document, names)) {
            PsiDocumentManager.getInstance(project).commitDocument(document);
        }
    }

    private static TreeSet<String> collectImportNames(@NotNull PsiFile jteFile) {
        TreeSet<String> names = new TreeSet<>();
        for (JtePsiImport existingImport : PsiTreeUtil.findChildrenOfType(jteFile, JtePsiImport.class)) {
            names.add(extractQualifiedName(existingImport.getText()));
        }
        return names;
    }

    static boolean isImportUsed(@NotNull PsiFile jteFile, @NotNull String importedName) {
        if (importedName.startsWith("static ") || importedName.endsWith(".*")) {
            return true;
        }

        String simpleName = importedName.substring(importedName.lastIndexOf('.') + 1);
        InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(jteFile.getProject());

        AtomicBoolean used = new AtomicBoolean(false);
        for (PsiLanguageInjectionHost host : PsiTreeUtil.findChildrenOfType(jteFile, PsiLanguageInjectionHost.class)) {
            injectedLanguageManager.enumerate(host, (injectedPsi, places) -> {
                if (PsiTreeUtil.findChildrenOfType(injectedPsi, PsiJavaCodeReferenceElement.class).stream()
                        .anyMatch(ref -> ref.getQualifier() == null && simpleName.equals(ref.getReferenceName()))) {
                    used.set(true);
                }
            });

            if (used.get()) {
                break;
            }
        }

        return used.get();
    }

    /**
     * Rewrites the leading block of {@code @import} statements to match the given (sorted) set of
     * names, ensuring a blank line separates it from a following {@code @param} block. Returns
     * whether the document was changed.
     */
    private static boolean rewriteImports(@NotNull PsiFile jteFile, @NotNull Document document, @NotNull TreeSet<String> names) {
        List<JtePsiImport> imports = PsiTreeUtil.findChildrenOfType(jteFile, JtePsiImport.class).stream()
                .sorted(Comparator.comparingInt(element -> element.getTextRange().getStartOffset()))
                .toList();

        boolean hasParams = PsiTreeUtil.findChildOfType(jteFile, JtePsiParam.class) != null;

        StringBuilder replacement = new StringBuilder();
        for (String name : names) {
            replacement.append(IMPORT_PREFIX).append(name).append('\n');
        }
        if (hasParams && !names.isEmpty()) {
            replacement.append('\n');
        }

        int startOffset = 0;
        int endOffset = 0;
        if (!imports.isEmpty()) {
            startOffset = imports.getFirst().getTextRange().getStartOffset();
            endOffset = skipLineBreaks(document, imports.getLast().getTextRange().getEndOffset());
        }

        if (document.getCharsSequence().subSequence(startOffset, endOffset).toString().equals(replacement.toString())) {
            return false;
        }

        document.replaceString(startOffset, endOffset, replacement);
        return true;
    }

    static String extractQualifiedName(@NotNull String importText) {
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
