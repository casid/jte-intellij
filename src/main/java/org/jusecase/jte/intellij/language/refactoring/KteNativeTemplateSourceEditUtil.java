package org.jusecase.jte.intellij.language.refactoring;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.KteLanguage;
import org.jusecase.jte.intellij.language.psi.JtePsiImport;
import org.jusecase.jte.intellij.language.psi.JtePsiJavaInjection;
import org.jusecase.jte.intellij.language.psi.KtePsiFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class KteNativeTemplateSourceEditUtil {
    private KteNativeTemplateSourceEditUtil() {
    }

    public static boolean isKteFile(@NotNull PsiFile file) {
        return file instanceof KtePsiFile ||
                file.getViewProvider().getPsi(KteLanguage.INSTANCE) instanceof KtePsiFile;
    }

    public static boolean isValidRange(@NotNull PsiFile file, @NotNull TextRange range) {
        return range.getStartOffset() >= 0 &&
                range.getEndOffset() <= file.getTextLength() &&
                range.getStartOffset() <= range.getEndOffset();
    }

    public static void replace(@NotNull PsiFile file, @NotNull TextRange range, @NotNull String replacement) {
        Document document = document(file);
        if (document == null || !isValidRange(file, range)) {
            return;
        }

        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(file.getProject());
        if (documentManager.isDocumentBlockedByPsi(document)) {
            documentManager.doPostponedOperationsAndUnblockDocument(document);
        }

        document.replaceString(range.getStartOffset(), range.getEndOffset(), replacement);
        documentManager.commitDocument(document);
    }

    public static void delete(@NotNull PsiFile file, @NotNull TextRange range) {
        replace(file, range, "");
    }

    public static void insert(@NotNull PsiFile file, int offset, @NotNull String text) {
        replace(file, TextRange.create(offset, offset), text);
    }

    public static void deleteLine(@NotNull PsiFile file, @NotNull TextRange range) {
        Document document = document(file);
        if (document == null || !isValidRange(file, range)) {
            return;
        }

        delete(file, lineRange(document, range));
    }

    @NotNull
    public static TextRange argumentRemovalRange(@NotNull PsiFile file, @NotNull TextRange argumentRange) {
        if (!isValidRange(file, argumentRange)) {
            return argumentRange;
        }

        String text = file.getText();
        int startOffset = argumentRange.getStartOffset();
        int endOffset = argumentRange.getEndOffset();
        int after = skipWhitespaceForward(text, endOffset);
        if (after < text.length() && text.charAt(after) == ',') {
            return TextRange.create(startOffset, skipWhitespaceForward(text, after + 1));
        }

        int before = skipWhitespaceBackward(text, startOffset);
        if (before > 0 && text.charAt(before - 1) == ',') {
            return TextRange.create(before - 1, endOffset);
        }

        return argumentRange;
    }

    @Nullable
    public static ImportReplacement computeOptimizedImportReplacement(@NotNull PsiFile file) {
        if (!isKteFile(file)) {
            return null;
        }

        ImportBlock importBlock = findTopLevelImportBlock(file);
        if (importBlock == null) {
            return null;
        }

        Set<String> distinctImports = new LinkedHashSet<>(importBlock.imports());
        List<String> sortedImports = new ArrayList<>(distinctImports);
        sortedImports.sort(Comparator.naturalOrder());
        String optimizedText = String.join("\n", sortedImports) + "\n";
        String currentText = importBlock.range().substring(file.getText());
        if (currentText.equals(optimizedText)) {
            return null;
        }

        return new ImportReplacement(importBlock.range(), optimizedText);
    }

    public static boolean optimizeImports(@NotNull PsiFile file) {
        ImportReplacement replacement = computeOptimizedImportReplacement(file);
        if (replacement == null) {
            return false;
        }

        replace(file, replacement.range(), replacement.newText());
        return true;
    }

    public static boolean addImport(@NotNull PsiFile file, @NotNull String qualifiedName) {
        if (!isKteFile(file) || hasImport(file, qualifiedName)) {
            return false;
        }

        ImportBlock importBlock = findLeadingImportBlock(file);
        int offset = importBlock == null ? 0 : importBlock.range().getEndOffset();
        insert(file, offset, "@import " + qualifiedName + "\n");
        optimizeImports(file);
        return true;
    }

    public static boolean replaceImport(@NotNull PsiFile file,
                                        @NotNull TextRange importRange,
                                        @NotNull String qualifiedName) {
        if (!isValidRange(file, importRange)) {
            return false;
        }

        replace(file, importRange, qualifiedName);
        optimizeImports(file);
        return true;
    }

    @Nullable
    private static Document document(@NotNull PsiFile file) {
        return PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
    }

    @Nullable
    private static ImportBlock findTopLevelImportBlock(@NotNull PsiFile file) {
        List<ImportEntry> entries = importEntries(file);
        if (entries.isEmpty() || !textBetween(file, 0, entries.get(0).lineRange().getStartOffset()).isBlank()) {
            return null;
        }

        ImportBlock block = contiguousLeadingImportBlock(file, entries);
        return block != null && block.imports().size() == entries.size() ? block : null;
    }

    @Nullable
    private static ImportBlock findLeadingImportBlock(@NotNull PsiFile file) {
        List<ImportEntry> entries = importEntries(file);
        if (entries.isEmpty() || !textBetween(file, 0, entries.get(0).lineRange().getStartOffset()).isBlank()) {
            return null;
        }

        return contiguousLeadingImportBlock(file, entries);
    }

    @Nullable
    private static ImportBlock contiguousLeadingImportBlock(@NotNull PsiFile file,
                                                           @NotNull List<ImportEntry> entries) {
        List<String> imports = new ArrayList<>();
        TextRange importRange = entries.get(0).lineRange();
        imports.add(entries.get(0).sourceText());

        for (int index = 1; index < entries.size(); index++) {
            ImportEntry previous = entries.get(index - 1);
            ImportEntry current = entries.get(index);
            if (!textBetween(file, previous.lineRange().getEndOffset(), current.lineRange().getStartOffset()).isEmpty()) {
                break;
            }

            imports.add(current.sourceText());
            importRange = TextRange.create(importRange.getStartOffset(), current.lineRange().getEndOffset());
        }

        return new ImportBlock(importRange, imports);
    }

    private static boolean hasImport(@NotNull PsiFile file, @NotNull String qualifiedName) {
        for (ImportEntry entry : importEntries(file)) {
            if (entry.qualifiedName().equals(qualifiedName)) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    private static List<ImportEntry> importEntries(@NotNull PsiFile file) {
        Document document = document(file);
        if (document == null) {
            return List.of();
        }

        List<ImportEntry> result = new ArrayList<>();
        for (JtePsiImport importElement : PsiTreeUtil.findChildrenOfType(file, JtePsiImport.class)) {
            JtePsiJavaInjection injection = PsiTreeUtil.getChildOfType(importElement, JtePsiJavaInjection.class);
            if (injection == null) {
                continue;
            }

            String qualifiedName = injection.getText().trim();
            if (qualifiedName.isEmpty()) {
                continue;
            }

            result.add(new ImportEntry(
                    qualifiedName,
                    "@import " + qualifiedName,
                    importLineRange(document, importElement.getTextRange())
            ));
        }

        result.sort(Comparator.comparingInt(entry -> entry.lineRange().getStartOffset()));
        return result;
    }

    @NotNull
    private static String textBetween(@NotNull PsiFile file, int startOffset, int endOffset) {
        return file.getText().substring(startOffset, endOffset);
    }

    @NotNull
    private static TextRange importLineRange(@NotNull Document document, @NotNull TextRange range) {
        int line = document.getLineNumber(range.getStartOffset());
        int startOffset = document.getLineStartOffset(line);
        int endOffset = line < document.getLineCount() - 1
                ? document.getLineStartOffset(line + 1)
                : document.getLineEndOffset(line);
        return TextRange.create(startOffset, endOffset);
    }

    @NotNull
    private static TextRange lineRange(@NotNull Document document, @NotNull TextRange range) {
        int line = document.getLineNumber(range.getStartOffset());
        int startOffset = document.getLineStartOffset(line);
        int endOffset;
        if (line < document.getLineCount() - 1) {
            endOffset = document.getLineStartOffset(line + 1);
        } else {
            endOffset = document.getLineEndOffset(line);
            if (startOffset > 0) {
                int previousLine = line - 1;
                startOffset = previousLine >= 0 ? document.getLineEndOffset(previousLine) : startOffset;
            }
        }
        return TextRange.create(startOffset, endOffset);
    }

    private static int skipWhitespaceForward(@NotNull String text, int offset) {
        int result = offset;
        while (result < text.length() && Character.isWhitespace(text.charAt(result))) {
            result++;
        }
        return result;
    }

    private static int skipWhitespaceBackward(@NotNull String text, int offset) {
        int result = offset;
        while (result > 0 && Character.isWhitespace(text.charAt(result - 1))) {
            result--;
        }
        return result;
    }

    public record ImportReplacement(@NotNull TextRange range, @NotNull String newText) {
    }

    private record ImportEntry(@NotNull String qualifiedName,
                               @NotNull String sourceText,
                               @NotNull TextRange lineRange) {
    }

    private record ImportBlock(@NotNull TextRange range, @NotNull List<String> imports) {
    }
}
