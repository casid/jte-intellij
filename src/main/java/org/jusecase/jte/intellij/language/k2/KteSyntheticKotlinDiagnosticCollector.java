package org.jusecase.jte.intellij.language.k2;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.KteLanguage;
import org.jusecase.jte.intellij.language.psi.JtePsiImport;
import org.jusecase.jte.intellij.language.psi.JtePsiJavaInjection;
import org.jusecase.jte.intellij.language.psi.KtePsiFile;
import org.jusecase.jte.intellij.language.refactoring.KteOptimizeImportsIntention;
import org.jusecase.jte.intellij.language.refactoring.KteRemoveImportIntention;
import org.jusecase.jte.intellij.language.template.KteKotlinTypeText;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scoped diagnostics for the synthetic K2 path. This intentionally covers only cases that can be
 * mapped safely or edited deterministically in .kte source.
 */
public final class KteSyntheticKotlinDiagnosticCollector {
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    @NotNull
    public List<Diagnostic> collect(@NotNull PsiFile templateFile) {
        if (!isKteFile(templateFile)) {
            return List.of();
        }

        KteSyntheticKotlinDiagnosticSink sink = new KteSyntheticKotlinDiagnosticSink();
        KteSyntheticKotlinModel model = KteSyntheticKotlinModelService.getInstance(templateFile.getProject()).getModel(templateFile);
        KteKotlinImportResolver importResolver = new KteKotlinImportResolver(templateFile);

        KteSyntheticKotlinSemanticService semanticService =
                KteSyntheticKotlinSemanticService.getInstance(templateFile.getProject());
        for (Diagnostic diagnostic : semanticService.collectKotlinDiagnostics(templateFile, model)) {
            sink.add(withNativeImportFixes(templateFile, importResolver, diagnostic));
        }

        for (Diagnostic diagnostic : new KteTemplateContractDiagnosticChecker().collect(templateFile)) {
            sink.add(diagnostic);
        }

        return sink.diagnostics();
    }

    @NotNull
    private Diagnostic withNativeImportFixes(@NotNull PsiFile templateFile,
                                             @NotNull KteKotlinImportResolver resolver,
                                             @NotNull Diagnostic diagnostic) {
        if (!diagnostic.origin().equals(Origin.SYNTHETIC_KOTLIN) ||
                !diagnostic.fixes().isEmpty()) {
            return diagnostic;
        }

        Diagnostic importDirectiveDiagnostic = withImportDirectiveFixes(templateFile, resolver, diagnostic);
        if (importDirectiveDiagnostic != diagnostic) {
            return importDirectiveDiagnostic;
        }

        if (!diagnostic.message().contains("Unresolved reference")) {
            return diagnostic;
        }

        String unresolvedName = unresolvedIdentifier(templateFile, diagnostic.templateRange());
        if (unresolvedName == null) {
            return diagnostic;
        }

        List<IntentionAction> fixes = KteNativeImportFixFactory.addImportFixes(resolver, unresolvedName, true);
        if (fixes.isEmpty()) {
            return diagnostic;
        }

        return new Diagnostic(
                diagnostic.severity(),
                diagnostic.message(),
                diagnostic.templateRange(),
                diagnostic.origin(),
                fixes
        );
    }

    @NotNull
    private Diagnostic withImportDirectiveFixes(@NotNull PsiFile templateFile,
                                                @NotNull KteKotlinImportResolver resolver,
                                                @NotNull Diagnostic diagnostic) {
        JtePsiJavaInjection injection = importDirectiveInjection(templateFile, diagnostic.templateRange());
        if (injection == null) {
            return diagnostic;
        }

        String importedName = injection.getText().trim();
        if (importedName.isEmpty()) {
            return diagnostic;
        }

        List<IntentionAction> fixes = new ArrayList<>(KteNativeImportFixFactory.replaceImportFixes(
                resolver,
                KteKotlinTypeText.shortName(importedName),
                injection.getTextRange()
        ));
        fixes.add(new KteRemoveImportIntention(injection.getTextRange()));
        fixes.add(new KteOptimizeImportsIntention());

        return new Diagnostic(
                diagnostic.severity(),
                diagnostic.message(),
                diagnostic.templateRange(),
                diagnostic.origin(),
                fixes
        );
    }

    private JtePsiJavaInjection importDirectiveInjection(@NotNull PsiFile templateFile,
                                                        @NotNull TextRange range) {
        if (!isValidRange(templateFile, range)) {
            return null;
        }

        int offset = Math.min(range.getStartOffset(), Math.max(0, templateFile.getTextLength() - 1));
        PsiElement element = templateFile.findElementAt(offset);
        if (element == null && range.getEndOffset() > range.getStartOffset()) {
            element = templateFile.findElementAt(range.getEndOffset() - 1);
        }
        if (element == null || PsiTreeUtil.getParentOfType(element, JtePsiImport.class, false) == null) {
            return null;
        }

        return PsiTreeUtil.getParentOfType(element, JtePsiJavaInjection.class, false);
    }

    private String unresolvedIdentifier(@NotNull PsiFile templateFile, @NotNull TextRange range) {
        if (!isValidRange(templateFile, range)) {
            return null;
        }

        Matcher matcher = IDENTIFIER.matcher(templateFile.getText().substring(range.getStartOffset(), range.getEndOffset()));
        return matcher.matches() ? matcher.group() : null;
    }

    private boolean isValidRange(@NotNull PsiFile file, @NotNull TextRange range) {
        return range.getStartOffset() >= 0 &&
                range.getEndOffset() <= file.getTextLength() &&
                range.getStartOffset() <= range.getEndOffset();
    }

    private boolean isKteFile(@NotNull PsiFile file) {
        return file instanceof KtePsiFile ||
                file.getViewProvider().getPsi(KteLanguage.INSTANCE) instanceof KtePsiFile;
    }

    public record Diagnostic(@NotNull HighlightSeverity severity,
                             @NotNull String message,
                             @NotNull TextRange templateRange,
                             @NotNull Origin origin,
                             @NotNull List<IntentionAction> fixes) {
        public Diagnostic {
            fixes = List.copyOf(fixes);
        }

        public Diagnostic(@NotNull HighlightSeverity severity,
                          @NotNull String message,
                          @NotNull TextRange templateRange) {
            this(severity, message, templateRange, Origin.FALLBACK, List.of());
        }

        public Diagnostic(@NotNull HighlightSeverity severity,
                          @NotNull String message,
                          @NotNull TextRange templateRange,
                          @NotNull Origin origin) {
            this(severity, message, templateRange, origin, List.of());
        }
    }

    public enum Origin {
        FALLBACK,
        SYNTHETIC_KOTLIN,
        TEMPLATE_DIRECTIVE,
        TEMPLATE_STRUCTURE
    }
}
