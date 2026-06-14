package org.jusecase.jte.intellij.language.k2;

import org.jetbrains.annotations.NotNull;

final class KteSyntheticKotlinGeneratedNames {
    private KteSyntheticKotlinGeneratedNames() {
    }

    static boolean isGeneratedLookup(@NotNull String lookupString) {
        return "INSTANCE".equals(lookupString) ||
                "DummyTemplate".equals(lookupString) ||
                "jteOutput".equals(lookupString) ||
                "dummyCall".equals(lookupString) ||
                lookupString.startsWith("__jte_template_");
    }
}
