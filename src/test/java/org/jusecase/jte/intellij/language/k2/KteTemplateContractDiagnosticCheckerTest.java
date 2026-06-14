package org.jusecase.jte.intellij.language.k2;

import java.util.List;

public class KteTemplateContractDiagnosticCheckerTest extends KteK2FixtureSupport {
    public void testValidNamedTemplateCallIsStructurallyClean() {
        addProfileClass();
        addTemplateRoot();
        addCardTemplate();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param title: String
                @template.components.card(profile = profile, title = title, content = @`
                    ${profile.displayName}
                `)
                """);

        assertTrue(contractDescriptions().toString(), contractDescriptions().isEmpty());
    }

    public void testReportsMissingParameterAssignment() {
        addProfileClass();
        addTemplateRoot();
        addCardTemplate();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @template.components.card(profile = profile, title = , content = @`
                    ${profile.displayName}
                `)
                """);

        assertContainsDescription(contractDescriptions(), "Missing parameter assignment");
    }

    public void testOnlyDeterministicNativeContractProblemsAreReportedByStructuralChecker() {
        addProfileClassWithKotlinProperties();
        addTemplateRoot();
        addManualCardTemplate();
        addTagListTemplate();

        myFixture.configureByText("template-positionals.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param title: String
                @template.components.card(title = title, title = profile.displayName, unknown = title)
                @template.components.card(title, profile.tags)
                @template.components.card(profile)
                @template.components.tagList(profile.tags, profile.tags)
                """);

        List<String> descriptions = contractDescriptions();

        assertContainsDescription(descriptions, "Duplicate parameter title");
        assertTrue(descriptions.toString(), descriptions.stream()
                .anyMatch(description -> description.startsWith("Missing required parameters:")));
        assertFalse(descriptions.toString(), descriptions.stream().anyMatch(description ->
                description.contains("No value passed for parameter") ||
                        description.contains("No parameter with name") ||
                        description.contains("Too many arguments") ||
                        description.contains("Argument type mismatch")));
    }

    private List<String> contractDescriptions() {
        return new KteTemplateContractDiagnosticChecker().collect(myFixture.getFile())
                .stream()
                .map(KteSyntheticKotlinDiagnosticCollector.Diagnostic::message)
                .toList();
    }
}
