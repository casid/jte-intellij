package org.jusecase.jte.intellij.language.k2;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.psi.KtePsiJavaContent;

public final class KteSyntheticKotlinDiagnosticAnnotator implements Annotator {
    private final KteSyntheticKotlinDiagnosticCollector collector = new KteSyntheticKotlinDiagnosticCollector();

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!(element instanceof KtePsiJavaContent)) {
            return;
        }

        PsiFile templateFile = element.getContainingFile();
        for (KteSyntheticKotlinDiagnosticCollector.Diagnostic diagnostic : collector.collect(templateFile)) {
            AnnotationBuilder builder = holder.newAnnotation(diagnostic.severity(), diagnostic.message())
                    .range(diagnostic.templateRange());
            for (IntentionAction fix : diagnostic.fixes()) {
                builder = builder.withFix(fix);
            }
            builder.create();
        }
    }
}
