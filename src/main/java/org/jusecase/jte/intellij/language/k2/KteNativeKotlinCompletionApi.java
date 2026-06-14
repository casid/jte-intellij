package org.jusecase.jte.intellij.language.k2;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.completion.CompletionUtilsNoResolveKt;
import org.jetbrains.kotlin.idea.completion.impl.k2.Completions;
import org.jetbrains.kotlin.idea.completion.impl.k2.KotlinFirCompletionParameters;
import org.jetbrains.kotlin.idea.util.positionContext.KotlinPositionContextDetector;
import org.jetbrains.kotlin.idea.util.positionContext.KotlinRawPositionContext;

import java.util.function.Consumer;

final class KteNativeKotlinCompletionApi {
    private KteNativeKotlinCompletionApi() {
    }

    static boolean complete(@NotNull CompletionParameters parameters,
                            @NotNull CompletionResultSet result,
                            @NotNull Consumer<String> debugSink) {
        // Kotlin has no public source-language completion API for this bridge; keep the internal K2 call isolated here.
        KotlinFirCompletionParameters kotlinParameters = KotlinFirCompletionParameters.Companion.create(parameters);
        if (kotlinParameters == null) {
            debugSink.accept("directK2 skipped: parameters=null");
            return false;
        }

        KotlinRawPositionContext positionContext = KotlinPositionContextDetector.INSTANCE.detect(kotlinParameters.getPosition());
        debugSink.accept("directK2 positionContext=" + positionContext.getClass().getName());
        String prefix = CompletionUtil.findIdentifierPrefix(
                kotlinParameters.getCompletionFile(),
                kotlinParameters.getOffset(),
                CompletionUtilsNoResolveKt.kotlinIdentifierPartPattern(),
                CompletionUtilsNoResolveKt.kotlinIdentifierStartPattern()
        );
        CompletionResultSet resultSet = result.withPrefixMatcher(prefix);
        Completions.INSTANCE.complete(
                kotlinParameters,
                positionContext,
                resultSet
        );
        return true;
    }
}
