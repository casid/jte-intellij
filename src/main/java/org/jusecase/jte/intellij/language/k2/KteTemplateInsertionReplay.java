package org.jusecase.jte.intellij.language.k2;

import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.OffsetMap;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementDecorator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

final class KteTemplateInsertionReplay {
    private static final String COMPLETION_DUMMY_IDENTIFIER = CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED;

    private final PsiFile templateFile;
    private final KteSyntheticKotlinFile syntheticFile;
    private final KtFile ktFile;
    private final int kotlinOffset;
    private final int hostOffset;
    private final Consumer<String> debugSink;

    KteTemplateInsertionReplay(@NotNull PsiFile templateFile,
                               @NotNull KteSyntheticKotlinFile syntheticFile,
                               @NotNull KtFile ktFile,
                               int kotlinOffset,
                               int hostOffset,
                               @NotNull Consumer<String> debugSink) {
        this.templateFile = templateFile;
        this.syntheticFile = syntheticFile;
        this.ktFile = ktFile;
        this.kotlinOffset = kotlinOffset;
        this.hostOffset = hostOffset;
        this.debugSink = debugSink;
    }

    @NotNull
    LookupElement wrap(@NotNull LookupElement element) {
        return LookupElementDecorator.withInsertHandler(element, (context, item) ->
                handleInsert(context, element, item.getLookupString()));
    }

    void handleInsert(@NotNull InsertionContext context,
                      @NotNull LookupElement element,
                      @NotNull String lookupString) {
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(templateFile.getProject());
        Document templateDocument = documentManager.getDocument(templateFile);
        if (templateDocument == null) {
            return;
        }

        ReplayResult replayResult = replayNativeSyntheticInsertion(context, element, lookupString, documentManager, templateDocument);
        List<TemplateEdit> edits = replayResult.edits();
        if (edits.isEmpty() && replayResult.allowSimpleFallback()) {
            TemplateEdit fallbackEdit = simpleTemplateEdit(templateDocument, lookupString);
            edits = fallbackEdit == null ? List.of() : List.of(fallbackEdit);
        }
        if (edits.isEmpty()) {
            debugSink.accept("templateInsert rejected lookup=" + lookupString + " reason=" + replayResult.reason());
            return;
        }

        debugSink.accept("templateInsert lookup=" + lookupString +
                " edits=" + describeEdits(edits) +
                " before=" + excerpt(templateDocument.getText(), Math.min(hostOffset, templateDocument.getTextLength())));
        int caretOffset = caretOffsetAfterEdits(edits);
        List<TemplateEdit> applicationEdits = new ArrayList<>(edits);
        applicationEdits.sort(Comparator.comparingInt((TemplateEdit edit) -> edit.range().getStartOffset()).reversed());
        for (TemplateEdit edit : applicationEdits) {
            templateDocument.replaceString(edit.range().getStartOffset(), edit.range().getEndOffset(), edit.text());
        }
        documentManager.commitDocument(templateDocument);
        if (context.getEditor().getDocument() == templateDocument) {
            context.getEditor().getCaretModel().moveToOffset(Math.clamp(caretOffset, 0, templateDocument.getTextLength()));
        }
    }

