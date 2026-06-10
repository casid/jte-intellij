package org.jusecase.jte.intellij.language.k2;

import com.intellij.codeInsight.lookup.LookupElement;

import java.util.Set;

public class KteNativeKotlinCompletionLookupTest extends KteK2FixtureSupport {
    public void testCompletesPropertiesForImportedParamTypeInOutputExpression() {
        myFixture.addClass("""
                package com.example;

                public class Profile {
                    public String getDisplayName() {
                        return "";
                    }

                    public String getEmail() {
                        return "";
                    }
                }
                """);

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.<caret>}
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertTrue(lookupStrings.contains("displayName"));
        assertTrue(lookupStrings.contains("email"));
    }

    public void testFiltersByTypedPropertyPrefix() {
        myFixture.addClass("""
                package com.example;

                public class Profile {
                    public String getDisplayName() {
                        return "";
                    }

                    public String getEmail() {
                        return "";
                    }
                }
                """);

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.em<caret>}
                """);

        LookupElement[] elements = myFixture.completeBasic();

        if (elements == null) {
            assertTrue(topLevelFileText().contains("${profile.email}"));
            return;
        }

        Set<String> lookupStrings = lookupStrings(elements);
        assertTrue(lookupStrings.contains("email"));
    }

    public void testCompletesParamsInOutputExpression() {
        myFixture.configureByText("profile.kte", """
                @param profile: String
                @param project: String
                ${pr<caret>}
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertTrue(lookupStrings.contains("profile"));
        assertTrue(lookupStrings.contains("project"));
    }

    public void testCompletesSimpleForLoopVariableInOutputExpression() {
        myFixture.configureByText("profile.kte", """
                @param profiles: List<String>
                @param project: String
                @for(profile in profiles)
                    ${pr<caret>}
                @endfor
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertTrue(lookupStrings.contains("profile"));
        assertTrue(lookupStrings.contains("project"));
    }

    public void testCompletesPropertiesForSimpleForLoopVariable() {
        myFixture.addClass("""
                package com.example;

                public class Profile {
                    public String getDisplayName() {
                        return "";
                    }

                    public String getEmail() {
                        return "";
                    }
                }
                """);

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profiles: List<Profile>
                @for(profile in profiles)
                    ${profile.<caret>}
                @endfor
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertTrue(lookupStrings.contains("displayName"));
        assertTrue(lookupStrings.contains("email"));
    }

    public void testCompletesPropertiesInIfCondition() {
        addProfileJavaClass();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @if(profile.<caret>)
                    ${profile.displayName}
                @endif
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertTrue(lookupStrings.contains("isActive"));
        assertTrue(lookupStrings.contains("displayName"));
        assertTrue(lookupStrings.contains("email"));
        assertTrue(lookupStrings.contains("manager"));
        assertTrue(lookupStrings.contains("tags"));
    }

    public void testCompletesImportedTopLevelFunctionInOutputExpression() {
        addSupportHelpers();

        myFixture.configureByText("helpers.kte", """
                @import com.example.i18n
                ${i<caret>}
                """);

        LookupElement[] elements = myFixture.completeBasic();
        if (elements == null) {
            assertTrue(topLevelFileText().contains("${i18n}"));
            return;
        }

        assertTrue(lookupStrings(elements).contains("i18n"));
    }

    public void testCompletesStarImportedTopLevelFunctionInOutputExpression() {
        addSupportHelpers();

        myFixture.configureByText("helpers.kte", """
                @import com.example.*
                ${i<caret>}
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertContainsLookup(lookupStrings, "i18n");
    }

    public void testCompletesImportedObjectMemberExtensionFunctionAfterReceiverDot() {
        addNestedObjectAndExtensionFixture();

        myFixture.configureByText("base.kte", """
                @import com.example.navigation.breadcrumb.Breadcrumb
                @import com.example.navigation.routing.RoutingUtils.isCurrentPage
                @param breadcrumbs: List<Breadcrumb>?
                @if(breadcrumbs.<caret>)
                    ${breadcrumbs}
                @endif
                """);

        LookupElement[] elements = myFixture.completeBasic();
        if (elements == null) {
            assertTrue(topLevelFileText().contains("breadcrumbs.isCurrentPage"));
            return;
        }

        assertTrue(lookupStrings(elements).contains("isCurrentPage"));
    }

    public void testCompletesNullableReceiverAfterSafeCall() {
        addProfileClassWithKotlinProperties();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile?
                ${profile?.<caret>}
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertContainsLookup(lookupStrings, "displayName");
        assertContainsLookup(lookupStrings, "manager");
    }

    public void testCompletesNestedReceiverMembersFromSourceBackedType() {
        addProfileClassWithKotlinProperties();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.manager?.<caret>}
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertContainsLookup(lookupStrings, "displayName");
        assertContainsLookup(lookupStrings, "manager");
    }

    public void testCompletesCallReceiverMembersFromSourceBackedType() {
        addProfileClassWithKotlinProperties();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${requireNotNull(profile.manager).<caret>}
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertContainsLookup(lookupStrings, "displayName");
        assertContainsLookup(lookupStrings, "manager");
    }

    public void testCompletesHtmlAttributeLocalVariableAfterDeclaration() {
        addNestedObjectAndExtensionFixture();

        myFixture.configureByText("base.kte", """
                @import com.example.navigation.config.PathConfig
                @param isLoggedIn: Boolean
                <footer>
                    <li>
                        !{val link = if (isLoggedIn) PathConfig.BackOffice.GDPR else PathConfig.FrontOffice.GDPR}
                        <a href="${li<caret>}">GDPR</a>
                    </li>
                </footer>
                """);

        LookupElement[] elements = myFixture.completeBasic();
        if (elements == null) {
            assertTrue(topLevelFileText().contains("${link}"));
            return;
        }

        assertTrue(lookupStrings(elements).contains("link"));
    }

    public void testCompletesCompanionMembersForImportedKotlinClass() {
        myFixture.addFileToProject("src/com/example/Profile.kt", """
                package com.example

                class Profile(val displayName: String) {
                    companion object {
                        val DEFAULT = Profile("Default")
                    }
                }
                """);

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                ${Profile.<caret>}
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertContainsLookup(lookupStrings, "DEFAULT");
    }

    public void testCompletesNestedObjectsForImportedKotlinObject() {
        addNestedObjectAndExtensionFixture();

        myFixture.configureByText("base.kte", """
                @import com.example.navigation.config.PathConfig
                ${PathConfig.<caret>}
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertContainsLookup(lookupStrings, "FrontOffice");
        assertContainsLookup(lookupStrings, "BackOffice");
        assertDoesNotContainLookup(lookupStrings, "INSTANCE");
    }

    public void testCompletesConstPropertiesForNestedKotlinObject() {
        addNestedObjectAndExtensionFixture();

        myFixture.configureByText("base.kte", """
                @import com.example.navigation.config.PathConfig
                ${PathConfig.FrontOffice.<caret>}
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertContainsLookup(lookupStrings, "HOME");
        assertContainsLookup(lookupStrings, "TOOLS");
        assertContainsLookup(lookupStrings, "ACCESSIBILITY");
        assertContainsLookup(lookupStrings, "GDPR");
        assertDoesNotContainLookup(lookupStrings, "INSTANCE");
    }

    public void testCompletesMembersForImportedKotlinObjectWithoutInstance() {
        addNestedObjectAndExtensionFixture();

        myFixture.configureByText("base.kte", """
                @import com.example.navigation.routing.RoutingUtils
                ${RoutingUtils.<caret>}
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertContainsLookup(lookupStrings, "getDefaultPath");
        assertDoesNotContainLookup(lookupStrings, "INSTANCE");
    }

    public void testCompletesPropertiesInStatement() {
        addProfileJavaClass();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                !{ profile.<caret> }
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertTrue(lookupStrings.contains("displayName"));
        assertTrue(lookupStrings.contains("email"));
        assertTrue(lookupStrings.contains("manager"));
    }

    public void testCompletesLocalStatementVariableAfterDeclaration() {
        addCareOfferingFixture();

        myFixture.configureByText("facility.kte", """
                @import com.example.Page
                @param page: Page
                !{val careOfferingForm = requireNotNull(page.careOfferingForm)}
                ${care<caret>}
                """);

        LookupElement[] elements = myFixture.completeBasic();
        if (elements == null) {
            assertTrue(topLevelFileText().contains("${careOfferingForm}"));
            return;
        }

        assertTrue(lookupStrings(elements).contains("careOfferingForm"));
    }

    public void testDoesNotCompleteLocalStatementVariableBeforeDeclaration() {
        addCareOfferingFixture();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("facility.kte", """
                    @import com.example.Page
                    @param page: Page
                    ${care<caret>}
                    !{val careOfferingForm = requireNotNull(page.careOfferingForm)}
                    """);

            LookupElement[] elements = myFixture.completeBasic();
            if (elements == null) {
                assertFalse(
                        "Completion inserted future local:\n" + topLevelFileText() +
                                "\n\nDebug events:\n" + String.join("\n", KteNativeKotlinSourceCompletionBridge.debugEvents()),
                        topLevelFileText().contains("${careOfferingForm}")
                );
                return;
            }
            Set<String> lookupStrings = lookupStrings(elements);

            assertFalse(
                    "Did not expect future local in " + lookupStrings +
                            "\n\nDebug events:\n" + String.join("\n", KteNativeKotlinSourceCompletionBridge.debugEvents()),
                    lookupStrings.contains("careOfferingForm")
            );
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    public void testCompletesPropertiesForLocalStatementVariable() {
        addCareOfferingFixture();

        myFixture.configureByText("facility.kte", """
                @import com.example.Page
                @param page: Page
                !{val careOfferingForm = requireNotNull(page.careOfferingForm)}
                ${careOfferingForm.<caret>}
                """);

        Set<String> lookupStrings = completeBasicLookupStrings();

        assertTrue(lookupStrings.contains("displayName"));
    }

    public void testCompletesKotlinKeywordsInOutputExpression() {
        myFixture.configureByText("keywords.kte", """
                ${tr<caret>}
                """);

        LookupElement[] elements = myFixture.completeBasic();
        if (elements == null) {
            assertTrue(topLevelFileText().contains("${true}"));
            return;
        }

        assertContainsLookup(lookupStrings(elements), "true");
    }
}
