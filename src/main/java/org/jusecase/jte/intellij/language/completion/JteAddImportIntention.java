package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.ide.util.PsiClassListCellRenderer;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;

/**
 * The generic "Import class" quick fix is unusable for jte's injected Java fragments (it
 * can crash or silently corrupt the host file), so this edits the host .jte document directly.
 */
public class JteAddImportIntention extends PsiElementBaseIntentionAction {

    @Override
    public @NotNull String getFamilyName() {
        return "Add @import statement";
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @Nullable PsiElement element) {
        PsiJavaCodeReferenceElement reference = JteImportUtil.findUnresolvedReference(project, element);
        if (reference == null) {
            return false;
        }

        PsiClass[] candidates = JteImportUtil.resolveCandidates(project, reference);
        if (candidates.length == 0) {
            return false;
        }

        if (candidates.length == 1) {
            setText("Add @import " + candidates[0].getQualifiedName());
        } else {
            setText("Add @import for " + reference.getReferenceName());
        }

        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiJavaCodeReferenceElement reference = JteImportUtil.findUnresolvedReference(project, element);
        if (reference == null) {
            return;
        }

        PsiClass[] candidates = JteImportUtil.resolveCandidates(project, reference);
        if (candidates.length == 0) {
            return;
        }

        PsiFile injectedFile = reference.getContainingFile();

        if (candidates.length == 1) {
            addImport(project, injectedFile, candidates[0]);
            return;
        }

        Arrays.sort(candidates, Comparator.comparing(PsiClass::getQualifiedName, Comparator.nullsLast(Comparator.naturalOrder())));

        JBPopupFactory.getInstance()
                .createPopupChooserBuilder(Arrays.asList(candidates))
                .setRenderer(new PsiClassListCellRenderer())
                .setTitle("Add Import")
                .setItemChosenCallback(target -> addImport(project, injectedFile, target))
                .createPopup()
                .showInBestPositionFor(editor);
    }

    private static void addImport(Project project, PsiFile injectedFile, PsiClass target) {
        String qualifiedName = target.getQualifiedName();
        if (qualifiedName == null) {
            return;
        }

        JteImportUtil.insertImport(project, injectedFile, qualifiedName);
    }
}
