package org.jusecase.jte.intellij.language.k2;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtFile;

import java.util.function.Consumer;

record KteCompletionSyntheticFileContext(@NotNull PsiFile originalTemplateFile,
                                         @NotNull KteSyntheticKotlinFile syntheticFile,
                                         @NotNull KtFile ktFile,
                                         int kotlinOffset,
                                         @NotNull PsiElement position,
                                         @NotNull PsiElement analysisContext) {
    @Nullable
    static KteCompletionSyntheticFileContext create(@NotNull PsiFile templateCopy,
                                                    int hostOffset,
                                                    @NotNull Consumer<String> debugSink) {
        KteSyntheticKotlinFile syntheticFile = new KteSyntheticKotlinFileBuilder().buildForCompletion(templateCopy, hostOffset);
        Integer kotlinOffset = syntheticFile.mapTemplateOffsetToKotlin(hostOffset);
        if (kotlinOffset == null) {
            debugSink.accept("rebasedK2 skipped: no mapping for hostOffset=" + hostOffset);
            return null;
        }

        PsiFile originalTemplateFile = templateCopy.getOriginalFile();
        if (!KteTemplateSignatureService.isKteTemplate(originalTemplateFile)) {
            originalTemplateFile = templateCopy;
        }

        KteSyntheticKotlinAnalysisContextService contextService =
                KteSyntheticKotlinAnalysisContextService.getInstance(templateCopy.getProject());
        VirtualFile sourceRoot = contextService.findModuleSourceRoot(originalTemplateFile);
        PsiElement analysisContext = contextService.findAnalysisContext(originalTemplateFile, sourceRoot);
        KtFile baseKtFile = KteSyntheticKotlinPsiFactory.createKtFile(templateCopy.getProject(), syntheticFile, analysisContext);
        KtFile ktFile = (KtFile) baseKtFile.copy();
        // K2 completion requires a file copy, but the origin must stay synthetic so project .kt locals do not leak into .kte completion.
        ktFile.putUserData(PsiFileFactory.ORIGINAL_FILE, baseKtFile);
        KteSyntheticKotlinPsiFactory.configureAnalysisContext(templateCopy.getProject(), ktFile, analysisContext);
        PsiElement position = ktFile.findElementAt(kotlinOffset);
        if (position == null) {
            debugSink.accept("rebasedK2 skipped: no position at kotlinOffset=" + kotlinOffset);
            return null;
        }

        debugSink.accept("rebasedK2 file=" + describe(ktFile) +
                " context=" + describeElement(analysisContext) +
                " offset=" + kotlinOffset +
                " position=" + describeElement(position));
        return new KteCompletionSyntheticFileContext(
                originalTemplateFile,
                syntheticFile,
                ktFile,
                kotlinOffset,
                position,
                analysisContext
        );
    }

    @NotNull
    CompletionParameters toCompletionParameters(@NotNull CompletionParameters originalParameters) {
        CompletionParameters parameters = new CompletionParameters(
                position,
                ktFile,
                originalParameters.getCompletionType(),
                kotlinOffset,
                originalParameters.getInvocationCount(),
                originalParameters.getEditor(),
                originalParameters.getProcess()
        );
        parameters.setCompleteOnlyNotImported(originalParameters.isCompleteOnlyNotImported());
        return parameters;
    }

    @NotNull
    private static String describe(@Nullable PsiFile file) {
        if (file == null) {
            return "null";
        }

        PsiFile originalFile = file.getOriginalFile();
        return file.getClass().getSimpleName() +
                "(name=" + file.getName() +
                ", language=" + file.getLanguage().getID() +
                ", kt=" + (file instanceof KtFile) +
                ", physical=" + file.isPhysical() +
                ", original=" + (originalFile == file ? "self" : originalFile.getClass().getSimpleName() + ":" + originalFile.getName()) +
                ")";
    }

    @NotNull
    private static String describeElement(@Nullable PsiElement element) {
        if (element == null) {
            return "null";
        }
        return element.getClass().getSimpleName() +
                "(text='" + sanitize(element.getText()) +
                "', range=" + element.getTextRange() +
                ")";
    }

    @NotNull
    private static String sanitize(@Nullable String text) {
        if (text == null) {
            return "null";
        }
        return text.replace("\n", "\\n").replace("\r", "\\r");
    }
}