    @NotNull
    private ReplayResult replayNativeSyntheticInsertion(@NotNull InsertionContext context,
                                                        @NotNull LookupElement element,
                                                        @NotNull String lookupString,
                                                        @NotNull PsiDocumentManager documentManager,
                                                        @NotNull Document templateDocument) {
        Document syntheticDocument = ktFile.getViewProvider().getDocument();
        if (syntheticDocument == null) {
            syntheticDocument = documentManager.getDocument(ktFile);
        }
        if (syntheticDocument == null || syntheticDocument == templateDocument) {
            debugSink.accept("templateInsert native replay skipped: no isolated synthetic document");
            return ReplayResult.fallbackAllowed("no isolated synthetic document");
        }

        String syntheticBefore = syntheticFile.getText();
        if (!syntheticBefore.contentEquals(syntheticDocument.getCharsSequence())) {
            syntheticDocument.setText(syntheticBefore);
        }

        int insertionStart = identifierStart(syntheticDocument.getCharsSequence(), kotlinOffset);
        int insertionEnd = identifierEnd(syntheticDocument.getCharsSequence(), kotlinOffset);

        Editor syntheticEditor = EditorFactory.getInstance().createEditor(syntheticDocument, templateFile.getProject());
        try {
            syntheticDocument.replaceString(insertionStart, insertionEnd, lookupString);
            int tailOffset = insertionStart + lookupString.length();
            documentManager.commitDocument(syntheticDocument);
            syntheticEditor.getCaretModel().moveToOffset(tailOffset);
            OffsetMap offsetMap = new OffsetMap(syntheticDocument);
            offsetMap.addOffset(CompletionInitializationContext.START_OFFSET, insertionStart);
            offsetMap.addOffset(CompletionInitializationContext.SELECTION_END_OFFSET, insertionStart);
            offsetMap.addOffset(CompletionInitializationContext.IDENTIFIER_END_OFFSET, tailOffset);
            InsertionContext syntheticContext = new InsertionContext(
                    offsetMap,
                    context.getCompletionChar(),
                    context.getElements(),
                    ktFile,
                    syntheticEditor,
                    true
            );
            element.handleInsert(syntheticContext);
            documentManager.commitDocument(syntheticDocument);
        } catch (ProcessCanceledException exception) {
            throw exception;
        } catch (RuntimeException | AssertionError | LinkageError exception) {
            debugSink.accept("templateInsert native replay failed: " +
                    exception.getClass().getName() + ": " + exception.getMessage());
            return ReplayResult.fallbackAllowed("native replay failed");
        } finally {
            EditorFactory.getInstance().releaseEditor(syntheticEditor);
        }

        String syntheticAfter = syntheticDocument.getText();
        if (syntheticBefore.equals(syntheticAfter)) {
            return ReplayResult.fallbackAllowed("native replay produced no synthetic edits");
        }

        List<TemplateEdit> editsWithImports = editsFromSyntheticDiffWithAddedImports(syntheticBefore, syntheticAfter, templateDocument);
        if (editsWithImports != null) {
            return ReplayResult.success(editsWithImports, "native replay mapped with imports");
        }

        TemplateEdit edit = editFromSyntheticDiff(syntheticBefore, syntheticAfter, templateDocument);
        if (edit != null) {
            return ReplayResult.success(List.of(edit), "native replay mapped");
        }

        debugSink.accept("templateInsert native replay unmapped beforeLength=" +
                syntheticBefore.length() + " afterLength=" + syntheticAfter.length() +
                " before=" + excerpt(syntheticBefore, Math.min(kotlinOffset, syntheticBefore.length())) +
                " after=" + excerpt(syntheticAfter, Math.min(kotlinOffset, syntheticAfter.length())));
        return ReplayResult.rejected("native replay touched generated or unmapped synthetic Kotlin");
    }

    @Nullable
    private TemplateEdit editFromSyntheticDiff(@NotNull String syntheticBefore,
                                               @NotNull String syntheticAfter,
                                               @NotNull Document templateDocument) {
        if (syntheticBefore.equals(syntheticAfter)) {
            return null;
        }

        int commonPrefix = commonPrefixLength(syntheticBefore, syntheticAfter);
        int commonSuffix = commonSuffixLength(syntheticBefore, syntheticAfter, commonPrefix);
        int syntheticOldEnd = syntheticBefore.length() - commonSuffix;
        int syntheticNewEnd = syntheticAfter.length() - commonSuffix;
        if (commonPrefix > syntheticOldEnd || commonPrefix > syntheticNewEnd) {
            return null;
        }

        TextRange syntheticOldRange = new TextRange(commonPrefix, syntheticOldEnd);
        String replacement = syntheticAfter.substring(commonPrefix, syntheticNewEnd);
        TextRange templateRange = mapSyntheticEditRangeToTemplate(syntheticOldRange, templateDocument);
        return templateRange == null ? null : normalizeForCurrentTemplateDocument(templateDocument, templateRange, replacement);
    }

