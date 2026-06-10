package org.jusecase.jte.intellij.language.k2;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionSorter;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtFile;

import java.util.function.Consumer;

final class KteTemplateInsertionMapper {
    private KteTemplateInsertionMapper() {
    }

    @NotNull
    static CompletionResultSet mapInsertions(@NotNull CompletionResultSet delegate,
                                             @NotNull PsiFile templateFile,
                                             @NotNull KteSyntheticKotlinFile syntheticFile,
                                             @NotNull KtFile ktFile,
                                             int kotlinOffset,
                                             int hostOffset,
                                             @NotNull Consumer<String> debugSink) {
        return new TemplateInsertionResultSet(delegate, templateFile, syntheticFile, ktFile, kotlinOffset, hostOffset, debugSink);
    }

    @NotNull
    static LookupElement mapInsertion(@NotNull LookupElement element,
                                      @NotNull PsiFile templateFile,
                                      @NotNull KteSyntheticKotlinFile syntheticFile,
                                      @NotNull KtFile ktFile,
                                      int kotlinOffset,
                                      int hostOffset,
                                      @NotNull Consumer<String> debugSink) {
        return new KteTemplateInsertionReplay(templateFile, syntheticFile, ktFile, kotlinOffset, hostOffset, debugSink).wrap(element);
    }

    private static final class TemplateInsertionResultSet extends CompletionResultSet {
        private final CompletionResultSet delegate;
        private final KteTemplateInsertionReplay insertion;

        private TemplateInsertionResultSet(@NotNull CompletionResultSet delegate,
                                           @NotNull PsiFile templateFile,
                                           @NotNull KteSyntheticKotlinFile syntheticFile,
                                           @NotNull KtFile ktFile,
                                           int kotlinOffset,
                                           int hostOffset,
                                           @NotNull Consumer<String> debugSink) {
            this(delegate, new KteTemplateInsertionReplay(templateFile, syntheticFile, ktFile, kotlinOffset, hostOffset, debugSink));
        }

        private TemplateInsertionResultSet(@NotNull CompletionResultSet delegate,
                                           @NotNull KteTemplateInsertionReplay insertion) {
            super(delegate.getPrefixMatcher(), delegate.getConsumer(), delegate.contributor);
            this.delegate = delegate;
            this.insertion = insertion;
        }

        @Override
        public void addElement(@NotNull LookupElement element) {
            delegate.addElement(insertion.wrap(element));
        }

        @Override
        public @NotNull CompletionResultSet withPrefixMatcher(@NotNull PrefixMatcher matcher) {
            return copy(delegate.withPrefixMatcher(matcher));
        }

        @Override
        public @NotNull CompletionResultSet withPrefixMatcher(@NotNull String prefix) {
            return copy(delegate.withPrefixMatcher(prefix));
        }

        @Override
        public @NotNull CompletionResultSet withRelevanceSorter(@NotNull CompletionSorter sorter) {
            return copy(delegate.withRelevanceSorter(sorter));
        }

        @Override
        public void addLookupAdvertisement(@NotNull String advertisement) {
            delegate.addLookupAdvertisement(advertisement);
        }

        @Override
        public @NotNull CompletionResultSet caseInsensitive() {
            return copy(delegate.caseInsensitive());
        }

        @Override
        public void restartCompletionOnPrefixChange(@NotNull ElementPattern prefixCondition) {
            delegate.restartCompletionOnPrefixChange(prefixCondition);
        }

        @Override
        public void restartCompletionWhenNothingMatches() {
            delegate.restartCompletionWhenNothingMatches();
        }

        @NotNull
        private CompletionResultSet copy(@NotNull CompletionResultSet delegate) {
            return new TemplateInsertionResultSet(delegate, insertion);
        }
    }
}
