package org.jusecase.jte.intellij.language.k2;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.refactoring.KteAddImportIntention;
import org.jusecase.jte.intellij.language.refactoring.KteReplaceImportIntention;

import java.util.List;

final class KteNativeImportFixFactory {
    private static final int MAX_CANDIDATE_FIXES = 10;

    private KteNativeImportFixFactory() {
    }

    @NotNull
    static List<IntentionAction> addImportFixes(@NotNull KteKotlinImportResolver resolver,
                                                @NotNull String visibleName,
                                                boolean includeCallables) {
        List<KteKotlinImportResolver.ImportCandidate> candidates = candidates(resolver, visibleName, includeCallables);
        if (candidates.isEmpty()) {
            return List.of();
        }

        return candidates.stream()
                .map(candidate -> (IntentionAction) new KteAddImportIntention(candidate.qualifiedName()))
                .toList();
    }

    @NotNull
    static List<IntentionAction> replaceImportFixes(@NotNull KteKotlinImportResolver resolver,
                                                    @NotNull String visibleName,
                                                    @NotNull TextRange importRange) {
        List<KteKotlinImportResolver.ImportCandidate> candidates = candidates(resolver, visibleName, true);
        if (candidates.isEmpty()) {
            return List.of();
        }

        return candidates.stream()
                .map(candidate -> (IntentionAction) new KteReplaceImportIntention(importRange, candidate.qualifiedName()))
                .toList();
    }

    @NotNull
    private static List<KteKotlinImportResolver.ImportCandidate> candidates(
            @NotNull KteKotlinImportResolver resolver,
            @NotNull String visibleName,
            boolean includeCallables) {
        List<KteKotlinImportResolver.ImportCandidate> candidates =
                resolver.importCandidates(visibleName, includeCallables);
        return candidates.size() > MAX_CANDIDATE_FIXES ? List.of() : candidates;
    }
}