    @Nullable
    private List<TemplateEdit> editsFromSyntheticDiffWithAddedImports(@NotNull String syntheticBefore,
                                                                      @NotNull String syntheticAfter,
                                                                      @NotNull Document templateDocument) {
        AddedSyntheticImports addedImports = removeAddedSyntheticImports(syntheticBefore, syntheticAfter);
        if (addedImports.imports().isEmpty()) {
            return null;
        }

        TemplateEdit sourceEdit = editFromSyntheticDiff(syntheticBefore, addedImports.syntheticTextWithoutAddedImports(), templateDocument);
        if (sourceEdit == null && !syntheticBefore.equals(addedImports.syntheticTextWithoutAddedImports())) {
            return null;
        }

        List<TemplateEdit> result = new ArrayList<>();
        if (sourceEdit != null) {
            result.add(sourceEdit);
        }
        TemplateEdit importEdit = templateImportEdit(templateDocument, addedImports.imports());
        if (importEdit != null) {
            result.add(importEdit);
        }
        return result.isEmpty() ? null : result;
    }

    @NotNull
    private AddedSyntheticImports removeAddedSyntheticImports(@NotNull String syntheticBefore,
                                                              @NotNull String syntheticAfter) {
        List<String> beforeImports = syntheticImportLines(syntheticBefore).stream()
                .map(ImportLine::text)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        StringBuilder strippedAfter = new StringBuilder(syntheticAfter);
        List<String> addedImports = new ArrayList<>();
        int removedLength = 0;
        for (ImportLine afterImport : syntheticImportLines(syntheticAfter)) {
            if (beforeImports.remove(afterImport.text())) {
                continue;
            }

            addedImports.add(afterImport.text().substring("import ".length()).trim());
            int startOffset = afterImport.range().getStartOffset() - removedLength;
            int endOffset = afterImport.range().getEndOffset() - removedLength;
            strippedAfter.delete(startOffset, endOffset);
            removedLength += afterImport.range().getLength();
        }

        return new AddedSyntheticImports(
                addedImports,
                removeUnexpectedBlankLineAfterSyntheticImports(syntheticBefore, strippedAfter.toString())
        );
    }

    @Nullable
    private TemplateEdit templateImportEdit(@NotNull Document templateDocument,
                                            @NotNull List<String> syntheticImports) {
        Set<String> missingImports = new LinkedHashSet<>();
        String templateText = templateDocument.getText();
        for (String syntheticImport : syntheticImports) {
            if (!hasTemplateImport(templateText, syntheticImport)) {
                missingImports.add(syntheticImport);
            }
        }
        if (missingImports.isEmpty()) {
            return null;
        }

        StringBuilder text = new StringBuilder();
        for (String missingImport : missingImports) {
            text.append("@import ").append(missingImport).append('\n');
        }
        return new TemplateEdit(new TextRange(templateImportInsertionOffset(templateText), templateImportInsertionOffset(templateText)),
                text.toString(),
                "native-replay-import");
    }

    @Nullable
    private TextRange mapSyntheticEditRangeToTemplate(@NotNull TextRange syntheticOldRange,
                                                      @NotNull Document templateDocument) {
        int dummyStart = syntheticFile.getText().indexOf(COMPLETION_DUMMY_IDENTIFIER);
        int dummyEnd = dummyStart < 0 ? -1 : dummyStart + COMPLETION_DUMMY_IDENTIFIER.length();
        boolean replacesCompletionDummy = dummyStart >= 0 &&
                syntheticOldRange.getStartOffset() <= dummyStart &&
                syntheticOldRange.getEndOffset() >= dummyEnd;

        TextRange mappedTemplateRange = syntheticFile.mapKotlinSourceEditRangeToTemplate(syntheticOldRange);
        if (mappedTemplateRange == null) {
            return null;
        }

        int templateEnd = replacesCompletionDummy
                ? Math.min(hostOffset, templateDocument.getTextLength())
                : mappedTemplateRange.getEndOffset();
        int safeStart = Math.clamp(mappedTemplateRange.getStartOffset(), 0, templateDocument.getTextLength());
        int safeEnd = Math.clamp(templateEnd, safeStart, templateDocument.getTextLength());
        return new TextRange(safeStart, safeEnd);
    }

