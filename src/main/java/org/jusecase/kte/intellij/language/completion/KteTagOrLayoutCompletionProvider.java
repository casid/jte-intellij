package org.jusecase.kte.intellij.language.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jusecase.kte.intellij.language.psi.KtePsiTagOrLayoutName;

public class KteTagOrLayoutCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        if (position.getParent() == null) {
            return;
        }

        if (!(position.getParent() instanceof KtePsiTagOrLayoutName)) {
            return;
        }
        KtePsiTagOrLayoutName nameElement = (KtePsiTagOrLayoutName)position.getParent();

        KtePsiTagOrLayoutName prevNameElement = PsiTreeUtil.getPrevSiblingOfType(nameElement, KtePsiTagOrLayoutName.class);
        if (prevNameElement == null) {
            PsiDirectory directory = nameElement.findRootDirectory();
            if (directory != null) {
                addSuggestionsForDirectory(directory, result);
            }
        } else {
            PsiReference reference = prevNameElement.getReference();
            if (reference == null) {
                return;
            }

            PsiElement prevReferenceElement = reference.resolve();
            if (prevReferenceElement instanceof PsiDirectory) {
                addSuggestionsForDirectory((PsiDirectory)prevReferenceElement, result);
            }
        }
    }

    private void addSuggestionsForDirectory(PsiDirectory directory, @NotNull CompletionResultSet result) {
        for (PsiDirectory subdirectory : directory.getSubdirectories()) {
            result.addElement(LookupElementBuilder.create(subdirectory));
        }

        for (PsiFile file : directory.getFiles()) {
            String name = file.getName();
            int index = name.indexOf(".kte");
            if (index == -1) {
                continue;
            }

            String referenceName = name.substring(0, index);
            result.addElement(LookupElementBuilder.create(referenceName));
        }
    }
}
