package org.jusecase.jte.intellij.language.k2;

import com.intellij.codeInsight.lookup.LookupElement;

public class KteKotlinTypePositionCompletionTest extends KteK2FixtureSupport {
    public void testCompletesImportedTypeInParamType() {
        addProfileClass();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Pro<caret>
                ${profile}
                """);

        LookupElement[] elements = myFixture.completeBasic();
        if (elements == null) {
            assertTrue(topLevelFileText().contains("@param profile: Profile"));
            return;
        }

        assertContainsLookup(lookupStrings(elements), "Profile");
    }

    public void testCompletesStarImportedTypeInParamType() {
        addProfileClass();

        myFixture.configureByText("profile.kte", """
                @import com.example.*
                @param profile: Pro<caret>
                ${profile}
                """);

        LookupElement[] elements = myFixture.completeBasic();
        if (elements == null) {
            assertTrue(topLevelFileText().contains("@param profile: Profile"));
            return;
        }

        assertContainsLookup(lookupStrings(elements), "Profile");
    }

    public void testCompletesBuiltinTypeInParamType() {
        myFixture.configureByText("profile.kte", """
                @param title: Str<caret>
                ${title}
                """);

        LookupElement[] elements = myFixture.completeBasic();
        if (elements == null) {
            assertTrue(topLevelFileText().contains("@param title: String"));
            return;
        }

        assertContainsLookup(lookupStrings(elements), "String");
    }

    public void testCompletesImportedGenericTypeArgumentInParamType() {
        addProfileClass();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profiles: List<Pro<caret>>
                ${profiles}
                """);

        LookupElement[] elements = myFixture.completeBasic();
        if (elements == null) {
            assertTrue(topLevelFileText().contains("@param profiles: List<Profile>"));
            return;
        }

        assertContainsLookup(lookupStrings(elements), "Profile");
    }
}
