package org.jusecase.jte.intellij.language.k2;

import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;

import java.util.List;

public class KteSyntheticKotlinDiagnosticCollectorTest extends KteK2FixtureSupport {
    public void testMapsOutputSyntaxErrorToTemplateRange() {
        myFixture.configureByText("syntax-output.kte", """
                @param profile: String
                ${profile #}
                """);

        assertKotlinSyntaxDiagnosticIntersects("profile #");
    }

    public void testMapsIfConditionSyntaxErrorToTemplateRange() {
        myFixture.configureByText("syntax-if.kte", """
                @param profile: String
                @if(profile #)
                    ${profile}
                @endif
                """);

        assertKotlinSyntaxDiagnosticIntersects("profile #");
    }

    public void testMapsStatementSyntaxErrorToTemplateRange() {
        myFixture.configureByText("syntax-statement.kte", """
                @param profile: String
                !{val displayName = profile #}
                """);

        assertKotlinSyntaxDiagnosticIntersects("profile #");
    }

    public void testMapsTemplateArgumentSyntaxErrorToTemplateRange() {
        addProfileClass();
        addTemplateRoot();
        addCardTemplate();

        myFixture.configureByText("syntax-template-argument.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param title: String
                @template.components.card(profile = profile #, title = title, content = @`
                    ${title}
                `)
                """);

        assertKotlinSyntaxDiagnosticIntersects("profile #");
    }

    public void testGeneratedWrapperOnlySyntaxErrorsAreSuppressed() {
        myFixture.configureByText("wrapper-only.kte", """
                @endif
                """);

        assertFalse("Generated wrapper-only syntax errors should not surface: " + diagnostics(),
                diagnostics().stream().anyMatch(this::isKotlinSyntaxError));
    }

    public void testTextOnlyAndImportOnlyTemplatesStayClean() {
        myFixture.configureByText("text-only.kte", """
                <p>Hello</p>
                """);
        assertTrue(diagnostics().toString(), diagnostics().isEmpty());

        addProfileClass();
        myFixture.configureByText("import-only.kte", """
                @import com.example.Profile
                """);
        assertTrue(diagnostics().toString(), diagnostics().isEmpty());
    }

    public void testTemplateContractDiagnosticIsNotDuplicatedByAggregation() {
        addProfileClassWithKotlinProperties();
        addTemplateRoot();
        addManualCardTemplate();

        myFixture.configureByText("contract.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param title: String
                @template.components.card(profile = title, tags = profile.tags)
                """);

        List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics = diagnostics();
        List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> typeMismatches = diagnostics.stream()
                .filter(this::isTemplateArgumentTypeMismatch)
                .toList();

        assertEquals(diagnostics.toString(), 1, typeMismatches.size());
        assertEquals(KteSyntheticKotlinDiagnosticCollector.Origin.SYNTHETIC_KOTLIN, typeMismatches.get(0).origin());
        assertTrue(typeMismatches.get(0).templateRange().intersects(sourceRange("profile = title")));
    }

    public void testK2ReportsTemplateCallNameAndArityDiagnostics() {
        addProfileClassWithKotlinProperties();
        addTemplateRoot();
        addCardTemplate();
        addTagListTemplate();

        myFixture.configureByText("contract.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param title: String
                @template.components.card(title = title, title = profile.displayName, unknown = title)
                @template.components.tagList(profile.tags, profile.tags)
                """);

        List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics = diagnostics();

        assertHasNativeTemplateStructureDiagnostic(diagnostics, "Missing required parameters", "Add missing template parameters");
        assertHasNativeTemplateStructureDiagnostic(diagnostics, "Duplicate parameter title", "Remove duplicate template parameter");
        assertHasK2TemplateDiagnostic(diagnostics, "No parameter with name");
        assertHasK2TemplateDiagnostic(diagnostics, "Too many arguments");
    }

    public void testValidNestedContractDoesNotReportKotlinDefaultTypeDiagnostics() {
        addJteRuntimeStubs();
        addProfileClassWithKotlinProperties();
        addTemplateRoot();
        addLayoutTemplate();
        addProfileSummaryTemplate();
        addTagListTemplate();
        addManualCardTemplate();

        myFixture.configureByText("contract-valid.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param profiles: List<Profile>
                @param title: String

                @template.layout(title = title, content = @`
                    <section>
                        @template.components.profileSummary(
                            profile = profile,
                            title = title,
                            optionalEmail = profile.email,
                            tags = profile.tags,
                            content = @`
                                <p>${profile.displayName}</p>
                                @template.components.tagList(tags = profile.tags)
                            `
                        )

                        <ul>
                            @for(item in profiles)
                                @template.components.card(profile = item, tags = item.tags)
                            @endfor
                        </ul>
                    </section>
                `)
                """);

        List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics = diagnostics();

        assertFalse("Did not expect Kotlin default type diagnostics in " + diagnostics,
                diagnostics.stream().anyMatch(this::isKotlinDefaultTypeDiagnostic));
    }

    public void testK2DiagnosticsRemainDiagnosticOnly() {
        myFixture.configureByText("profile.kte", """
                ${missingProfile}
                """);

        KteSyntheticKotlinDiagnosticCollector.Diagnostic diagnostic = diagnostics().stream()
                .filter(candidate -> isK2Diagnostic(candidate) && candidate.message().contains("Unresolved reference"))
                .findFirst()
                .orElseThrow();

        assertTrue(diagnostic.toString(), diagnostic.fixes().isEmpty());
    }

    public void testK2UnresolvedReferenceCanExposeNativeAddImportFix() {
        addProfileClass();
        myFixture.addFileToProject("src/com/example/ProfileSummary.kt", """
                package com.example

                data class ProfileSummary(val headline: String, val featured: Profile)
                """);

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${ProfileSummary("Featured", profile).headline}
                """);

        KteSyntheticKotlinDiagnosticCollector.Diagnostic diagnostic = diagnostics().stream()
                .filter(candidate -> isK2Diagnostic(candidate) && candidate.message().contains("Unresolved reference"))
                .findFirst()
                .orElseThrow();

        assertTrue(diagnostic.fixes().toString(), diagnostic.fixes().stream()
                .anyMatch(fix -> "Import 'com.example.ProfileSummary'".equals(fix.getText())));
    }

    public void testK2ImportDiagnosticsExposeNativeImportFixes() {
        myFixture.configureByText("profile.kte", """
                @import com.example.MissingProfile
                @param title: String
                ${title}
                """);

        KteSyntheticKotlinDiagnosticCollector.Diagnostic diagnostic = diagnostics().stream()
                .filter(candidate -> isK2Diagnostic(candidate) && candidate.message().contains("[UNRESOLVED_IMPORT]"))
                .findFirst()
                .orElseThrow();

        assertEquals(KteSyntheticKotlinDiagnosticCollector.Origin.SYNTHETIC_KOTLIN, diagnostic.origin());
        assertTrue(diagnostic.fixes().toString(), diagnostic.fixes().stream()
                .anyMatch(fix -> "Remove unresolved .kte import".equals(fix.getText())));
    }

    public void testK2ReportsTemplateCallGenericContentAndNullabilityDiagnostics() {
        addJteRuntimeStubs();
        addProfileClassWithKotlinProperties();
        addTemplateRoot();
        addCardTemplate();

        myFixture.configureByText("contract.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param profiles: List<Profile>
                @param title: String
                @template.components.card(profile = profile.manager, title = @`
                    ${profile.displayName}
                `, tags = profiles, content = title)
                """);

        List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics = diagnostics();

        assertHasK2TemplateDiagnostic(diagnostics, "Argument type mismatch");
        assertTrue("Expected nullable Profile? mismatch in " + diagnostics,
                diagnostics.stream().anyMatch(diagnostic ->
                        isK2Diagnostic(diagnostic) &&
                                diagnostic.message().contains("Profile?") &&
                                diagnostic.message().contains("Profile")));
        assertTrue("Expected generic List<Profile> mismatch in " + diagnostics,
                diagnostics.stream().anyMatch(diagnostic ->
                        isK2Diagnostic(diagnostic) &&
                                diagnostic.message().contains("List<Profile>") &&
                                diagnostic.message().contains("List<String>")));
        assertTrue("Expected content/String mismatch in " + diagnostics,
                diagnostics.stream().anyMatch(diagnostic ->
                        isK2Diagnostic(diagnostic) &&
                                diagnostic.message().contains("String") &&
                                diagnostic.message().contains("Content")));
    }

    public void testResolvedTemplateCallIsModeledAsTypedKotlinFunctionCall() {
        addProfileClassWithKotlinProperties();
        addTemplateRoot();
        addManualCardTemplate();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @template.components.card(profile = profile, tags = profile.tags)
                """);

        String syntheticText = KteSyntheticKotlinModelService.getInstance(getProject())
                .getModel(myFixture.getFile())
                .getSyntheticFile()
                .getText();

        assertTrue(syntheticText, syntheticText.contains("fun __jte_template_"));
        assertTrue(syntheticText, syntheticText.contains("profile: Profile"));
        assertTrue(syntheticText, syntheticText.contains("tags: List<String>"));
        assertTrue(syntheticText, syntheticText.contains("profile = profile"));
        assertTrue(syntheticText, syntheticText.contains("tags = profile.tags"));
    }

    public void testChildTemplateOnlyImportIsQualifiedInSyntheticStub() {
        addProfileClassWithKotlinProperties();
        addTemplateRoot();
        addManualCardTemplate();

        myFixture.configureByText("profile.kte", """
                @param title: String
                @param tags: List<String>
                @template.components.card(profile = title, tags = tags)
                """);

        String syntheticText = KteSyntheticKotlinModelService.getInstance(getProject())
                .getModel(myFixture.getFile())
                .getSyntheticFile()
                .getText();
        List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics = diagnostics();

        assertTrue(syntheticText, syntheticText.contains("profile: com.example.Profile"));
        assertTrue("Expected Kotlin-backed child-template type mismatch in " + diagnostics,
                diagnostics.stream().anyMatch(diagnostic ->
                        diagnostic.origin().equals(KteSyntheticKotlinDiagnosticCollector.Origin.SYNTHETIC_KOTLIN) &&
                                isTemplateArgumentTypeMismatch(diagnostic) &&
                                diagnostic.templateRange().intersects(sourceRange("profile = title"))));
    }

    public void testResolvedTemplateCallModelsDefaultsContentAndVarargs() {
        addProfileClassWithKotlinProperties();
        addTemplateRoot();
        addSignatureTemplate();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @template.components.signatureKitchenSink(profile = profile, tags = profile.tags, content = @`
                    ${profile.displayName}
                `, labels = "featured")
                """);

        String syntheticText = KteSyntheticKotlinModelService.getInstance(getProject())
                .getModel(myFixture.getFile())
                .getSyntheticFile()
                .getText();

        assertTrue(syntheticText, syntheticText.contains("title: String = \"\""));
        assertTrue(syntheticText, syntheticText.contains("content: gg.jte.Content"));
        assertTrue(syntheticText, syntheticText.contains("vararg labels: String"));
        assertTrue(syntheticText, syntheticText.contains("content = object : gg.jte.Content"));
        assertTrue(syntheticText, syntheticText.contains("labels = \"featured\""));
    }

    public void testDelegatesKotlinNullableReceiverDiagnosticsFromSyntheticFile() {
        addProfileClassWithKotlinProperties();

        myFixture.configureByText("nullable.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.manager.displayName}
                """);

        TextRange expectedRange = sourceRange("profile.manager.displayName");
        List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics = diagnostics();

        assertTrue("Expected Kotlin-backed nullable diagnostic in " + diagnostics,
                diagnostics.stream().anyMatch(diagnostic ->
                        diagnostic.origin().equals(KteSyntheticKotlinDiagnosticCollector.Origin.SYNTHETIC_KOTLIN) &&
                                diagnostic.message().contains("Only safe (?.) or non-null asserted") &&
                                diagnostic.templateRange().intersects(expectedRange)));
        assertEquals("Expected fallback nullable diagnostic to be suppressed by Kotlin-backed diagnostic: " + diagnostics,
                1,
                diagnostics.stream()
                        .filter(diagnostic -> diagnostic.message().contains("Only safe (?.) or non-null asserted"))
                        .count());
    }

    public void testDelegatedDiagnosticsUseSourceRootContextForKteOutsideKotlinSourceRoot() {
        myFixture.addFileToProject("src/main/kotlin/com/example/Profile.kt", """
                package com.example

                data class Profile(
                    val displayName: String,
                    val active: Boolean,
                )
                """);
        PsiFile file = myFixture.addFileToProject("src/main/jte/profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.displayName}
                @if(profile.active)
                    ${profile.displayName}
                @endif
                """);
        myFixture.configureFromExistingVirtualFile(file.getVirtualFile());

        List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics = diagnostics();

        assertFalse("Did not expect delegated unresolved member diagnostics in " + diagnostics,
                diagnostics.stream().anyMatch(diagnostic ->
                        diagnostic.origin().equals(KteSyntheticKotlinDiagnosticCollector.Origin.SYNTHETIC_KOTLIN) &&
                                diagnostic.message().contains("Unresolved reference")));
    }

    public void testDelegatesKotlinConditionTypeDiagnosticsFromSyntheticFile() {
        addProfileClassWithKotlinProperties();

        myFixture.configureByText("conditions.kte", """
                @import com.example.Profile
                @param profile: Profile
                @if(profile.displayName)
                    ${profile.displayName}
                @endif
                """);

        TextRange expectedRange = sourceRange("profile.displayName");
        List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics = diagnostics();

        assertTrue("Expected Kotlin-backed condition diagnostic in " + diagnostics,
                diagnostics.stream().anyMatch(diagnostic ->
                        diagnostic.origin().equals(KteSyntheticKotlinDiagnosticCollector.Origin.SYNTHETIC_KOTLIN) &&
                                diagnostic.message().contains("Condition type mismatch") &&
                                diagnostic.templateRange().intersects(expectedRange)));
        assertFalse("Fallback condition diagnostic should be suppressed by Kotlin-backed diagnostic: " + diagnostics,
                diagnostics.stream().anyMatch(diagnostic ->
                        diagnostic.message().startsWith("Condition must be Boolean")));
    }

    public void testDelegatesAnalysisApiWarningsFromSyntheticKotlin() {
        addProfileClassWithKotlinProperties();

        myFixture.configureByText("unused-local.kte", """
                @import com.example.Profile
                @param profile: Profile

                !{ val ignored2 = profile.active || profile.manager != null || true }
                """);

        List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics = diagnostics();

        assertTrue("Expected Analysis API warning mapped from synthetic Kotlin in " + diagnostics,
                diagnostics.stream().anyMatch(diagnostic ->
                        diagnostic.origin().equals(KteSyntheticKotlinDiagnosticCollector.Origin.SYNTHETIC_KOTLIN) &&
                                (diagnostic.message().contains("[UNUSED_VARIABLE]") ||
                                        diagnostic.message().contains("Variable is unused")) &&
                                diagnostic.templateRange().intersects(sourceRange("ignored2"))));
    }

    public void testDiagnosticSinkKeepsKotlinBackedDiagnosticOverFallbackDuplicate() {
        KteSyntheticKotlinDiagnosticSink sink = new KteSyntheticKotlinDiagnosticSink();

        sink.add(new KteSyntheticKotlinDiagnosticCollector.Diagnostic(
                HighlightSeverity.ERROR,
                "[UNSAFE_CALL] Only safe (?.) or non-null asserted (!!.) calls are allowed",
                TextRange.create(10, 30),
                KteSyntheticKotlinDiagnosticCollector.Origin.SYNTHETIC_KOTLIN
        ));
        sink.add(HighlightSeverity.ERROR,
                "Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver",
                TextRange.create(14, 21));

        List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics = sink.diagnostics();

        assertEquals(diagnostics.toString(), 1, diagnostics.size());
        assertEquals(KteSyntheticKotlinDiagnosticCollector.Origin.SYNTHETIC_KOTLIN, diagnostics.get(0).origin());
        assertTrue(diagnostics.get(0).message(), diagnostics.get(0).message().startsWith("[UNSAFE_CALL]"));
    }

    public void testDiagnosticSinkDeduplicatesExactDiagnosticsAndKeepsNarrowerRange() {
        KteSyntheticKotlinDiagnosticSink sink = new KteSyntheticKotlinDiagnosticSink();

        sink.add(HighlightSeverity.ERROR, "Unresolved reference: profile", TextRange.create(10, 30));
        sink.add(HighlightSeverity.ERROR, "Unresolved reference: profile", TextRange.create(14, 21));
        sink.add(HighlightSeverity.ERROR, "Unresolved reference: email", TextRange.create(14, 21));

        List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics = sink.diagnostics();

        assertEquals(diagnostics.toString(), 2, diagnostics.size());
        assertTrue(diagnostics.stream().anyMatch(diagnostic ->
                "Unresolved reference: profile".equals(diagnostic.message()) &&
                        TextRange.create(14, 21).equals(diagnostic.templateRange())));
        assertTrue(diagnostics.stream().anyMatch(diagnostic ->
                "Unresolved reference: email".equals(diagnostic.message())));
    }

    private void assertKotlinSyntaxDiagnosticIntersects(String expectedSourceText) {
        TextRange expectedRange = sourceRange(expectedSourceText);
        List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics = diagnostics();

        assertTrue("Expected mapped Kotlin syntax diagnostic intersecting '" + expectedSourceText + "' in " + diagnostics,
                diagnostics.stream().anyMatch(diagnostic ->
                        isKotlinSyntaxError(diagnostic) &&
                                diagnostic.templateRange().intersects(expectedRange)));
    }

    private boolean isKotlinSyntaxError(KteSyntheticKotlinDiagnosticCollector.Diagnostic diagnostic) {
        return diagnostic.message().startsWith("Kotlin syntax error:");
    }

    private boolean isTemplateArgumentTypeMismatch(KteSyntheticKotlinDiagnosticCollector.Diagnostic diagnostic) {
        return diagnostic.message().contains("Argument type mismatch") ||
                diagnostic.message().contains("actual type is") && diagnostic.message().contains("Profile") ||
                diagnostic.message().contains("cannot be cast to Profile");
    }

    private boolean isKotlinDefaultTypeDiagnostic(KteSyntheticKotlinDiagnosticCollector.Diagnostic diagnostic) {
        String message = diagnostic.message();
        return message.contains("PLATFORM_CLASS_MAPPED_TO_KOTLIN") ||
                message.contains("This class is not recommended for use in Kotlin") ||
                message.contains("Unresolved reference") && message.contains("List");
    }

    private void assertHasK2TemplateDiagnostic(List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics,
                                               String messageFragment) {
        assertTrue("Expected K2 diagnostic containing '" + messageFragment + "' in " + diagnostics,
                diagnostics.stream().anyMatch(diagnostic ->
                        isK2Diagnostic(diagnostic) && diagnostic.message().contains(messageFragment)));
    }

    private void assertHasNativeTemplateStructureDiagnostic(
            List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics,
            String messageFragment,
            String fixText) {
        assertTrue("Expected native template-structure diagnostic containing '" + messageFragment + "' in " + diagnostics,
                diagnostics.stream().anyMatch(diagnostic ->
                        diagnostic.origin().equals(KteSyntheticKotlinDiagnosticCollector.Origin.TEMPLATE_STRUCTURE) &&
                                diagnostic.message().contains(messageFragment) &&
                                diagnostic.fixes().stream().anyMatch(fix -> fixText.equals(fix.getText()))));
    }

    private boolean isK2Diagnostic(KteSyntheticKotlinDiagnosticCollector.Diagnostic diagnostic) {
        return diagnostic.origin().equals(KteSyntheticKotlinDiagnosticCollector.Origin.SYNTHETIC_KOTLIN);
    }

    private List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics() {
        return new KteSyntheticKotlinDiagnosticCollector().collect(myFixture.getFile());
    }

    private TextRange sourceRange(String text) {
        int offset = myFixture.getFile().getText().indexOf(text);
        assertTrue("Expected source text: " + text, offset >= 0);
        return TextRange.from(offset, text.length());
    }
}
