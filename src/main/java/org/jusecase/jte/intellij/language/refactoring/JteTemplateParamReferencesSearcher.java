package org.jusecase.jte.intellij.language.refactoring;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.JteFileType;
import org.jusecase.jte.intellij.language.KteFileType;
import org.jusecase.jte.intellij.language.psi.JtePsiParam;
import org.jusecase.jte.intellij.language.psi.JtePsiParamName;
import org.jusecase.jte.intellij.language.psi.KtePsiParamName;

public class JteTemplateParamReferencesSearcher
        extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
    public JteTemplateParamReferencesSearcher() {
        super(true);
    }

    @Override
    public void processQuery(@NotNull ReferencesSearch.SearchParameters queryParameters,
                             @NotNull Processor<? super PsiReference> consumer) {
        PsiElement target = queryParameters.getElementToSearch();
        if (!(target instanceof JtePsiParam)) {
            return;
        }

        SearchScope scope = queryParameters.getEffectiveSearchScope();
        if (scope instanceof LocalSearchScope localSearchScope) {
            processLocalScope(target, localSearchScope, consumer);
        } else if (scope instanceof GlobalSearchScope globalSearchScope) {
            processGlobalScope(target, globalSearchScope, consumer);
        }
    }

    private void processLocalScope(@NotNull PsiElement target,
                                   @NotNull LocalSearchScope scope,
                                   @NotNull Processor<? super PsiReference> consumer) {
        for (PsiElement element : scope.getScope()) {
            PsiFile file = element instanceof PsiFile psiFile ? psiFile : element.getContainingFile();
            if (file != null && !processFile(target, file, consumer)) {
                return;
            }
        }
    }

    private void processGlobalScope(@NotNull PsiElement target,
                                    @NotNull GlobalSearchScope scope,
                                    @NotNull Processor<? super PsiReference> consumer) {
        PsiManager psiManager = PsiManager.getInstance(target.getProject());
        if (!processFiles(target, psiManager, FileTypeIndex.getFiles(KteFileType.INSTANCE, scope), consumer)) {
            return;
        }
        processFiles(target, psiManager, FileTypeIndex.getFiles(JteFileType.INSTANCE, scope), consumer);
    }

    private boolean processFiles(@NotNull PsiElement target,
                                 @NotNull PsiManager psiManager,
                                 @NotNull Iterable<VirtualFile> virtualFiles,
                                 @NotNull Processor<? super PsiReference> consumer) {
        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile file = psiManager.findFile(virtualFile);
            if (file != null && !processFile(target, file, consumer)) {
                return false;
            }
        }
        return true;
    }

    private boolean processFile(@NotNull PsiElement target,
                                @NotNull PsiFile file,
                                @NotNull Processor<? super PsiReference> consumer) {
        for (KtePsiParamName paramName : PsiTreeUtil.findChildrenOfType(file, KtePsiParamName.class)) {
            PsiReference reference = paramName.getReference();
            if (reference != null && reference.isReferenceTo(target) && !consumer.process(reference)) {
                return false;
            }
        }

        for (JtePsiParamName paramName : PsiTreeUtil.findChildrenOfType(file, JtePsiParamName.class)) {
            PsiReference reference = paramName.getReference();
            if (reference != null && reference.isReferenceTo(target) && !consumer.process(reference)) {
                return false;
            }
        }

        return true;
    }
}
