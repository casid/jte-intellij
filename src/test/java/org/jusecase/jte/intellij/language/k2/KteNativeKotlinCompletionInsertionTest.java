package org.jusecase.jte.intellij.language.k2;

public class KteNativeKotlinCompletionInsertionTest extends KteK2FixtureSupport {
    public void testNativeCompletionReplaysImportedTopLevelFunctionCallFromSourceExpression() {
        addNoArgTopLevelCompletionFunctions();
        addJteRuntimeStubs();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("helpers.kte", """
                    @import com.example.i18n
                    ${i18<caret>}
                    """);

            chooseCompletion("i18n");

            assertTopLevelFileEquals("""
                    @import com.example.i18n
                    ${i18n()}
                    """);
            assertNativeCompletionWasUsed();
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    public void testNativeCompletionReplaysAutoImportForTopLevelFunctionFromSourceExpression() {
        addNoArgTopLevelCompletionFunctions();
        addJteRuntimeStubs();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("helpers.kte", """
                    ${i18<caret>}
                    """);

            chooseCompletion("i18n");

            assertTopLevelFileEquals("""
                    @import com.example.i18n
                    ${i18n()}
                    """);
            assertNativeCompletionWasUsed();
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    public void testNativeCompletionReplaysImportedKotlinPropertyFromSourceExpression() {
        addProfileClassWithKotlinProperties();
        addJteRuntimeStubs();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("profile.kte", """
                    @import com.example.Profile
                    @param profile: Profile
                    ${profile.dis<caret>}
                    """);

            chooseCompletion("displayName");

            assertTopLevelFileEquals("""
                    @import com.example.Profile
                    @param profile: Profile
                    ${profile.displayName}
                    """);
            assertNativeCompletionWasUsed();
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    public void testNativeCompletionReplaysTemplateParamFromSourceExpression() {
        addJteRuntimeStubs();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("profile.kte", """
                    @param profile: String
                    @param project: String
                    ${prof<caret>}
                    """);

            chooseCompletion("profile");

            assertTopLevelFileEquals("""
                    @param profile: String
                    @param project: String
                    ${profile}
                    """);
            assertNativeCompletionWasUsed();
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    public void testNativeCompletionReplaysStatementLocalAfterDeclaration() {
        addCareOfferingFixture();
        addJteRuntimeStubs();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("facility.kte", """
                    @import com.example.Page
                    @param page: Page
                    !{val careOfferingForm = requireNotNull(page.careOfferingForm)}
                    ${care<caret>}
                    """);

            chooseCompletion("careOfferingForm");

            assertTopLevelFileEquals("""
                    @import com.example.Page
                    @param page: Page
                    !{val careOfferingForm = requireNotNull(page.careOfferingForm)}
                    ${careOfferingForm}
                    """);
            assertNativeCompletionWasUsed();
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    public void testNativeCompletionReplaysSafeCallMemberFromSourceExpression() {
        addProfileClassWithKotlinProperties();
        addJteRuntimeStubs();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("profile.kte", """
                    @import com.example.Profile
                    @param profile: Profile?
                    ${profile?.dis<caret>}
                    """);

            chooseCompletion("displayName");

            assertTopLevelFileEquals("""
                    @import com.example.Profile
                    @param profile: Profile?
                    ${profile?.displayName}
                    """);
            assertNativeCompletionWasUsed();
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    public void testNativeCompletionReplaysImportedExtensionFunctionAfterReceiverDot() {
        addNestedObjectAndExtensionFixture();
        addJteRuntimeStubs();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("base.kte", """
                    @import com.example.navigation.breadcrumb.Breadcrumb
                    @import com.example.navigation.routing.RoutingUtils.isCurrentPage
                    @param breadcrumbs: List<Breadcrumb>?
                    @if(breadcrumbs.is<caret>)
                        ${breadcrumbs}
                    @endif
                    """);

            chooseCompletion("isCurrentPage");

            assertTopLevelFileContains("breadcrumbs.isCurrentPage");
            assertNativeCompletionWasUsed();
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    public void testNativeCompletionReplaysImportedTypeInParamType() {
        addProfileClass();
        addJteRuntimeStubs();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("profile.kte", """
                    @import com.example.Profile
                    @param profile: Pro<caret>
                    ${profile}
                    """);

            chooseCompletion("Profile");

            assertTopLevelFileEquals("""
                    @import com.example.Profile
                    @param profile: Profile
                    ${profile}
                    """);
            assertNativeCompletionWasUsed();
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    public void testNativeCompletionReplaysEnumEntryInTemplateArgument() {
        addSupportHelpers();
        addJteRuntimeStubs();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("form.kte", """
                    @import com.example.HiddenHttpMethod
                    @template.form.hidden_http_method(method = HiddenHttpMethod.P<caret>)
                    """);

            chooseCompletion("PUT");

            assertTopLevelFileEquals("""
                    @import com.example.HiddenHttpMethod
                    @template.form.hidden_http_method(method = HiddenHttpMethod.PUT)
                    """);
            assertNativeCompletionWasUsed();
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    public void testNativeCompletionReplaysStarImportedTopLevelFunctionCall() {
        addNoArgTopLevelCompletionFunctions();
        addJteRuntimeStubs();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("helpers.kte", """
                    @import com.example.*
                    ${i18<caret>}
                    """);

            chooseCompletion("i18n");

            assertTopLevelFileEquals("""
                    @import com.example.*
                    ${i18n()}
                    """);
            assertNativeCompletionWasUsed();
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    public void testNativeCompletionReplaysCompanionMember() {
        myFixture.addFileToProject("src/com/example/Profile.kt", """
                package com.example

                class Profile(val displayName: String) {
                    companion object {
                        val DEFAULT = Profile("Default")
                    }
                }
                """);
        addJteRuntimeStubs();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("profile.kte", """
                    @import com.example.Profile
                    ${Profile.DEF<caret>}
                    """);

            chooseCompletion("DEFAULT");

            assertTopLevelFileEquals("""
                    @import com.example.Profile
                    ${Profile.DEFAULT}
                    """);
            assertNativeCompletionWasUsed();
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    public void testNativeCompletionReplaysNestedObjectMember() {
        addNestedObjectAndExtensionFixture();
        addJteRuntimeStubs();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("base.kte", """
                    @import com.example.navigation.config.PathConfig
                    ${PathConfig.Front<caret>}
                    """);

            chooseCompletion("FrontOffice");

            assertTopLevelFileEquals("""
                    @import com.example.navigation.config.PathConfig
                    ${PathConfig.FrontOffice}
                    """);
            assertNativeCompletionWasUsed();
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    public void testNativeCompletionReplaysCallReceiverMember() {
        addProfileClassWithKotlinProperties();
        addJteRuntimeStubs();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("profile.kte", """
                    @import com.example.Profile
                    @param profile: Profile
                    ${requireNotNull(profile.manager).dis<caret>}
                    """);

            chooseCompletion("displayName");

            assertTopLevelFileEquals("""
                    @import com.example.Profile
                    @param profile: Profile
                    ${requireNotNull(profile.manager).displayName}
                    """);
            assertNativeCompletionWasUsed();
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    public void testNativeCompletionReplaysBuiltinTypeInParamType() {
        addJteRuntimeStubs();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("profile.kte", """
                    @param title: Str<caret>
                    ${title}
                    """);

            chooseCompletion("String");

            assertTopLevelFileEquals("""
                    @param title: String
                    ${title}
                    """);
            assertNativeCompletionWasUsed();
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    public void testNativeCompletionReplaysAutoImportForParamType() {
        addProfileClass();
        addJteRuntimeStubs();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("profile.kte", """
                    @param profile: Pro<caret>
                    ${profile}
                    """);

            chooseCompletion("Profile");

            assertTopLevelFileEquals("""
                    @import com.example.Profile
                    @param profile: Profile
                    ${profile}
                    """);
            assertNativeCompletionWasUsed();
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    public void testNativeCompletionReplaysClassLookupInExpression() {
        myFixture.addFileToProject("src/com/example/Widget.kt", """
                package com.example

                class Widget
                """);
        addJteRuntimeStubs();

        KteNativeKotlinSourceCompletionBridge.enableDebug();
        try {
            myFixture.configureByText("widget.kte", """
                    @import com.example.Widget
                    ${Wid<caret>}
                    """);

            chooseCompletion("Widget");

            assertTopLevelFileEquals("""
                    @import com.example.Widget
                    ${Widget}
                    """);
            assertNativeCompletionWasUsed();
        } finally {
            KteNativeKotlinSourceCompletionBridge.disableDebug();
        }
    }

    private void addNoArgTopLevelCompletionFunctions() {
        myFixture.addFileToProject("src/com/example/TopLevelFunctions.kt", """
                package com.example

                fun i18n(): String = ""

                fun icon(): String = ""
                """);
    }

    private void assertTopLevelFileEquals(String expected) {
        assertEquals(
                expected,
                topLevelFileText()
        );
    }

    private void assertTopLevelFileContains(String expected) {
        String text = topLevelFileText();
        assertTrue(
                "Expected top-level file to contain '" + expected + "':\n" + text +
                        "\n\nDebug events:\n" + String.join("\n", KteNativeKotlinSourceCompletionBridge.debugEvents()),
                text.contains(expected)
        );
    }

    private void assertNativeCompletionWasUsed() {
        assertTrue(
                "Expected native K2 completion bridge to run.\n\nDebug events:\n" +
                        String.join("\n", KteNativeKotlinSourceCompletionBridge.debugEvents()),
                KteNativeKotlinSourceCompletionBridge.debugEvents().stream()
                        .anyMatch(event -> event.startsWith("directK2 "))
        );
    }
}
