package org.jusecase.jte.intellij.language.k2;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import java.util.Set;

public class KteSyntheticKotlinReferenceResolveTest extends KteK2FixtureSupport {
    public void testParamTypeReferenceResolvesToImportedKotlinClass() {
        myFixture.addFileToProject("src/com/example/Profile.kt", """
                package com.example

                data class Profile(val displayName: String)
                """);

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Pro<caret>file
                ${profile.displayName}
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof KtClass);
        assertEquals("Profile", ((KtClass) resolved).getName());
    }

    public void testGenericParamTypeReferenceResolvesToImportedKotlinClass() {
        myFixture.addFileToProject("src/com/example/Profile.kt", """
                package com.example

                data class Profile(val displayName: String)
                """);

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profiles: List<Pro<caret>file>
                ${profiles}
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof KtClass);
        assertEquals("Profile", ((KtClass) resolved).getName());
    }

    public void testImportReferenceResolvesToKotlinClass() {
        myFixture.addFileToProject("src/com/example/Profile.kt", """
                package com.example

                data class Profile(val displayName: String)
                """);

        myFixture.configureByText("profile.kte", """
                @import com.example.Pro<caret>file
                @param profile: Profile
                ${profile.displayName}
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof KtClass);
        assertEquals("Profile", ((KtClass) resolved).getName());
    }

    public void testImportReferenceResolvesToTopLevelKotlinFunction() {
        addSupportHelpers();

        myFixture.configureByText("helpers.kte", """
                @import com.example.i<caret>18n
                ${i18n("common.ok")}
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof KtNamedFunction);
        assertEquals("i18n", ((KtNamedFunction) resolved).getName());
    }

    public void testImportReferenceResolvesToKotlinObjectMemberExtensionFunction() {
        addNestedObjectAndExtensionFixture();

        myFixture.configureByText("base.kte", """
                @import com.example.navigation.routing.RoutingUtils.isCurrent<caret>Page
                @import com.example.navigation.breadcrumb.Breadcrumb
                @param breadcrumbs: List<Breadcrumb>?
                ${breadcrumbs}
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof KtNamedFunction);
        assertEquals("isCurrentPage", ((KtNamedFunction) resolved).getName());
    }

    public void testObjectMemberExtensionFunctionCallResolvesFromImportedExtension() {
        addNestedObjectAndExtensionFixture();

        myFixture.configureByText("base.kte", """
                @import com.example.navigation.config.PathConfig
                @import com.example.navigation.breadcrumb.Breadcrumb
                @import com.example.navigation.routing.RoutingUtils.isCurrentPage
                @param breadcrumbs: List<Breadcrumb>?

                @if(breadcrumbs.isCurrent<caret>Page(PathConfig.FrontOffice.HOME))
                    ${breadcrumbs}
                @endif
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof KtNamedFunction);
        assertEquals("isCurrentPage", ((KtNamedFunction) resolved).getName());
    }

    public void testHtmlAttributeLocalVariableReferenceResolvesToStatementDeclaration() {
        addNestedObjectAndExtensionFixture();

        myFixture.configureByText("base.kte", """
                @import com.example.navigation.config.PathConfig
                @param isLoggedIn: Boolean

                <footer>
                    <li>
                        !{val link = if (isLoggedIn) PathConfig.BackOffice.GDPR else PathConfig.FrontOffice.GDPR}
                        <a href="${li<caret>nk}">GDPR</a>
                    </li>
                </footer>
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertSame(myFixture.getFile(), resolved.getContainingFile());
        assertTrue(resolved.getText().contains("val link"));
    }

    public void testExplicitImportDoesNotResolveUnrelatedSameShortNameClass() {
        myFixture.addFileToProject("src/com/tools/Profile.kt", """
                package com.tools

                enum class Profile
                """);

        myFixture.configureByText("profile.kte", """
                @import com.example.Pro<caret>file
                @param profile: Profile
                ${profile}
                """);

        assertNull(referenceAtCaret().resolve());
    }

    public void testOutputPropertyReferenceResolvesToImportedKotlinProperty() {
        myFixture.addFileToProject("src/com/example/Profile.kt", """
                package com.example

                data class Profile(val displayName: String)
                """);

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.display<caret>Name}
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof PsiNamedElement);
        assertEquals("displayName", ((PsiNamedElement) resolved).getName());
    }

    public void testReceiverReferenceMapsBackToTemplateParam() {
        myFixture.addFileToProject("src/com/example/Profile.kt", """
                package com.example

                data class Profile(val displayName: String)
                """);

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${pro<caret>file.displayName}
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertSame(myFixture.getFile(), resolved.getContainingFile());
        assertTrue(Set.of("profile: Profile", "profile").contains(resolved.getText()));
    }

    public void testOutputPropertyReferenceResolvesThroughSimpleForLoopVariable() {
        myFixture.addFileToProject("src/com/example/Profile.kt", """
                package com.example

                data class Profile(val displayName: String)
                """);

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profiles: List<Profile>
                @for(profile in profiles)
                    ${profile.display<caret>Name}
                @endfor
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof PsiNamedElement);
        assertEquals("displayName", ((PsiNamedElement) resolved).getName());
    }

    public void testTemplatePositionalArgumentReceiverReferenceMapsBackToTemplateParam() {
        addProfileClassWithKotlinProperties();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @template.components.card(pro<caret>file, profile.tags)
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertSame(myFixture.getFile(), resolved.getContainingFile());
        assertTrue(Set.of("profile: Profile", "profile").contains(resolved.getText()));
    }

    public void testTemplatePositionalArgumentPropertyReferenceResolvesToImportedKotlinProperty() {
        addProfileClassWithKotlinProperties();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @template.components.card(profile, profile.ta<caret>gs)
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof PsiNamedElement);
        assertEquals("tags", ((PsiNamedElement) resolved).getName());
    }

    public void testTemplateNamedArgumentPropertyReferenceResolvesToImportedKotlinProperty() {
        addProfileClassWithKotlinProperties();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @template.components.card(profile = profile, tags = profile.ta<caret>gs)
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof PsiNamedElement);
        assertEquals("tags", ((PsiNamedElement) resolved).getName());
    }

    public void testTemplateArgumentEnumEntryReferenceResolvesToEnumConstant() {
        addSupportHelpers();

        myFixture.configureByText("form.kte", """
                @import com.example.HiddenHttpMethod
                @template.form.hidden_http_method(method = HiddenHttpMethod.P<caret>UT)
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof PsiNamedElement);
        assertEquals("PUT", ((PsiNamedElement) resolved).getName());
    }

    public void testTemplateArgumentEnumClassReferenceResolvesToImportedEnumClass() {
        addSupportHelpers();

        myFixture.configureByText("form.kte", """
                @import com.example.HiddenHttpMethod
                @template.form.hidden_http_method(method = Hidden<caret>HttpMethod.PUT)
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof KtClass);
        assertEquals("HiddenHttpMethod", ((KtClass) resolved).getName());
    }

    public void testCompanionPropertyReferenceResolvesThroughSyntheticKotlin() {
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
                ${Profile.DE<caret>FAULT.displayName}
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof PsiNamedElement);
        assertEquals("DEFAULT", ((PsiNamedElement) resolved).getName());
    }

    public void testIfConditionPropertyReferencesResolveToImportedKotlinProperties() {
        addProfileClassWithKotlinProperties();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @if(profile.act<caret>ive || profile.manager != null)
                    ${profile.displayName}
                @endif
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof PsiNamedElement);
        assertEquals("active", ((PsiNamedElement) resolved).getName());
    }

    public void testIfConditionNullablePropertyReferenceResolvesToImportedKotlinProperty() {
        addProfileClassWithKotlinProperties();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @if(profile.active || profile.man<caret>ager != null)
                    ${profile.displayName}
                @endif
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof PsiNamedElement);
        assertEquals("manager", ((PsiNamedElement) resolved).getName());
    }

    public void testStatementPropertyReferenceResolvesToImportedKotlinProperty() {
        addProfileClassWithKotlinProperties();

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                !{ val ignored = profile.display<caret>Name }
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof PsiNamedElement);
        assertEquals("displayName", ((PsiNamedElement) resolved).getName());
    }

    public void testLocalStatementVariableReferenceResolvesToDeclaration() {
        addCareOfferingFixture();

        myFixture.configureByText("facility.kte", """
                @import com.example.Page
                @param page: Page
                @if(page.careOfferingForm != null)
                    !{val careOfferingForm = requireNotNull(page.careOfferingForm)}
                    @template.components.care_offering_form_section(form = care<caret>OfferingForm)
                @endif
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertSame(myFixture.getFile(), resolved.getContainingFile());
        assertTrue(resolved.getText().contains("val careOfferingForm"));
    }

    public void testLocalStatementVariablePropertyReferenceResolvesToImportedKotlinProperty() {
        addCareOfferingFixture();

        myFixture.configureByText("facility.kte", """
                @import com.example.Page
                @param page: Page
                !{val careOfferingForm = requireNotNull(page.careOfferingForm)}
                ${careOfferingForm.display<caret>Name}
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof PsiNamedElement);
        assertEquals("displayName", ((PsiNamedElement) resolved).getName());
    }

    public void testLocalStatementVariableDoesNotResolveBeforeDeclaration() {
        addCareOfferingFixture();

        myFixture.configureByText("facility.kte", """
                @import com.example.Page
                @param page: Page
                ${care<caret>OfferingForm}
                !{val careOfferingForm = requireNotNull(page.careOfferingForm)}
                """);

        assertNull(referenceAtCaret().resolve());
    }

    public void testLocalStatementVariableDoesNotResolveAcrossIfElseBoundary() {
        addCareOfferingFixture();

        myFixture.configureByText("facility.kte", """
                @import com.example.Page
                @param page: Page
                @if(page.careOfferingForm != null)
                    !{val careOfferingForm = requireNotNull(page.careOfferingForm)}
                    ${careOfferingForm.displayName}
                @else
                    ${care<caret>OfferingForm}
                @endif
                """);

        assertNull(referenceAtCaret().resolve());
    }

    public void testLocalStatementVariableDoesNotResolveAfterIfBlock() {
        addCareOfferingFixture();

        myFixture.configureByText("facility.kte", """
                @import com.example.Page
                @param page: Page
                @if(page.careOfferingForm != null)
                    !{val careOfferingForm = requireNotNull(page.careOfferingForm)}
                    ${careOfferingForm.displayName}
                @endif
                ${care<caret>OfferingForm}
                """);

        assertNull(referenceAtCaret().resolve());
    }

    public void testLocalStatementVariableDoesNotResolveOutOfContentBlock() {
        addTemplateRoot();
        addLayoutTemplate();

        myFixture.configureByText("content.kte", """
                @template.layout(title = "Title", content = @`
                    !{val contentLocal = "inside"}
                    ${contentLocal}
                `)
                ${content<caret>Local}
                """);

        assertNull(referenceAtCaret().resolve());
    }

    public void testLoopVariableDoesNotResolveAfterForBlock() {
        addCareOfferingFixture();

        myFixture.configureByText("facility.kte", """
                @import com.example.CareOfferingForm
                @param forms: List<CareOfferingForm>
                @for(careOfferingForm in forms)
                    ${careOfferingForm.displayName}
                @endfor
                ${care<caret>OfferingForm}
                """);

        assertNull(referenceAtCaret().resolve());
    }
}