    @NotNull
    private TemplateEdit normalizeForCurrentTemplateDocument(@NotNull Document templateDocument,
                                                             @NotNull TextRange sourceRange,
                                                             @NotNull String desiredText) {
        String templateText = templateDocument.getText();
        int start = Math.clamp(sourceRange.getStartOffset(), 0, templateText.length());
        int end = Math.clamp(sourceRange.getEndOffset(), start, templateText.length());
        String currentRangeText = templateText.substring(start, end);
        if (desiredText.startsWith(currentRangeText)) {
            String remainingDesiredText = desiredText.substring(currentRangeText.length());
            int duplicatedLookupLength = matchingPrefixLengthAt(templateText, end, desiredText);
            int remainingLength = matchingPrefixLengthAt(templateText, end, remainingDesiredText);
            end += Math.max(duplicatedLookupLength, remainingLength);
        }

        String currentEditText = templateText.substring(start, end);
        String mode = currentEditText.equals(desiredText) ? "native-replay-noop" : "native-replay";
        return new TemplateEdit(new TextRange(start, end), desiredText, mode);
    }

    @Nullable
    private TemplateEdit simpleTemplateEdit(@NotNull Document document, @NotNull String lookupString) {
        CharSequence syntheticText = syntheticFile.getText();
        int startOffset = identifierStart(syntheticText, kotlinOffset);
        int endOffset = identifierEnd(syntheticText, kotlinOffset);
        TextRange templateRange = mapSyntheticEditRangeToTemplate(new TextRange(startOffset, endOffset), document);
        if (templateRange == null) {
            debugSink.accept("templateInsert simple fallback skipped: completion range is not source-backed");
            return null;
        }

        return normalizeForCurrentTemplateDocument(document, templateRange, lookupString);
    }

    @NotNull
    private static List<ImportLine> syntheticImportLines(@NotNull String text) {
        List<ImportLine> result = new ArrayList<>();
        int offset = 0;
        while (offset < text.length()) {
            int lineTextEnd = lineTextEnd(text, offset);
            int nextLineOffset = nextLineOffset(text, lineTextEnd);
            String line = text.substring(offset, lineTextEnd);
            if (line.isBlank()) {
                offset = nextLineOffset;
                continue;
            }
            if (!line.startsWith("import ")) {
                break;
            }

            result.add(new ImportLine(line, new TextRange(offset, nextLineOffset)));
            offset = nextLineOffset;
        }
        return result;
    }

    @NotNull
    private static String removeUnexpectedBlankLineAfterSyntheticImports(@NotNull String syntheticBefore,
                                                                         @NotNull String strippedAfter) {
        int offset = syntheticImportBlockEnd(strippedAfter);
        while (startsWithLineBreak(strippedAfter, offset) && !startsWithLineBreak(syntheticBefore, offset)) {
            int nextOffset = nextLineOffset(strippedAfter, offset);
            strippedAfter = strippedAfter.substring(0, offset) + strippedAfter.substring(nextOffset);
        }
        return strippedAfter;
    }

    private static int syntheticImportBlockEnd(@NotNull String text) {
        int offset = 0;
        boolean sawImport = false;
        while (offset < text.length()) {
            int lineTextEnd = lineTextEnd(text, offset);
            int nextLineOffset = nextLineOffset(text, lineTextEnd);
            String line = text.substring(offset, lineTextEnd);
            if (line.isBlank()) {
                if (!sawImport) {
                    break;
                }
                offset = nextLineOffset;
                continue;
            }
            if (!line.startsWith("import ")) {
                break;
            }
            sawImport = true;
            offset = nextLineOffset;
        }
        return offset;
    }

    private static boolean hasTemplateImport(@NotNull String templateText, @NotNull String importText) {
        String expectedLine = "@import " + importText;
        int offset = 0;
        while (offset < templateText.length()) {
            int lineTextEnd = lineTextEnd(templateText, offset);
            if (expectedLine.equals(templateText.substring(offset, lineTextEnd).trim())) {
                return true;
            }
            offset = nextLineOffset(templateText, lineTextEnd);
        }
        return false;
    }

    private static int templateImportInsertionOffset(@NotNull String templateText) {
        int result = 0;
        int offset = 0;
        while (offset < templateText.length()) {
            int lineTextEnd = lineTextEnd(templateText, offset);
            int nextLineOffset = nextLineOffset(templateText, lineTextEnd);
            if (templateText.substring(offset, lineTextEnd).trim().startsWith("@import ")) {
                result = nextLineOffset;
            }
            offset = nextLineOffset;
        }
        return result;
    }

    private static int matchingPrefixLengthAt(@NotNull String text, int offset, @NotNull String expectedPrefix) {
        int result = 0;
        while (result < expectedPrefix.length() &&
                offset + result < text.length() &&
                text.charAt(offset + result) == expectedPrefix.charAt(result)) {
            result++;
        }
        return result;
    }

