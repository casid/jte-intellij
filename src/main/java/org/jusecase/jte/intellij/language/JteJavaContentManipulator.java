package org.jusecase.jte.intellij.language;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.psi.JtePsiImport;
import org.jusecase.jte.intellij.language.psi.JtePsiJavaContent;
import org.jusecase.jte.intellij.language.psi.JtePsiJavaInjection;

import java.util.Collection;

public class JteJavaContentManipulator extends AbstractElementManipulator<JtePsiJavaContent> {
    @Nullable
    @Override
    public JtePsiJavaContent handleContentChange(@NotNull JtePsiJavaContent element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
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

    private void optimizeImportsAfterContentManipulation(@NotNull Document document, @NotNull JtePsiJavaContent element, String newContent) {
        if (isRequiredToOptimizeImports(element, newContent)) {
            ApplicationManager.getApplication().invokeLater(() -> {
                //noinspection DialogTitleCapitalization
                WriteCommandAction.runWriteCommandAction(element.getProject(), "Optimize jte Imports", null, () -> {
                    String optimizedText = document.getText().replace(";import ", "\n@import ");
                    document.setText(optimizedText);
                }, element.getContainingFile());
            });
        }
    }

    private boolean isRequiredToOptimizeImports(@NotNull JtePsiJavaContent element, String newContent) {
        if (!newContent.contains(";import ")) {
            return false;
        }

        Collection<JtePsiImport> jtePsiImports = PsiTreeUtil.findChildrenOfType(element, JtePsiImport.class);
        for (JtePsiImport jtePsiImport : jtePsiImports) {
            JtePsiJavaInjection javaInjection = PsiTreeUtil.findChildOfType(jtePsiImport, JtePsiJavaInjection.class);
            if (javaInjection == null) {
                continue;
            }

            String text = javaInjection.getText();
            if (!text.contains(";import ")) {
                continue;
            }

            String[] classes = text.split(";import ");
            if (classes.length < 2) {
                continue;
            }

            int startSearchIndex = javaInjection.getTextOffset() + text.length();

            for (int i = 1; i < classes.length; ++i) {
                if (newContent.indexOf(classes[i], startSearchIndex) != -1) {
                    return false;
                }
            }

            break;
        }

        return true;
    }
}
