package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.util.TextRange;

public class KteSyntheticKotlinSemanticServiceTest extends KteK2FixtureSupport {
    public void testExpressionTypeQueriesUseSyntheticKotlinFlow() {
        addProfileClassWithKotlinProperties();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile

                ${profile.manager?.displayName ?: "fallback"}
                ${requireNotNull(profile.manager)}
                ${profile.manager!!.displayName}
                ${profile.manager as Profile}
                @if(profile.manager != null)
                    ${profile.manager.displayName}
                @endif
                """);

        assertEquals("String", typeOfFirst("profile.manager?.displayName ?: \"fallback\""));
        assertEquals("Profile", typeOfFirst("requireNotNull(profile.manager)"));
        assertEquals("String", typeOfFirst("profile.manager!!.displayName"));
        assertEquals("Profile", typeOfFirst("profile.manager as Profile"));
        assertEquals("Profile", typeOfLast("profile.manager"));
    }

    public void testDeclarationTypeQueriesUseSyntheticKotlinLocalsAndLoopVariables() {
        addCareOfferingFixture();

        myFixture.configureByText("facility.kte", """
                @import com.example.Page
                @param page: Page

                !{val careOfferingForm = requireNotNull(page.careOfferingForm)}
                @for(form in page.forms)
                    ${form.displayName}
                @endfor
                """);

        assertEquals("CareOfferingForm", declarationTypeAt("careOfferingForm"));
        assertEquals("CareOfferingForm", declarationTypeAt("form in page.forms"));
    }

    private String typeOfFirst(String text) {
        return typeOf(sourceRange(text, myFixture.getFile().getText().indexOf(text)));
    }

    private String typeOfLast(String text) {
        return typeOf(sourceRange(text, myFixture.getFile().getText().lastIndexOf(text)));
    }

    private String typeOf(TextRange range) {
        KteSyntheticKotlinSemanticService.SemanticType type =
                KteSyntheticKotlinSemanticService.getInstance(getProject())
                        .expressionTypeAtTemplateRange(myFixture.getFile(), range);
        assertNotNull(type);
        return type.typeText();
    }

    private String declarationTypeAt(String text) {
        int offset = myFixture.getFile().getText().indexOf(text);
        assertTrue("Expected source text: " + text, offset >= 0);

        KteSyntheticKotlinSemanticService.SemanticType type =
                KteSyntheticKotlinSemanticService.getInstance(getProject())
                        .declarationTypeAtTemplateOffset(myFixture.getFile(), offset);
        assertNotNull(type);
        return type.typeText();
    }

    private TextRange sourceRange(String text, int offset) {
        assertTrue("Expected source text: " + text, offset >= 0);
        return TextRange.from(offset, text.length());
    }

}
