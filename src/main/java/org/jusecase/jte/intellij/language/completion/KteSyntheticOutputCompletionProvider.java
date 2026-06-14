package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.KteLanguage;
import org.jusecase.jte.intellij.language.k2.KteNativeKotlinSourceCompletionBridge;
import org.jusecase.jte.intellij.language.psi.JtePsiElseIf;
import org.jusecase.jte.intellij.language.psi.JtePsiFor;
import org.jusecase.jte.intellij.language.psi.JtePsiIf;
import org.jusecase.jte.intellij.language.psi.JtePsiImport;
import org.jusecase.jte.intellij.language.psi.JtePsiJavaInjection;
import org.jusecase.jte.intellij.language.psi.JtePsiOutput;
import org.jusecase.jte.intellij.language.psi.JtePsiParam;
import org.jusecase.jte.intellij.language.psi.JtePsiStatement;
import org.jusecase.jte.intellij.language.psi.JtePsiTemplate;
import org.jusecase.jte.intellij.language.psi.KtePsiFile;

import java.util.Optional;

public class KteSyntheticOutputCompletionProvider extends CompletionProvider<CompletionParameters> {
    private final KteNativeKotlinSourceCompletionBridge nativeCompletionBridge = new KteNativeKotlinSourceCompletionBridge();

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        JtePsiJavaInjection completionInjection = findKotlinFragmentInjection(parameters, false);
        if (completionInjection == null) {
            return;
        }

        if (isEmptyTemplateArgumentNameSlot(completionInjection, parameters.getOffset())) {
            return;
        }

        nativeCompletionBridge.complete(parameters, result, completionInjection.getContainingFile(), parameters.getOffset());
    }

    @Nullable
    private JtePsiJavaInjection findKotlinFragmentInjection(CompletionParameters parameters, boolean preferOriginalPosition) {
        PsiElement position = preferOriginalPosition
                ? Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition())
                : parameters.getPosition();
        JtePsiJavaInjection injection = PsiTreeUtil.getParentOfType(position, JtePsiJavaInjection.class, false);
        if (injection == null && position instanceof JtePsiJavaInjection javaInjection) {
            injection = javaInjection;
        }

        if (injection == null || !isKteFile(injection.getContainingFile()) || !isSupportedInjection(injection)) {
            return null;
        }

        return injection;
    }

    private boolean isKteFile(@NotNull PsiElement element) {
        return element instanceof KtePsiFile ||
                element.getContainingFile().getViewProvider().getPsi(KteLanguage.INSTANCE) instanceof KtePsiFile;
    }

    private boolean isSupportedInjection(@NotNull JtePsiJavaInjection injection) {
        if (PsiTreeUtil.getParentOfType(injection, JtePsiImport.class, false) != null) {
            return false;
        }

        return PsiTreeUtil.getParentOfType(injection, JtePsiParam.class, false) != null ||
                PsiTreeUtil.getParentOfType(injection, JtePsiOutput.class, false) != null ||
                PsiTreeUtil.getParentOfType(injection, JtePsiIf.class, false) != null ||
                PsiTreeUtil.getParentOfType(injection, JtePsiElseIf.class, false) != null ||
                PsiTreeUtil.getParentOfType(injection, JtePsiFor.class, false) != null ||
                PsiTreeUtil.getParentOfType(injection, JtePsiStatement.class, false) != null ||
                PsiTreeUtil.getParentOfType(injection, JtePsiTemplate.class, false) != null;
    }

    private boolean isEmptyTemplateArgumentNameSlot(@NotNull JtePsiJavaInjection injection, int offset) {
        if (PsiTreeUtil.getParentOfType(injection, JtePsiTemplate.class, false) == null) {
            return false;
        }

        String text = injection.getContainingFile().getText();
        int index = Math.min(offset, text.length()) - 1;
        while (index >= 0 && Character.isWhitespace(text.charAt(index))) {
            index--;
        }

        return index >= 0 && (text.charAt(index) == '(' || text.charAt(index) == ',');
    }
}
