package org.jusecase.jte.intellij.language.k2;

import java.util.List;

public class KteSyntheticKotlinDiagnosticAnnotatorTest extends KteK2FixtureSupport {
    public void testValidSimpleExpressionsDoNotReportSyntheticDiagnostics() {
        addProfileClass();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param profiles: List<Profile>
                @if(profile.active)
                    ${profile.displayName}
                @endif
                @for(item in profiles)
                    ${item.displayName}
                @endfor
                """);

        List<String> descriptions = errorDescriptions();

        assertDoesNotContainDescriptionContaining(descriptions, "Unresolved reference", "displayName");
        assertDoesNotContainDescriptionContaining(descriptions, "Unresolved reference", "active");
        assertDoesNotContainDescriptionContaining(descriptions, "Unresolved reference", "item");
    }

    public void testReportsUnresolvedOutputProperty() {
        addProfileClass();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.missingName}
                """);

        assertContainsDescriptionContaining(errorDescriptions(), "Unresolved reference", "missingName");
    }

    public void testReportsUnresolvedImportAndParamType() {
        myFixture.configureByText("profile.kte", """
                @import com.example.MissingProfile
                @param profile: MissingProfile
                ${profile}
                """);

        List<String> descriptions = errorDescriptions();

        assertContainsDescriptionContaining(descriptions, "[UNRESOLVED_IMPORT]", "Unresolved reference 'com'");
        assertContainsDescriptionContaining(descriptions, "[UNRESOLVED_REFERENCE]", "Unresolved reference 'MissingProfile'");
    }

    public void testLocalStatementVariableDoesNotReportFalseErrorsAfterDeclaration() {
        addCareOfferingFixture();
        addTemplateRoot();
        addCareOfferingSectionTemplate();

        myFixture.configureByText("facility.kte", """
                @import com.example.Page
                @param page: Page
                @if(page.careOfferingForm != null)
                    !{val careOfferingForm = requireNotNull(page.careOfferingForm)}
                    @template.components.care_offering_form_section(form = careOfferingForm)
                    ${careOfferingForm.displayName}
                @endif
                """);

        List<String> descriptions = errorDescriptions();

        assertDoesNotContainDescription(descriptions, "Unresolved reference: careOfferingForm");
        assertFalse("Did not expect local template argument type errors in " + descriptions,
                descriptions.stream().anyMatch(description -> description.contains("cannot be cast")));
    }

    public void testObjectMemberExtensionImportAndHtmlLocalsDoNotReportFalseErrors() {
        addNestedObjectAndExtensionFixture();

        myFixture.configureByText("base.kte", """
                @import com.example.navigation.config.PathConfig
                @import com.example.navigation.breadcrumb.Breadcrumb
                @import com.example.navigation.routing.RoutingUtils
                @import com.example.navigation.routing.RoutingUtils.isCurrentPage
                @param breadcrumbs: List<Breadcrumb>?
                @param isLoggedIn: Boolean

                !{val isCurrentHome = breadcrumbs.isCurrentPage(PathConfig.FrontOffice.HOME)}
                <header>
                    <div>
                        !{val logoTitle = if (isLoggedIn) "home" else "start"}
                        <a title="${logoTitle}" href="${RoutingUtils.getDefaultPath(isLoggedIn)}"></a>
                    </div>
                </header>
                <footer>
                    <ul>
                        <li>
                            !{val link = if (isLoggedIn) PathConfig.BackOffice.GDPR else PathConfig.FrontOffice.GDPR}
                            <a href="${link}">GDPR</a>
                        </li>
                    </ul>
                </footer>
                """);

        List<String> descriptions = errorDescriptions();

        assertDoesNotContainDescription(descriptions,
                "Unresolved import: com.example.navigation.routing.RoutingUtils.isCurrentPage");
        assertDoesNotContainDescriptionContaining(descriptions, "Unresolved reference", "FrontOffice");
        assertDoesNotContainDescriptionContaining(descriptions, "Unresolved reference", "BackOffice");
        assertDoesNotContainDescriptionContaining(descriptions, "Unresolved reference", "getDefaultPath");
        assertDoesNotContainDescriptionContaining(descriptions, "Unresolved reference", "logoTitle");
        assertDoesNotContainDescriptionContaining(descriptions, "Unresolved reference", "link");
    }

    public void testDelegatesAnalysisApiWarningsForUnusedStatementVariables() {
        addProfileClassWithKotlinProperties();

        myFixture.configureByText("warnings.kte", """
                @import com.example.Profile
                @param profile: Profile

                !{ val ignored2 = profile.active || profile.manager != null || true }
                """);

        assertTrue("Expected Analysis API unused variable warning in " + warningDescriptions(),
                warningDescriptions().stream().anyMatch(description ->
                        description.contains("[UNUSED_VARIABLE]") ||
                                description.contains("Variable is unused")));
    }

    public void testProfileFixtureStyleTemplateCallsDoNotReportFalseContractDiagnostics() {
        addProfileClassWithKotlinProperties();
        addTemplateRoot();
        addLayoutTemplate();
        addHeaderTemplate();
        addManualCardTemplate();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param profiles: List<Profile>
                @param title: String = "K2 profile"
                @template.layout(title = title, content = @`
                    <section>
                        @template.components.header(title = title)
                        @template.components.card(profile = profile, tags = profile.tags)
                    </section>
                `)
                """);

        assertNoContractErrors();
    }

    public void testReportsTemplateCallParamContractErrors() {
        addProfileClass();
        addTemplateRoot();
        addCardTemplate();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param title: String
                @template.components.card(profile = profile, title = title, title = profile.displayName, unknown = title, content = @`
                    ${profile.displayName}
                `)
                """);

        List<String> descriptions = errorDescriptions();

        assertContainsDescriptionContaining(descriptions, "Duplicate parameter title");
        assertContainsDescriptionContaining(descriptions, "No parameter with name", "unknown");
    }

    private void assertContainsDescriptionContaining(List<String> descriptions, String... fragments) {
        assertTrue("Expected description containing " + List.of(fragments) + " in " + descriptions,
                descriptions.stream().anyMatch(description -> containsAll(description, fragments)));
    }

    private void assertDoesNotContainDescriptionContaining(List<String> descriptions, String... fragments) {
        assertFalse("Did not expect description containing " + List.of(fragments) + " in " + descriptions,
                descriptions.stream().anyMatch(description -> containsAll(description, fragments)));
    }

    private boolean containsAll(String description, String... fragments) {
        for (String fragment : fragments) {
            if (!description.contains(fragment)) {
                return false;
            }
        }
        return true;
    }
}
