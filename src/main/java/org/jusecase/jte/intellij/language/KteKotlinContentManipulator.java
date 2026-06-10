package org.jusecase.jte.intellij.language;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.psi.KtePsiJavaContent;
import org.jusecase.jte.intellij.language.refactoring.KteNativeTemplateSourceEditUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Source-edit hook for IntelliJ element manipulation on the parsed .kte host PSI.
 * This is intentionally separate from K2 semantic analysis: it only applies text edits to the
 * template file and repairs the historical collapsed-import shape produced by some content edits.
 */
public class KteKotlinContentManipulator extends AbstractElementManipulator<KtePsiJavaContent> {

    private static final Pattern BROKEN_IMPORT = Pattern.compile("(@import\\s*\\S*)(import\\s*\\S*\\n)");

    @Nullable
    @Override
    public KtePsiJavaContent handleContentChange(@NotNull KtePsiJavaContent element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        PsiFile file = element.getContainingFile();
        KteNativeTemplateSourceEditUtil.replace(file, range, newContent);
        Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(file);
        if (document != null) {
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
                    String optimizedText = document.getText().replace(importReplacement.oldText(), importReplacement.newText());
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

    record ImportReplacement(@NotNull String oldText, @NotNull String newText) {
    }
}
