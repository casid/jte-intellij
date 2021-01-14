package org.jusecase.jte.intellij.language;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.psi.*;

import java.util.List;

public class JteFoldingBuilder extends CustomFoldingBuilder {

    @Override
    protected void buildLanguageFoldRegions(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root, @NotNull Document document, boolean quick) {
        addFoldRegions(descriptors, root);
    }

    private void addFoldRegions(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement element) {
        final PsiElement[] children = element.getChildren();

        for (PsiElement child : children) {
            ProgressManager.checkCanceled();

            if (child instanceof JtePsiBlock) {
                descriptors.add(new FoldingDescriptor(child.getNode(), child.getTextRange(), null, "..."));
            }

            addFoldRegions(descriptors, child);
        }
    }

    @Override
    protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
        return null;
    }

    @Override
    protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
