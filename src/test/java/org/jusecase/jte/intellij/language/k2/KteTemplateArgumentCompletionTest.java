package org.jusecase.jte.intellij.language.k2;

import java.util.Set;

public class KteTemplateArgumentCompletionTest extends KteK2FixtureSupport {
    public void testCompletesPropertiesInTemplateArgument() {
        addProfileJavaClass();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @template.components.card(profile, profile.<caret>)
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertTrue(lookupStrings.contains("displayName"));
        assertTrue(lookupStrings.contains("email"));
        assertTrue(lookupStrings.contains("tags"));
    }

    public void testCompletesVariablesInTemplateArgument() {
        addProfileJavaClass();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param project: String
                @template.components.card(pr<caret>, profile.email)
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertTrue(lookupStrings.contains("profile"));
        assertTrue(lookupStrings.contains("project"));
    }

    public void testCompletesEnumEntriesForImportedEnumClass() {
        addSupportHelpers();

        myFixture.configureByText("form.kte", """
                @import com.example.HiddenHttpMethod
                @template.form.hidden_http_method(method = HiddenHttpMethod.<caret>)
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertTrue(lookupStrings.contains("PUT"));
        assertTrue(lookupStrings.contains("DELETE"));
    }
}
