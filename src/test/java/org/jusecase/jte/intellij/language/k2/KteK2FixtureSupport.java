package org.jusecase.jte.intellij.language.k2;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.kotlin.analysis.api.permissions.KaAnalysisPermissionRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

abstract class KteK2FixtureSupport extends LightJavaCodeInsightFixtureTestCase {
    private boolean previousAnalysisAllowedOnEdt;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        addKotlinStdlibToModule();
        KaAnalysisPermissionRegistry registry = KaAnalysisPermissionRegistry.Companion.getInstance();
        previousAnalysisAllowedOnEdt = registry.isAnalysisAllowedOnEdt();
        registry.setAnalysisAllowedOnEdt(true);
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            KaAnalysisPermissionRegistry.Companion.getInstance().setAnalysisAllowedOnEdt(previousAnalysisAllowedOnEdt);
        } finally {
            super.tearDown();
        }
    }

    private void addKotlinStdlibToModule() {
        Path kotlinLib = Path.of(PathManager.getHomePath(), "plugins", "Kotlin", "kotlinc", "lib");
        if (!Files.exists(kotlinLib.resolve("kotlin-stdlib.jar"))) {
            return;
        }

        PsiTestUtil.addLibrary(
                myFixture.getModule(),
                "kotlin-stdlib",
                kotlinLib.toString(),
                "kotlin-stdlib.jar",
                "kotlin-stdlib-jdk7.jar",
                "kotlin-stdlib-jdk8.jar"
        );
    }

    protected void addProfileClass() {
        myFixture.addFileToProject("src/com/example/Profile.kt", """
                package com.example

                data class Profile(
                    val displayName: String,
                    val active: Boolean,
                )
                """);
    }

    protected void addProfileClassWithKotlinProperties() {
        myFixture.addFileToProject("src/com/example/Profile.kt", """
                package com.example

                data class Profile(
                    val displayName: String,
                    val email: String,
                    val active: Boolean,
                    val tags: List<String>,
                    val manager: Profile? = null,
                )
                """);
    }

    protected void addProfileJavaClass() {
        myFixture.addClass("""
                package com.example;

                import java.util.List;

                public class Profile {
                    public String getDisplayName() {
                        return "";
                    }

                    public String getEmail() {
                        return "";
                    }

                    public boolean isActive() {
                        return true;
                    }

                    public List<String> getTags() {
                        return List.of();
                    }

                    public Profile getManager() {
                        return null;
                    }
                }
                """);
    }

    protected void addCareOfferingFixture() {
        myFixture.addFileToProject("src/com/example/CareOfferingForm.kt", """
                package com.example

                data class CareOfferingForm(val displayName: String)
                """);
        myFixture.addFileToProject("src/com/example/Page.kt", """
                package com.example

                data class Page(
                    val careOfferingForm: CareOfferingForm?,
                    val forms: List<CareOfferingForm>,
                )
                """);
    }

    protected void addSupportHelpers() {
        myFixture.addFileToProject("src/com/example/SupportHelpers.kt", """
                package com.example

                fun i18n(key: String, vararg args: Any): String = key

                enum class HiddenHttpMethod(val value: String) {
                    PUT("put"),
                    DELETE("delete"),
                }

                object UiLabels {
                    val title: String = "Title"
                }

                class ProfileFactory {
                    companion object {
                        fun defaultTitle(): String = "Default"
                    }
                }
                """);
    }

    protected void addJteRuntimeStubs() {
        myFixture.addFileToProject("src/gg/jte/RuntimeStubs.kt", """
                package gg.jte

                fun interface Content {
                    fun writeTo()
                }

                class TemplateOutput {
                    fun writeUserContent(value: Any?) {}
                }
                """);
    }

    protected void addNestedObjectAndExtensionFixture() {
        myFixture.addFileToProject("src/com/example/navigation/breadcrumb/Breadcrumb.kt", """
                package com.example.navigation.breadcrumb

                data class Breadcrumb(val url: String)
                """);
        myFixture.addFileToProject("src/com/example/navigation/config/PathConfig.kt", """
                package com.example.navigation.config

                object PathConfig {
                    object FrontOffice {
                        const val HOME: String = "/"
                        const val TOOLS: String = "/tools"
                        const val ACCESSIBILITY: String = "/accessibility"
                        const val GDPR: String = "/gdpr"
                    }

                    object BackOffice {
                        const val DASHBOARD: String = "/dashboard"
                        const val ACCESSIBILITY: String = "/admin/accessibility"
                        const val GDPR: String = "/admin/gdpr"
                    }
                }
                """);
        myFixture.addFileToProject("src/com/example/navigation/routing/RoutingUtils.kt", """
                package com.example.navigation.routing

                import com.example.navigation.breadcrumb.Breadcrumb

                object RoutingUtils {
                    fun List<Breadcrumb>?.isCurrentPage(path: String, vararg paths: String): Boolean =
                        this?.any { it.url == path || it.url in paths } ?: false

                    fun getDefaultPath(isLoggedIn: Boolean): String =
                        if (isLoggedIn) "/dashboard" else "/"
                }
                """);
    }

    protected void addTemplateRoot() {
        myFixture.addFileToProject(".jteroot", "");
    }

    protected void addCareOfferingSectionTemplate() {
        myFixture.addFileToProject("components/care_offering_form_section.kte", """
                @import com.example.CareOfferingForm
                @param form: CareOfferingForm

                <section>${form.displayName}</section>
                """);
    }

    protected void addCardTemplate() {
        myFixture.addFileToProject("components/card.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param title: String
                @param tags: List<String> = emptyList()
                @param content: gg.jte.Content

                <article>
                    <h2>${title}</h2>
                    <p>${profile.displayName}</p>
                    ${content}
                </article>
                """);
    }

    protected void addManualCardTemplate() {
        myFixture.addFileToProject("components/card.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param tags: List<String>

                <article>
                    <h3>${profile.displayName}</h3>
                    @for(tag in tags)
                        <span>${tag}</span>
                    @endfor
                </article>
                """);
    }

    protected void addLayoutTemplate() {
        myFixture.addFileToProject("layout.kte", """
                @param title: String
                @param content: gg.jte.Content

                <!doctype html>
                <html lang="en">
                <body>
                    ${content}
                </body>
                </html>
                """);
    }

    protected void addHeaderTemplate() {
        myFixture.addFileToProject("components/header.kte", """
                @param title: String

                <header>
                    <h2>${title}</h2>
                </header>
                """);
    }

    protected void addProfileSummaryTemplate() {
        myFixture.addFileToProject("components/profileSummary.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param title: String
                @param optionalEmail: String? = null
                @param tags: List<String>
                @param content: gg.jte.Content

                <article>
                    <h3>${title}</h3>
                    @template.components.tagList(tags = tags)
                    ${content}
                </article>
                """);
    }

    protected void addTagListTemplate() {
        myFixture.addFileToProject("components/tagList.kte", """
                @param tags: List<String>

                <ul>
                    @for(tag in tags)
                        <li>${tag}</li>
                    @endfor
                </ul>
                """);
    }

    protected void addSignatureTemplate() {
        myFixture.addFileToProject("components/signatureKitchenSink.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param title: String = "Profile"
                @param tags: List<String>
                @param content: gg.jte.Content
                @param vararg labels: String
                """);
    }

    protected List<String> errorDescriptions() {
        return myFixture.doHighlighting(HighlightSeverity.ERROR)
                .stream()
                .map(HighlightInfo::getDescription)
                .filter(Objects::nonNull)
                .toList();
    }

    protected List<String> warningDescriptions() {
        return myFixture.doHighlighting(HighlightSeverity.WARNING)
                .stream()
                .map(HighlightInfo::getDescription)
                .filter(Objects::nonNull)
                .toList();
    }

    protected void assertContainsDescription(String description) {
        assertContainsDescription(errorDescriptions(), description);
    }

    protected void assertContainsDescription(List<String> descriptions, String description) {
        assertTrue("Expected " + description + " in " + descriptions, descriptions.contains(description));
    }

    protected void assertDoesNotContainDescription(String description) {
        assertDoesNotContainDescription(errorDescriptions(), description);
    }

    protected void assertDoesNotContainDescription(List<String> descriptions, String description) {
        assertFalse("Did not expect " + description + " in " + descriptions, descriptions.contains(description));
    }

    protected void assertContainsWarningDescription(String description) {
        List<String> descriptions = warningDescriptions();
        assertTrue("Expected warning " + description + " in " + descriptions, descriptions.contains(description));
    }

    protected void assertDoesNotContainWarningDescription(String description) {
        List<String> descriptions = warningDescriptions();
        assertFalse("Did not expect warning " + description + " in " + descriptions, descriptions.contains(description));
    }

    protected void assertNoContractErrors() {
        List<String> descriptions = errorDescriptions();
        assertFalse("Did not expect missing parameter errors in " + descriptions,
                descriptions.stream().anyMatch(description -> description.startsWith("Missing required parameters") ||
                        description.contains("No value passed for parameter")));
        assertFalse("Did not expect duplicate parameter errors in " + descriptions,
                descriptions.stream().anyMatch(description -> description.startsWith("Duplicate parameter") ||
                        description.contains("already passed")));
        assertFalse("Did not expect unknown parameter errors in " + descriptions,
                descriptions.stream().anyMatch(description -> description.startsWith("Unknown parameter") ||
                        description.contains("No parameter with name")));
        assertFalse("Did not expect arity errors in " + descriptions,
                descriptions.stream().anyMatch(description -> description.startsWith("Too many positional arguments") ||
                        description.contains("Too many arguments")));
        assertFalse("Did not expect template type errors in " + descriptions,
                descriptions.stream().anyMatch(description -> description.contains(" cannot be cast to ") ||
                        description.contains("Argument type mismatch")));
    }

    protected Set<String> completeBasicLookupStrings() {
        LookupElement[] elements = myFixture.completeBasic();
        assertNotNull(elements);
        return lookupStrings(elements);
    }

    protected Set<String> lookupStrings(LookupElement[] elements) {
        return Arrays.stream(elements)
                .filter(Objects::nonNull)
                .map(LookupElement::getLookupString)
                .collect(Collectors.toSet());
    }

    protected String topLevelFileText() {
        PsiFile file = myFixture.getFile();
        PsiFile topLevelFile = InjectedLanguageManager.getInstance(getProject()).getTopLevelFile(file);
        return topLevelFile.getText();
    }

    protected void assertContainsLookup(Set<String> lookupStrings, String lookupString) {
        assertTrue("Expected lookup '" + lookupString + "' in " + lookupStrings, lookupStrings.contains(lookupString));
    }

    protected void assertDoesNotContainLookup(Set<String> lookupStrings, String lookupString) {
        assertFalse("Did not expect lookup '" + lookupString + "' in " + lookupStrings, lookupStrings.contains(lookupString));
    }

    protected void chooseCompletion(String lookupString) {
        LookupElement[] elements = myFixture.completeBasic();
        if (elements == null) {
            return;
        }

        LookupElement element = Arrays.stream(elements)
                .filter(candidate -> lookupString.equals(candidate.getLookupString()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected lookup '" + lookupString + "' in " + Arrays.toString(elements)));

        myFixture.getLookup().setCurrentItem(element);
        myFixture.type('\n');
    }

    protected PsiReference referenceAtCaret() {
        PsiReference reference = myFixture.getFile().findReferenceAt(myFixture.getCaretOffset());
        assertNotNull(reference);
        return reference;
    }

    protected PsiElement resolveReferenceAtCaret() {
        PsiReference reference = referenceAtCaret();
        PsiElement resolved = reference.resolve();
        assertNotNull(resolved);
        return resolved;
    }
}
