package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.JteIcons;
import org.jusecase.jte.intellij.language.psi.JtePsiTemplateName;

public abstract class AbstractTemplateCompletionProvider extends CompletionProvider<CompletionParameters> {

    private final String fileSuffix;

    protected AbstractTemplateCompletionProvider(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        if (position.getParent() == null) {
            return;
        }

        if (!(position.getParent() instanceof JtePsiTemplateName)) {
            return;
        }
        JtePsiTemplateName nameElement = (JtePsiTemplateName) position.getParent();
        PsiDirectory rootDirectory = nameElement.findRootDirectory();
        if (rootDirectory == null) {
            return;
        }

        JtePsiTemplateName prevNameElement = PsiTreeUtil.getPrevSiblingOfType(nameElement, JtePsiTemplateName.class);
        if (prevNameElement == null) {
            addSuggestionsForDirectory(rootDirectory, result);
        } else {
            PsiReference reference = prevNameElement.getReference();
            if (reference == null) {
                return;
            }

            PsiElement prevReferenceElement = reference.resolve();
            if (prevReferenceElement instanceof PsiDirectory) {
                addSuggestionsForDirectory((PsiDirectory) prevReferenceElement, result);
            }
        }
    }

    private void addSuggestionsForDirectory(@NotNull PsiDirectory directory, @NotNull CompletionResultSet result) {
        for (PsiDirectory subdirectory : directory.getSubdirectories()) {
            result.addElement(LookupElementBuilder.create(subdirectory).withIcon(AllIcons.Nodes.Folder));
        }

        addSuggestionsForDirectoryRecursively("", directory, result);
    }

    private void addSuggestionsForDirectoryRecursively(@NotNull String prefix, @NotNull PsiDirectory directory, @NotNull CompletionResultSet result) {
        for (PsiDirectory subdirectory : directory.getSubdirectories()) {
            addSuggestionsForDirectoryRecursively(prefix + subdirectory.getName() + ".", subdirectory, result);
        }

        for (PsiFile file : directory.getFiles()) {
            String referenceName = resolveReferenceName(prefix, file);
            if (referenceName == null) {
                continue;
            }

            result.addElement(LookupElementBuilder.create(referenceName).withInsertHandler(createAfterCompletionInsertHandler(file)).withIcon(JteIcons.ICON));
        }
    }

    private String resolveReferenceName(String prefix, PsiFile file) {
        String referenceName = resolveReferenceName(prefix, file, fileSuffix);
        if (referenceName != null) {
            return referenceName;
        }

        return resolveReferenceName(prefix, file, ".jte".equals(fileSuffix) ? ".kte" : ".jte");
    }

    private String resolveReferenceName(String prefix, PsiFile file, String fileSuffix) {
        String name = file.getName();
        int index = name.lastIndexOf(fileSuffix);
        if (index == -1 || !name.endsWith(fileSuffix)) {
            return null;
        }

        return prefix + name.substring(0, index);
    }

    protected abstract InsertHandler<LookupElement> createAfterCompletionInsertHandler(PsiFile file);
}