    private static int commonPrefixLength(@NotNull String first, @NotNull String second) {
        int max = Math.min(first.length(), second.length());
        int index = 0;
        while (index < max && first.charAt(index) == second.charAt(index)) {
            index++;
        }
        return index;
    }

    private static int commonSuffixLength(@NotNull String first, @NotNull String second, int commonPrefix) {
        int firstIndex = first.length() - 1;
        int secondIndex = second.length() - 1;
        int length = 0;
        while (firstIndex >= commonPrefix &&
                secondIndex >= commonPrefix &&
                first.charAt(firstIndex) == second.charAt(secondIndex)) {
            firstIndex--;
            secondIndex--;
            length++;
        }
        return length;
    }

    private static int identifierStart(@NotNull CharSequence text, int offset) {
        int index = Math.min(offset, text.length());
        while (index > 0 && Character.isJavaIdentifierPart(text.charAt(index - 1))) {
            index--;
        }
        return index;
    }

    private static int identifierEnd(@NotNull CharSequence text, int offset) {
        int index = Math.min(offset, text.length());
        while (index < text.length() && Character.isJavaIdentifierPart(text.charAt(index))) {
            index++;
        }
        return index;
    }

    @NotNull
    private static String describeEdits(@NotNull List<TemplateEdit> edits) {
        return edits.stream()
                .map(edit -> edit.mode() + ":" + edit.range().getStartOffset() + ".." + edit.range().getEndOffset() +
                        "='" + sanitize(edit.text()) + "'")
                .toList()
                .toString();
    }

    private static int caretOffsetAfterEdits(@NotNull List<TemplateEdit> edits) {
        TemplateEdit primaryEdit = edits.get(0);
        int offset = primaryEdit.range().getStartOffset() + primaryEdit.text().length();
        for (int index = 1; index < edits.size(); index++) {
            TemplateEdit edit = edits.get(index);
            if (edit.range().getStartOffset() <= primaryEdit.range().getStartOffset()) {
                offset += edit.text().length() - edit.range().getLength();
            }
        }
        return offset;
    }

    private static int lineTextEnd(@NotNull String text, int offset) {
        int result = offset;
        while (result < text.length() && text.charAt(result) != '\n' && text.charAt(result) != '\r') {
            result++;
        }
        return result;
    }

    private static int nextLineOffset(@NotNull String text, int lineTextEnd) {
        int result = lineTextEnd;
        if (result < text.length() && text.charAt(result) == '\r') {
            result++;
        }
        if (result < text.length() && text.charAt(result) == '\n') {
            result++;
        }
        return result;
    }

    private static boolean startsWithLineBreak(@NotNull String text, int offset) {
        return offset < text.length() && (text.charAt(offset) == '\n' || text.charAt(offset) == '\r');
    }

    @NotNull
    private static String excerpt(@NotNull String text, int offset) {
        int start = Math.max(0, offset - 80);
        int end = Math.min(text.length(), offset + 80);
        return sanitize(text.substring(start, offset)) + "<caret>" + sanitize(text.substring(offset, end));
    }

    @NotNull
    private static String sanitize(@NotNull String text) {
        return text.replace("\n", "\\n").replace("\r", "\\r");
    }

    private record ReplayResult(@NotNull List<TemplateEdit> edits,
                                boolean allowSimpleFallback,
                                @NotNull String reason) {
        @NotNull
        private static ReplayResult success(@NotNull List<TemplateEdit> edits, @NotNull String reason) {
            return new ReplayResult(edits, false, reason);
        }

        @NotNull
        private static ReplayResult fallbackAllowed(@NotNull String reason) {
            return new ReplayResult(List.of(), true, reason);
        }

        @NotNull
        private static ReplayResult rejected(@NotNull String reason) {
            return new ReplayResult(List.of(), false, reason);
        }
    }

    private record TemplateEdit(@NotNull TextRange range, @NotNull String text, @NotNull String mode) {
    }

    private record ImportLine(@NotNull String text, @NotNull TextRange range) {
    }

    private record AddedSyntheticImports(@NotNull List<String> imports,
                                         @NotNull String syntheticTextWithoutAddedImports) {
    }
}
