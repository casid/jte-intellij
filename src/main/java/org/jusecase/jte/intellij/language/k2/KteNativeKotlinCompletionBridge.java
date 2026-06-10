package org.jusecase.jte.intellij.language.k2;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionSorter;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

final class KteNativeKotlinCompletionBridge {
    private final Consumer<String> debugSink;

    KteNativeKotlinCompletionBridge(@NotNull Consumer<String> debugSink) {
        this.debugSink = debugSink;
    }

    boolean complete(@NotNull CompletionParameters originalParameters,
                     @NotNull CompletionResultSet result,
                     @NotNull PsiFile templateFile,
                     int hostOffset) {
        try {
            KteCompletionSyntheticFileContext syntheticContext =
                    KteCompletionSyntheticFileContext.create(templateFile, hostOffset, this::debug);
            if (syntheticContext == null) {
                return false;
            }

            CompletionResultSet insertionMappedResult = KteTemplateInsertionMapper.mapInsertions(
                    result,
                    syntheticContext.originalTemplateFile(),
                    syntheticContext.syntheticFile(),
                    syntheticContext.ktFile(),
                    syntheticContext.kotlinOffset(),
                    hostOffset,
                    this::debug
            );
            FilteringCountingResultSet filteredResult = new FilteringCountingResultSet(insertionMappedResult, this::debug);
            if (!KteNativeKotlinCompletionApi.complete(
                    syntheticContext.toCompletionParameters(originalParameters),
                    filteredResult,
                    this::debug
            )) {
                return false;
            }
            return filteredResult.sawNativeElements();
        } catch (ProcessCanceledException exception) {
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            debug("nativeK2 failed: " + exception.getClass().getName() + ": " + exception.getMessage());
            return false;
        }
    }

    private void debug(@NotNull String event) {
        debugSink.accept(event);
    }

    private static final class FilteringCountingResultSet extends CompletionResultSet {
        private final CompletionResultSet delegate;
        private final Consumer<String> debugSink;
        private final AtomicInteger nativeElementCount;

        private FilteringCountingResultSet(@NotNull CompletionResultSet delegate,
                                           @NotNull Consumer<String> debugSink) {
            this(delegate, debugSink, new AtomicInteger());
        }

        private FilteringCountingResultSet(@NotNull CompletionResultSet delegate,
                                           @NotNull Consumer<String> debugSink,
                                           @NotNull AtomicInteger nativeElementCount) {
            super(delegate.getPrefixMatcher(), delegate.getConsumer(), delegate.contributor);
            this.delegate = delegate;
            this.debugSink = debugSink;
            this.nativeElementCount = nativeElementCount;
        }

        private boolean sawNativeElements() {
            return nativeElementCount.get() > 0;
        }

        @Override
        public void addElement(@NotNull LookupElement element) {
            String lookupString = element.getLookupString();
            if (KteSyntheticKotlinGeneratedNames.isGeneratedLookup(lookupString)) {
                debugSink.accept("nativeK2 filtered generated lookup=" + lookupString);
                return;
            }

            nativeElementCount.incrementAndGet();
            delegate.addElement(element);
        }

        @Override
        public @NotNull CompletionResultSet withPrefixMatcher(@NotNull PrefixMatcher matcher) {
            return new FilteringCountingResultSet(delegate.withPrefixMatcher(matcher), debugSink, nativeElementCount);
        }

        @Override
        public @NotNull CompletionResultSet withPrefixMatcher(@NotNull String prefix) {
            return new FilteringCountingResultSet(delegate.withPrefixMatcher(prefix), debugSink, nativeElementCount);
        }

        @Override
        public @NotNull CompletionResultSet withRelevanceSorter(@NotNull CompletionSorter sorter) {
            return new FilteringCountingResultSet(delegate.withRelevanceSorter(sorter), debugSink, nativeElementCount);
        }

        @Override
        public void addLookupAdvertisement(@NotNull String advertisement) {
            delegate.addLookupAdvertisement(advertisement);
        }

        @Override
        public @NotNull CompletionResultSet caseInsensitive() {
            return new FilteringCountingResultSet(delegate.caseInsensitive(), debugSink, nativeElementCount);
        }

        @Override
        public void restartCompletionOnPrefixChange(@NotNull ElementPattern prefixCondition) {
            delegate.restartCompletionOnPrefixChange(prefixCondition);
        }

        @Override
        public void restartCompletionWhenNothingMatches() {
            delegate.restartCompletionWhenNothingMatches();
        }
    }
}
