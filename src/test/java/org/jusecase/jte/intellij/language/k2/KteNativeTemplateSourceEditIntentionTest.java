package org.jusecase.jte.intellij.language.k2;

import com.intellij.codeInsight.intention.IntentionAction;

import java.util.List;

public class KteNativeTemplateSourceEditIntentionTest extends KteK2FixtureSupport {
    public void testOptimizeImportsSortsAndDeduplicatesTopLevelImports() {
        addImportTargets();

        myFixture.configureByText("imports.kte", """
                @import com.example.Zed
                @import com.example.Alpha
                @import com.example.Zed

                @param title: String
                ${title}
                """);

        myFixture.launchAction(singleIntention("Optimize .kte imports"));

        assertEquals("""
                @import com.example.Alpha
                @import com.example.Zed

                @param title: String
                ${title}
                """, myFixture.getFile().getText());
    }

    public void testOptimizeImportsUnavailableForSeparatedImportBlocks() {
        addImportTargets();

        myFixture.configureByText("imports.kte", """
                @import com.example.Alpha

                @param title: String
                @import com.example.Zed
                ${title}
                """);

        List<IntentionAction> actions = myFixture.filterAvailableIntentions("Optimize .kte imports");

        assertTrue(actions.toString(), actions.isEmpty());
    }

    public void testRemoveUnresolvedImportQuickFixDeletesImportLine() {
        myFixture.configureByText("profile.kte", """
                @import com.example.<caret>MissingProfile
                @param title: String
                ${title}
                """);
        errorDescriptions();

        myFixture.launchAction(singleIntention("Remove unresolved .kte import"));

        assertEquals("""
                @param title: String
                ${title}
                """, myFixture.getFile().getText());
    }

    public void testReplaceUnresolvedImportQuickFixUsesSelectedCandidate() {
        addImportTargets();

        myFixture.configureByText("profile.kte", """
                @import com.missing.<caret>Alpha
                @param alpha: Alpha
                ${alpha}
                """);
        errorDescriptions();

        myFixture.launchAction(singleIntention("Replace import with 'com.example.Alpha'"));

        assertEquals("""
                @import com.example.Alpha
                @param alpha: Alpha
                ${alpha}
                """, myFixture.getFile().getText());
    }

    public void testAddImportForUnresolvedParamTypeQuickFixAddsAndOptimizesImport() {
        addImportTargets();

        myFixture.configureByText("profile.kte", """
                @import com.example.Zed
                @param alpha: <caret>Alpha
                ${alpha}
                """);
        errorDescriptions();

        myFixture.launchAction(singleIntention("Import 'com.example.Alpha'"));

        assertTrue(myFixture.getFile().getText(), myFixture.getFile().getText().startsWith("""
                @import com.example.Alpha
                @import com.example.Zed
                """));
    }

    public void testAmbiguousAddImportCandidatesAreSeparateQuickFixes() {
        myFixture.addFileToProject("src/com/one/DuplicateType.kt", """
                package com.one

                class DuplicateType
                """);
        myFixture.addFileToProject("src/com/two/DuplicateType.kt", """
                package com.two

                class DuplicateType
                """);

        myFixture.configureByText("profile.kte", """
                @param value: <caret>DuplicateType
                ${value}
                """);
        errorDescriptions();

        assertEquals(1, myFixture.filterAvailableIntentions("Import 'com.one.DuplicateType'").size());
        assertEquals(1, myFixture.filterAvailableIntentions("Import 'com.two.DuplicateType'").size());
    }

    public void testAddImportForK2UnresolvedReferenceQuickFixAddsImport() {
        addProfileClass();
        myFixture.addFileToProject("src/com/example/ProfileSummary.kt", """
                package com.example

                data class ProfileSummary(val headline: String, val featured: Profile)
                """);

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${<caret>ProfileSummary("Featured", profile).headline}
                """);
        errorDescriptions();

        myFixture.launchAction(singleIntention("Import 'com.example.ProfileSummary'"));

        assertEquals("""
                @import com.example.Profile
                @import com.example.ProfileSummary
                @param profile: Profile
                ${ProfileSummary("Featured", profile).headline}
                """, myFixture.getFile().getText());
    }

    public void testInsertMissingAssignmentQuickFixAddsAssignmentAndMovesCaret() {
        addProfileClass();
        addTemplateRoot();
        addCardTemplate();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param title: String
                @template.components.card(profile = profile, <caret>title =, content = @`
                    ${profile.displayName}
                `)
                """);
        errorDescriptions();

        myFixture.launchAction(singleIntention("Insert missing parameter assignment"));

        assertTrue(myFixture.getFile().getText(), myFixture.getFile().getText().contains("title = , content"));
        assertEquals(myFixture.getFile().getText().indexOf("title = ") + "title = ".length(),
                myFixture.getEditor().getCaretModel().getOffset());
    }

    public void testRemoveDuplicateTemplateParameterQuickFixRemovesLaterArgument() {
        addProfileClass();
        addTemplateRoot();
        addCardTemplate();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param title: String
                @template.components.card(profile = profile, title = title, <caret>title = profile.displayName, content = @`
                    ${profile.displayName}
                `)
                """);
        errorDescriptions();

        myFixture.launchAction(singleIntention("Remove duplicate template parameter"));

        assertFalse(myFixture.getFile().getText(), myFixture.getFile().getText().contains("title = profile.displayName"));
        assertTrue(myFixture.getFile().getText(), myFixture.getFile().getText().contains("profile = profile, title = title, content = @`"));
    }

    public void testAddMissingTemplateParametersQuickFixAppendsPlaceholdersAndMovesCaret() {
        addProfileClass();
        addTemplateRoot();
        addManualCardTemplate();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @template.components.card(<caret>)
                """);
        errorDescriptions();

        myFixture.launchAction(singleIntention("Add missing template parameters"));

        assertTrue(myFixture.getFile().getText(), myFixture.getFile().getText()
                .contains("@template.components.card(profile = , tags = )"));
        assertEquals(myFixture.getFile().getText().indexOf("profile = ") + "profile = ".length(),
                myFixture.getEditor().getCaretModel().getOffset());
    }

    private void addImportTargets() {
        myFixture.addFileToProject("src/com/example/Alpha.kt", """
                package com.example

                class Alpha
                """);
        myFixture.addFileToProject("src/com/example/Zed.kt", """
                package com.example

                class Zed
                """);
    }

    private IntentionAction singleIntention(String text) {
        List<IntentionAction> actions = myFixture.filterAvailableIntentions(text);
        assertEquals(actions.toString(), 1, actions.size());
        return actions.get(0);
    }
}
