package org.jusecase.jte.intellij.language;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.psi.KtePsiJavaContent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KteKotlinContentManipulator extends AbstractElementManipulator<KtePsiJavaContent> {

    private static final Pattern BROKEN_IMPORT = Pattern.compile("(@import\\s*\\S*)(import\\s*\\S*\\n)");

    @Nullable
    @Override
    public KtePsiJavaContent handleContentChange(@NotNull KtePsiJavaContent element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        Document document = FileDocumentManager.getInstance().getDocument(element.getContainingFile().getVirtualFile());
        if (document != null) {
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(element.getProject());
            if (documentManager.isDocumentBlockedByPsi(document)) {
                documentManager.doPostponedOperationsAndUnblockDocument(document);
            }

            document.replaceString(range.getStartOffset(), range.getEndOffset(), newContent);
            documentManager.commitDocument(document);

            optimizeImportsAfterContentManipulation(document, element, newContent);
        }

        return element;
    }

    private void optimizeImportsAfterContentManipulation(@NotNull Document document, @NotNull KtePsiJavaContent element, String newContent) {
        ImportReplacement importReplacement = isRequiredToOptimizeImports(newContent);

        if (importReplacement != null) {
            ApplicationManager.getApplication().invokeLater(() -> {
                //noinspection DialogTitleCapitalization
                WriteCommandAction.runWriteCommandAction(element.getProject(), "Optimize kte Imports", null, () -> {
                    String optimizedText = document.getText().replace(importReplacement.oldText, importReplacement.newText);
                    document.setText(optimizedText);
                }, element.getContainingFile());
            });
        }
    }

    static ImportReplacement isRequiredToOptimizeImports(String newContent) {
        Matcher matcher = BROKEN_IMPORT.matcher(newContent);
        if (matcher.find()) {
            String oldText = matcher.group();
            String import1 = matcher.group(1);
            String import2 = matcher.group(2);

            return new ImportReplacement(oldText, import1 + "\n@" + import2);
        }

        return null;
    }

    static class ImportReplacement {
        final String oldText;
        final String newText;

        public ImportReplacement(String oldText, String newText) {
            this.oldText = oldText;
            this.newText = newText;
        }
    }
}