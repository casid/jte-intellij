package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.platform.backend.documentation.DocumentationData;
import com.intellij.platform.backend.documentation.DocumentationResult;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider;
import com.intellij.psi.PsiElement;

public class KteSyntheticKotlinDocumentationProviderTest extends KteK2FixtureSupport {
    public void testQuickNavigateInfoForTemplateParamShowsDeclarationType() {
        addProfileClass();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${pro<caret>file.displayName}
                """);

        String info = quickNavigateInfoAtCaret();

        assertEquals("profile: Profile", info);
    }

    public void testQuickNavigateInfoForNullableTemplateParamUsesK2RenderedSignatureType() {
        addProfileClass();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile?
                ${pro<caret>file?.displayName}
                """);

        String info = quickNavigateInfoAtCaret();

        assertEquals("profile: Profile?", info);
    }

    public void testKotlinPropertyDocumentationFallsBackToPlatformProvider() {
        addProfileClass();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.display<caret>Name}
                """);

        assertNull(quickNavigateInfoAtCaret());

        String info = platformDocumentationHintAtCaret();
        assertNotNull(info);
        assertTrue(info, info.contains("displayName"));
    }

    public void testImportedKotlinClassDocumentationFallsBackToPlatformProvider() {
        addProfileClass();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Pro<caret>file
                ${profile.displayName}
                """);

        assertNull(quickNavigateInfoAtCaret());

        String info = platformDocumentationHintAtCaret();
        assertNotNull(info);
        assertTrue(info, plainDocumentationText(info).contains("data class Profile"));
    }

    public void testImportedKotlinFunctionDocumentationFallsBackToPlatformProvider() {
        addSupportHelpers();

        myFixture.configureByText("helpers.kte", """
                @import com.example.i18n
                ${i18<caret>n("title")}
                """);

        assertNull(quickNavigateInfoAtCaret());

        String info = platformDocumentationHintAtCaret();
        assertNotNull(info);
        String infoText = plainDocumentationText(info);
        assertTrue(info, infoText.contains("fun i18n"));
        assertFalse(info, infoText.contains("__args"));
    }

    public void testEnumEntryDocumentationFallsBackToPlatformProvider() {
        addSupportHelpers();

        myFixture.configureByText("form.kte", """
                @import com.example.HiddenHttpMethod
                @template.form.hidden_http_method(method = HiddenHttpMethod.P<caret>UT)
                """);

        assertNull(quickNavigateInfoAtCaret());

        String info = platformDocumentationHintAtCaret();
        assertNotNull(info);
        assertTrue(info.contains("PUT"));
    }

    public void testLocalStatementVariableDoesNotUseCustomDocumentation() {
        addCareOfferingFixture();

        myFixture.configureByText("facility.kte", """
                @import com.example.Page
                @param page: Page
                !{val careOfferingForm = requireNotNull(page.careOfferingForm)}
                ${care<caret>OfferingForm.displayName}
                """);

        assertNull(quickNavigateInfoAtCaret());
    }

    public void testLocalStatementDeclarationDoesNotUseCustomDocumentation() {
        addNestedObjectAndExtensionFixture();

        myFixture.configureByText("base.kte", """
                @import com.example.navigation.config.PathConfig
                @param isLoggedIn: Boolean
                !{val acc<caret>Link = if (isLoggedIn) PathConfig.BackOffice.ACCESSIBILITY else PathConfig.FrontOffice.ACCESSIBILITY}
                <a href="${accLink}">Accessibility</a>
                """);

        assertNull(quickNavigateInfoAtCaret());
    }

    public void testLocalStatementUsageDoesNotUseCustomDocumentation() {
        addNestedObjectAndExtensionFixture();

        myFixture.configureByText("base.kte", """
                @import com.example.navigation.config.PathConfig
                @param isLoggedIn: Boolean
                !{val accLink = if (isLoggedIn) PathConfig.BackOffice.ACCESSIBILITY else PathConfig.FrontOffice.ACCESSIBILITY}
                <a href="${acc<caret>Link}">Accessibility</a>
                """);

        assertNull(quickNavigateInfoAtCaret());
    }

    public void testForDeclarationDoesNotUseCustomDocumentation() {
        addProfileClassWithKotlinProperties();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profiles: List<Profile>
                @for(it<caret>em in profiles)
                    <li>${item.displayName}</li>
                @else
                    <li>empty</li>
                @endfor
                """);

        assertNull(quickNavigateInfoAtCaret());
    }

    public void testForVariableUsageDoesNotUseCustomDocumentation() {
        addProfileClassWithKotlinProperties();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profiles: List<Profile>
                @for(item in profiles)
                    <li>${it<caret>em.displayName}</li>
                @else
                    <li>empty</li>
                @endfor
                """);

        assertNull(quickNavigateInfoAtCaret());
    }

    public void testGeneratedDocumentationForLocalDeclarationFallsBackToPlatformProvider() {
        addNestedObjectAndExtensionFixture();

        myFixture.configureByText("base.kte", """
                @import com.example.navigation.config.PathConfig
                @param isLoggedIn: Boolean
                !{val acc<caret>Link = if (isLoggedIn) PathConfig.BackOffice.ACCESSIBILITY else PathConfig.FrontOffice.ACCESSIBILITY}
                <a href="${accLink}">Accessibility</a>
                """);

        String documentation = documentationAtCaret();

        assertNull(documentation);
    }

    public void testQuickNavigateInfoForTemplateReferenceShowsChildSignature() {
        addProfileClassWithKotlinProperties();
        addTemplateRoot();
        addSignatureTemplate();

        myFixture.configureByText("parent.kte", """
                @template.components.signatureKitchen<caret>Sink(profile = broken, tags = broken)
                """);

        String info = quickNavigateInfoAtCaret();

        assertNotNull(info);
        assertTrue(info, info.contains("template components.signatureKitchenSink("));
        assertTrue(info, info.contains("profile: Profile"));
        assertTrue(info, info.contains("title: String = \"Profile\""));
        assertTrue(info, info.contains("tags: List<String>"));
        assertTrue(info, info.contains("content: gg.jte.Content"));
        assertTrue(info, info.contains("vararg labels: String"));
    }

    public void testGeneratedDocumentationForTemplateReferenceShowsChildSignature() {
        addProfileClassWithKotlinProperties();
        addTemplateRoot();
        addSignatureTemplate();

        myFixture.configureByText("parent.kte", """
                @template.components.signatureKitchen<caret>Sink(profile = broken, tags = broken)
                """);

        String documentation = documentationAtCaret();
        String documentationText = plainDocumentationText(documentation);

        assertNotNull(documentation);
        assertTrue(documentation, documentation.contains("<span style=\""));
        assertTrue(documentation, documentationText.contains("template components.signatureKitchenSink("));
        assertTrue(documentation, documentationText.contains("profile: Profile"));
        assertTrue(documentation, documentationText.contains("vararg labels: String"));
    }

    public void testUnresolvedTemplateReferenceDoesNotShowMisleadingSignature() {
        addTemplateRoot();

        myFixture.configureByText("parent.kte", """
                @template.components.miss<caret>ing(profile = broken)
                """);

        assertNull(quickNavigateInfoAtCaret());
    }

    private String quickNavigateInfoAtCaret() {
        KteSyntheticKotlinDocumentationProvider provider = new KteSyntheticKotlinDocumentationProvider();
        PsiElement element = elementAtCaret();
        PsiElement documentationElement = documentationElementAtCaret(provider, element);
        return documentationElement == null ? null : provider.getQuickNavigateInfo(documentationElement, element);
    }

    private String platformDocumentationHintAtCaret() {
        KteSyntheticKotlinDocumentationProvider provider = new KteSyntheticKotlinDocumentationProvider();
        PsiElement element = elementAtCaret();
        PsiElement documentationElement = documentationElementAtCaret(provider, element);
        assertNotNull(documentationElement);
        for (PsiDocumentationTargetProvider targetProvider : PsiDocumentationTargetProvider.EP_NAME.getExtensionList()) {
            for (DocumentationTarget target : targetProvider.documentationTargets(documentationElement, element)) {
                String hint = target.computeDocumentationHint();
                if (hint != null) {
                    return hint;
                }

                DocumentationResult documentation = target.computeDocumentation();
                if (documentation instanceof DocumentationData data) {
                    return data.getHtml();
                }
            }
        }
        return null;
    }

    private String documentationAtCaret() {
        KteSyntheticKotlinDocumentationProvider provider = new KteSyntheticKotlinDocumentationProvider();
        PsiElement element = elementAtCaret();
        PsiElement documentationElement = documentationElementAtCaret(provider, element);
        return documentationElement == null ? null : provider.generateDoc(documentationElement, element);
    }

    private String plainDocumentationText(String documentation) {
        return StringUtil.unescapeXmlEntities(documentation
                .replace("<br>", "\n")
                .replace("&#32;", " ")
                .replaceAll("<[^>]+>", ""));
    }

    private PsiElement elementAtCaret() {
        PsiElement element = myFixture.getFile().findElementAt(Math.max(0, myFixture.getCaretOffset() - 1));
        assertNotNull(element);
        return element;
    }

    private PsiElement documentationElementAtCaret(KteSyntheticKotlinDocumentationProvider provider, PsiElement element) {
        return provider.getCustomDocumentationElement(
                myFixture.getEditor(),
                myFixture.getFile(),
                element,
                Math.max(0, myFixture.getCaretOffset() - 1)
        );
    }
}
