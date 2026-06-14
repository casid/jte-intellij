package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.util.TextRange;

public class KteTemplateSignatureServiceTest extends KteK2FixtureSupport {
    public void testResolvesRequiredDefaultNullableGenericAndContentParams() {
        addProfileClass();

        myFixture.configureByText("card.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param title: String = "Profile"
                @param subtitle: String? = null
                @param tags: List<String>
                @param grouped: Map<String, List<Profile?>>
                @param content: gg.jte.Content
                @param vararg labels: String
                """);

        KteTemplateSignatureService.TemplateSignature signature =
                KteTemplateSignatureService.resolve(myFixture.getFile());

        assertSame(myFixture.getFile(), signature.templateFile());
        assertEquals(7, signature.parameters().size());
        assertTrue(signature.parameter("profile").required());
        assertFalse(signature.parameter("profile").defaulted());
        assertNotNull(signature.parameter("profile").typeClass());
        assertEquals("Profile", signature.parameter("profile").rawType());
        assertFalse(signature.parameter("title").required());
        assertTrue(signature.parameter("title").defaulted());
        assertFalse(signature.parameter("subtitle").required());
        assertTrue(signature.parameter("subtitle").nullable());
        assertEquals("List<String>", signature.parameter("tags").typeText());
        assertEquals("List", signature.parameter("tags").rawType());
        assertEquals("String", signature.parameter("tags").genericArguments().get(0));
        assertEquals("String", signature.parameter("grouped").genericArguments().get(0));
        assertEquals("List<Profile?>", signature.parameter("grouped").genericArguments().get(1));
        assertTrue(signature.parameter("content").content());
        assertTrue(signature.parameter("labels").vararg());
        assertFalse(signature.parameter("labels").required());
        assertFalse(signature.requiredParameters().contains(signature.parameter("labels")));
    }

    public void testRecognizesImportedSimpleContentType() {
        myFixture.configureByText("content.kte", """
                @import gg.jte.Content
                @param content: Content
                """);

        KteTemplateSignatureService.TemplateSignature signature =
                KteTemplateSignatureService.resolve(myFixture.getFile());

        assertTrue(signature.parameter("content").content());
    }

    public void testAddsK2RenderedTypeMetadataForValidParams() {
        addProfileClass();

        myFixture.configureByText("card.kte", """
                @import com.example.Profile
                @param profile: Profile?
                @param profiles: List<Profile>
                """);

        KteTemplateSignatureService.TemplateSignature signature =
                KteTemplateSignatureService.resolve(myFixture.getFile());

        KteTemplateSignatureService.Parameter profile = signature.parameter("profile");
        KteTemplateSignatureService.Parameter profiles = signature.parameter("profiles");

        assertEquals("Profile?", profile.semanticTypeText());
        assertTrue(profile.semanticQualifiedTypeText(), profile.semanticQualifiedTypeText().contains("com.example.Profile"));
        assertNotNull(profile.typeElement());
        assertEquals("List<Profile>", profiles.semanticTypeText());
        assertTrue(profiles.semanticQualifiedTypeText(), profiles.semanticQualifiedTypeText().contains("com.example.Profile"));
    }

    public void testKeepsSourceTypeTextWhenParamTypeCannotBeAnalyzed() {
        myFixture.configureByText("broken.kte", """
                @param profile: MissingProfile
                """);

        KteTemplateSignatureService.TemplateSignature signature =
                KteTemplateSignatureService.resolve(myFixture.getFile());

        KteTemplateSignatureService.Parameter profile = signature.parameter("profile");

        assertEquals("MissingProfile", profile.typeText());
        assertEquals("MissingProfile", profile.renderedTypeText());
        assertNull(profile.semanticTypeText());
        assertNull(profile.semanticQualifiedTypeText());
    }

    public void testExposesParamSourceRanges() {
        addProfileClass();

        myFixture.configureByText("card.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param title: String = "Profile"
                """);

        KteTemplateSignatureService.TemplateSignature signature =
                KteTemplateSignatureService.resolve(myFixture.getFile());
        KteTemplateSignatureService.Parameter profile = signature.parameter("profile");
        KteTemplateSignatureService.Parameter title = signature.parameter("title");

        assertEquals("profile: Profile", text(profile.declarationRange()));
        assertEquals("profile", text(profile.nameRange()));
        assertEquals("Profile", text(profile.typeRange()));
        assertEquals("\"Profile\"", text(title.defaultValueRange()));
    }

    public void testReturnsEmptySignatureForNonKteTemplate() {
        myFixture.configureByText("card.jte", """
                @param String title
                """);

        KteTemplateSignatureService.TemplateSignature signature =
                KteTemplateSignatureService.resolve(myFixture.getFile());

        assertTrue(signature.parameters().isEmpty());
        assertFalse(KteTemplateSignatureService.isKteTemplate(myFixture.getFile()));
    }

    private String text(TextRange range) {
        assertNotNull(range);
        return myFixture.getFile().getText().substring(range.getStartOffset(), range.getEndOffset());
    }
}
