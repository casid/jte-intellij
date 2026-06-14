package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.psi.JtePsiFile;
import org.jusecase.jte.intellij.language.psi.JtePsiImport;

/**
 * Offers to remove all unused {@code @import} statements when the one under the caret is no
 * longer referenced from any injected Java fragment, mirroring Java's "Remove unused imports"
 * quick fix.
 */
public class JteRemoveUnusedImportIntention extends PsiElementBaseIntentionAction {

    @Override
    public @NotNull String getFamilyName() {
        return "Remove unused @imports";
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @Nullable PsiElement element) {
        JtePsiImport unusedImport = findUnusedImport(editor, element);
        if (unusedImport == null) {
            return false;
        }

        setText(getFamilyName());
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        JtePsiImport unusedImport = findUnusedImport(editor, element);
        if (unusedImport == null) {
            return;
        }

        PsiFile jteFile = unusedImport.getContainingFile();
        WriteCommandAction.runWriteCommandAction(project, "Remove Unused Imports", null, () -> JteImportUtil.removeUnusedImports(jteFile), jteFile);
    }

    @Nullable
    private static JtePsiImport findUnusedImport(@NotNull Editor editor, @Nullable PsiElement element) {
        if (element == null || !(element.getContainingFile() instanceof JtePsiFile jteFile)) {
            return null;
        }

        JtePsiImport possibleImport = PsiTreeUtil.getParentOfType(element, JtePsiImport.class, false);
        if (possibleImport == null) {
            // The @import element's range ends right after the imported class name, so a caret
            // placed at the end of the line resolves to the following whitespace/newline element.
            int offset = editor.getCaretModel().getOffset();
            if (offset > 0) {
                possibleImport = PsiTreeUtil.getParentOfType(jteFile.findElementAt(offset - 1), JtePsiImport.class, false);
            }
        }
        if (possibleImport == null) {
            return null;
        }

        String qualifiedName = JteImportUtil.extractQualifiedName(possibleImport.getText());
        boolean unused = !JteImportUtil.isImportUsed(jteFile, qualifiedName) || JteImportUtil.hasDuplicateImport(possibleImport);
        return unused ? possibleImport : null;
    }
}
