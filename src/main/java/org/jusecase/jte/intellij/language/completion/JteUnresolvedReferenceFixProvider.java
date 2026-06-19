package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.daemon.QuickFixActionRegistrar;
import com.intellij.codeInsight.quickfix.UnresolvedReferenceQuickFixProvider;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.psi.JtePsiFile;

public class JteUnresolvedReferenceFixProvider extends UnresolvedReferenceQuickFixProvider<PsiJavaCodeReferenceElement> {

    @Override
    public void registerFixes(@NotNull PsiJavaCodeReferenceElement ref, @NotNull QuickFixActionRegistrar registrar) {
        PsiFile file = ref.getContainingFile();
        if (file == null) {
            return;
        }

        PsiFile topLevelFile = InjectedLanguageManager.getInstance(ref.getProject()).getTopLevelFile(file);
        if (!(topLevelFile instanceof JtePsiFile)) {
            return;
        }

        if (JteImportUtil.resolveCandidates(ref.getProject(), ref).length == 0) {
            return;
        }

        registrar.register(new JteAddImportIntention());
    }

    @Override
    public @NotNull Class<PsiJavaCodeReferenceElement> getReferenceClass() {
        return PsiJavaCodeReferenceElement.class;
    }
}
