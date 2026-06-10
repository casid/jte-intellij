package org.jusecase.jte.intellij.language.k2;

import java.util.Set;

public class KteCompletionGeneratedSymbolFilteringTest extends KteK2FixtureSupport {
    public void testDoesNotCompleteGeneratedWrapperSymbols() {
        myFixture.configureByText("wrapper.kte", """
                ${jt<caret>}
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertDoesNotContainLookup(lookupStrings, "jteOutput");
        assertDoesNotContainLookup(lookupStrings, "dummyCall");
        assertDoesNotContainLookup(lookupStrings, "DummyTemplate");
    }
}
