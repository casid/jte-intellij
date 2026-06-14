package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightIdeaTestCase;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtFile;
import org.jusecase.jte.intellij.language.k2.KteSyntheticKotlinRangeMapping.Kind;
import org.jusecase.jte.intellij.language.psi.JtePsiJavaInjection;
import org.jusecase.jte.intellij.language.psi.JtePsiOutput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KteSyntheticKotlinFileBuilderTest extends LightIdeaTestCase {
    public void testBuildsFullKotlinFileForImportsParamsAndBody() {
        PsiFile file = createFile("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                @if(profile.active)
                ${profile.name}
                @endif
                """);

        KteSyntheticKotlinFile syntheticFile = build(file);

        assertTrue(syntheticFile.getText(), syntheticFile.getText().contains("import com.example.Profile"));
        assertTrue(syntheticFile.getText(), syntheticFile.getText().contains("fun render(profile: Profile)"));
        assertTrue(syntheticFile.getText(), syntheticFile.getText().contains("if (profile.active)"));
        assertTrue(syntheticFile.getText(), syntheticFile.getText().contains("writeUserContent(profile.name)"));

        assertMappedText(file, syntheticFile, Kind.IMPORT, "com.example.Profile");
        assertMappedText(file, syntheticFile, Kind.PARAMETER, "profile: Profile");
        assertMappedText(file, syntheticFile, Kind.IF_CONDITION, "profile.active");
        KteSyntheticKotlinRangeMapping output = assertMappedText(file, syntheticFile, Kind.OUTPUT_EXPRESSION, "profile.name");

        int kotlinOffset = output.getKotlinRange().getStartOffset() + "profile".length();
        assertEquals(
                Integer.valueOf(output.getTemplateRange().getStartOffset() + "profile".length()),
                syntheticFile.mapKotlinOffsetToTemplate(kotlinOffset)
        );
        assertEquals(
                Integer.valueOf(kotlinOffset),
                syntheticFile.mapTemplateOffsetToKotlin(output.getTemplateRange().getStartOffset() + "profile".length())
        );
    }

    public void testSyntheticTextParsesAsRegularKotlinFile() {
        PsiFile file = createFile("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.name}
                """);

        KteSyntheticKotlinFile syntheticFile = build(file);
        KtFile ktFile = parseSyntheticFile(syntheticFile);

        assertEquals(1, ktFile.getImportDirectives().size());
        assertNotNull(PsiTreeUtil.findChildOfType(ktFile, KtClass.class));
        assertNoSyntheticSyntaxErrors(syntheticFile);
    }

    public void testSourceEditMappingRejectsGeneratedWrapperRanges() {
        PsiFile file = createFile("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.name}
                """);

        KteSyntheticKotlinFile syntheticFile = build(file);

        KteSyntheticKotlinRangeMapping output = assertMappedText(file, syntheticFile, Kind.OUTPUT_EXPRESSION, "profile.name");
        assertEquals(output.getTemplateRange(), syntheticFile.mapKotlinSourceEditRangeToTemplate(output.getKotlinRange()));
        assertTrue(syntheticFile.isKotlinRangeFullySourceEditable(output.getKotlinRange()));

        TextRange insertionAtOutputEnd = TextRange.from(output.getKotlinRange().getEndOffset(), 0);
        assertEquals(
                TextRange.from(output.getTemplateRange().getEndOffset(), 0),
                syntheticFile.mapKotlinSourceEditRangeToTemplate(insertionAtOutputEnd)
        );

        KteSyntheticKotlinRangeMapping importMapping = assertMappedText(file, syntheticFile, Kind.IMPORT, "com.example.Profile");
        assertNull(syntheticFile.mapKotlinSourceEditRangeToTemplate(importMapping.getKotlinRange()));
        assertFalse(syntheticFile.touchesGeneratedKotlin(importMapping.getKotlinRange()));

        int wrapperOffset = syntheticFile.getText().indexOf("DummyTemplate");
        assertTrue(syntheticFile.getText(), wrapperOffset >= 0);
        TextRange wrapperRange = TextRange.from(wrapperOffset, "DummyTemplate".length());
        assertNull(syntheticFile.mapKotlinSourceEditRangeToTemplate(wrapperRange));
        assertTrue(syntheticFile.touchesGeneratedKotlin(wrapperRange));
    }

    public void testTrimsKotlinCompletionRecoveryTailFromOutputExpression() {
        PsiFile file = createFile("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.IntellijIdeaRulezzz$,}
                """);

        KteSyntheticKotlinFile syntheticFile = build(file);
        String injections = PsiTreeUtil.findChildrenOfType(file, JtePsiJavaInjection.class).stream()
                .map(JtePsiJavaInjection::getText)
                .reduce("", (left, right) -> left + "|" + right);
        JtePsiOutput output = PsiTreeUtil.findChildOfType(file, JtePsiOutput.class);
        String outputChildren = output == null ? "no output" : Arrays.stream(output.getChildren())
                .map(KteSyntheticKotlinFileBuilderTest::describeChild)
                .collect(Collectors.joining(" | "));

        assertTrue(injections + "\n" + outputChildren + "\n" + syntheticFile.getText(),
                syntheticFile.getText().contains("profile.IntellijIdeaRulezzz)"));
        assertFalse(injections + "\n" + outputChildren + "\n" + syntheticFile.getText(),
                syntheticFile.getText().contains("IntellijIdeaRulezzz$"));
    }

    public void testCompletionBuildSkipsFutureStatementDeclarations() {
        PsiFile file = createFile("facility.kte", """
                @param page: Page
                ${careIntellijIdeaRulezzz}
                !{val careOfferingForm = requireNotNull(page.careOfferingForm)}
                """);
        int completionOffset = file.getText().indexOf("IntellijIdeaRulezzz") + "IntellijIdeaRulezzz".length();

        KteSyntheticKotlinFile syntheticFile = new KteSyntheticKotlinFileBuilder().buildForCompletion(file, completionOffset);

        assertTrue(syntheticFile.getText(), syntheticFile.getText().contains("careIntellijIdeaRulezzz"));
        assertFalse(syntheticFile.getText(), syntheticFile.getText().contains("careOfferingForm"));
        assertNoSyntheticSyntaxErrors(syntheticFile);
    }

    public void testMapsParameterDefaultsWithoutOverlappingDeclarationMapping() {
        PsiFile file = createFile("params.kte", """
                @import com.example.Profile
                @import com.example.*
                @import com.example.Account as AccountAlias
                @param profile: Profile
                @param title: String = "Profile"
                @param count: Int = 1
                @param optional: Profile? = null
                @param profiles: List<Profile>
                @param vararg labels: String
                ${title}
                """);

        KteSyntheticKotlinFile syntheticFile = build(file);

        assertMappedText(file, syntheticFile, Kind.IMPORT, "com.example.Profile");
        assertMappedText(file, syntheticFile, Kind.IMPORT, "com.example.*");
        assertMappedText(file, syntheticFile, Kind.IMPORT, "com.example.Account as AccountAlias");
        assertMappedText(file, syntheticFile, Kind.PARAMETER, "profile: Profile");
        assertMappedText(file, syntheticFile, Kind.PARAMETER, "title: String");
        assertMappedText(file, syntheticFile, Kind.PARAMETER_DEFAULT_VALUE, "\"Profile\"");
        assertMappedText(file, syntheticFile, Kind.PARAMETER_DEFAULT_VALUE, "1");
        assertMappedText(file, syntheticFile, Kind.PARAMETER_DEFAULT_VALUE, "null");
        assertMappedText(file, syntheticFile, Kind.PARAMETER, "profiles: List<Profile>");
        assertMappedText(file, syntheticFile, Kind.PARAMETER, "vararg labels: String");
        assertNoOverlappingMappings(syntheticFile);
        assertEveryMappingRoundTrips(file, syntheticFile);
        assertNoSyntheticSyntaxErrors(syntheticFile);
    }

    public void testBuildsUnsafeOutputStatementsAndConditionals() {
        PsiFile file = createFile("features.kte", """
                @param profile: Profile
                @param profiles: List<Profile>
                !{ val localName = profile.name }
                @if(profile.active || profile.manager != null)
                    $unsafe{profile.html}
                @elseif(profiles.isEmpty())
                    ${localName}
                @else
                    ${profile.name}
                @endif
                """);

        KteSyntheticKotlinFile syntheticFile = build(file);

        assertMappedText(file, syntheticFile, Kind.STATEMENT, "val localName = profile.name");
        assertMappedText(file, syntheticFile, Kind.IF_CONDITION, "profile.active || profile.manager != null");
        assertMappedText(file, syntheticFile, Kind.ELSE_IF_CONDITION, "profiles.isEmpty()");
        assertMappedText(file, syntheticFile, Kind.UNSAFE_OUTPUT_EXPRESSION, "profile.html");
        assertMappedText(file, syntheticFile, Kind.OUTPUT_EXPRESSION, "localName");
        assertNoOverlappingMappings(syntheticFile);
        assertEveryMappingRoundTrips(file, syntheticFile);
        assertNoSyntheticSyntaxErrors(syntheticFile);
    }

    public void testBuildsForElseAsValidKotlinWithoutMappingGeneratedFlag() {
        PsiFile file = createFile("loop.kte", """
                @param profiles: List<Profile>
                @for(profile in profiles)
                    ${profile.name}
                @else
                    ${profiles.size}
                @endfor
                """);

        KteSyntheticKotlinFile syntheticFile = build(file);

        assertTrue(syntheticFile.getText().contains("var __jteForElse0 = true"));
        assertTrue(syntheticFile.getText().contains("__jteForElse0 = false"));
        assertTrue(syntheticFile.getText().contains("if (__jteForElse0)"));
        assertMappedText(file, syntheticFile, Kind.FOR_CONDITION, "profile in profiles");
        assertMappedText(file, syntheticFile, Kind.OUTPUT_EXPRESSION, "profile.name");
        assertMappedText(file, syntheticFile, Kind.OUTPUT_EXPRESSION, "profiles.size");
        assertNoMappingForText(syntheticFile, "__jteForElse0");
        assertNoOverlappingMappings(syntheticFile);
        assertEveryMappingRoundTrips(file, syntheticFile);
        assertNoSyntheticSyntaxErrors(syntheticFile);
    }

    public void testBuildsTemplateArgumentsAndNestedContentBlocks() {
        PsiFile file = createFile("card.kte", """
                @param profile: Profile
                @param title: String
                @template.card(profile = profile, title = title, content = @`
                    ${profile.name}
                    @template.badge(profile, @`
                        $unsafe{profile.badgeHtml}
                    `)
                `)
                """);

        KteSyntheticKotlinFile syntheticFile = build(file);

        assertTrue(syntheticFile.getText(), syntheticFile.getText().contains("dummyCall(profile,title,object : gg.jte.Content"));
        assertTrue(syntheticFile.getText(), syntheticFile.getText().contains("dummyCall(profile, object : gg.jte.Content"));
        assertFalse(syntheticFile.getText().contains("profile ="));
        assertFalse(syntheticFile.getText().contains("title ="));
        assertMappedText(file, syntheticFile, Kind.TEMPLATE_ARGUMENT_VALUE, "profile");
        assertMappedText(file, syntheticFile, Kind.TEMPLATE_ARGUMENT_VALUE, "title");
        assertMappedText(file, syntheticFile, Kind.OUTPUT_EXPRESSION, "profile.name");
        assertMappedText(file, syntheticFile, Kind.UNSAFE_OUTPUT_EXPRESSION, "profile.badgeHtml");
        assertNoOverlappingMappings(syntheticFile);
        assertEveryMappingRoundTrips(file, syntheticFile);
        assertNoSyntheticSyntaxErrors(syntheticFile);
    }

    public void testBuildsHtmlAttributeLocalVariablesAndObjectMemberExtensionImport() {
        PsiFile file = createFile("base.kte", """
                @import com.example.navigation.routing.RoutingUtils.isCurrentPage
                @param breadcrumbs: List<Breadcrumb>?
                @param isLoggedIn: Boolean

                !{val isCurrentHome = breadcrumbs.isCurrentPage(PathConfig.FrontOffice.HOME)}
                <head>
                    <link rel="stylesheet" href="/css/styles.css">
                </head>
                <header>
                    !{val logoTitle = if (isLoggedIn) "home" else "start"}
                    <a title="${logoTitle}"></a>
                </header>
                <footer>
                    <li>
                        !{val link = if (isLoggedIn) PathConfig.BackOffice.GDPR else PathConfig.FrontOffice.GDPR}
                        <a href="${link}">GDPR</a>
                    </li>
                </footer>
                """);

        KteSyntheticKotlinFile syntheticFile = build(file);

        assertMappedText(file, syntheticFile, Kind.IMPORT, "com.example.navigation.routing.RoutingUtils.isCurrentPage");
        assertMappedText(file, syntheticFile, Kind.STATEMENT,
                "val isCurrentHome = breadcrumbs.isCurrentPage(PathConfig.FrontOffice.HOME)");
        assertMappedText(file, syntheticFile, Kind.STATEMENT,
                "val logoTitle = if (isLoggedIn) \"home\" else \"start\"");
        assertMappedText(file, syntheticFile, Kind.OUTPUT_EXPRESSION, "logoTitle");
        assertMappedText(file, syntheticFile, Kind.STATEMENT,
                "val link = if (isLoggedIn) PathConfig.BackOffice.GDPR else PathConfig.FrontOffice.GDPR");
        assertMappedText(file, syntheticFile, Kind.OUTPUT_EXPRESSION, "link");
        assertNoOverlappingMappings(syntheticFile);
        assertEveryMappingRoundTrips(file, syntheticFile);
        assertNoSyntheticSyntaxErrors(syntheticFile);
    }

    public void testIgnoresRawBlocksCommentsAndPlainText() {
        PsiFile file = createFile("raw.kte", """
                @param profile: Profile
                <%-- ${broken.comment} --%>
                @raw
                    @template.broken(profile = missing)
                    ${thisShouldStayRaw}
                @endraw
                <p>${profile.name}</p>
                """);

        KteSyntheticKotlinFile syntheticFile = build(file);

        assertFalse(syntheticFile.getText().contains("broken.comment"));
        assertFalse(syntheticFile.getText().contains("thisShouldStayRaw"));
        assertFalse(syntheticFile.getText().contains("missing"));
        assertMappedText(file, syntheticFile, Kind.OUTPUT_EXPRESSION, "profile.name");
        assertNoOverlappingMappings(syntheticFile);
        assertEveryMappingRoundTrips(file, syntheticFile);
        assertNoSyntheticSyntaxErrors(syntheticFile);
    }

    public void testHandlesEmptyTextOnlyImportOnlyAndIncompleteTemplatesWithoutCrashing() {
        assertEquals("", build(createFile("empty.kte", "")).getText());
        assertEquals("", build(createFile("text.kte", "<p>Hello</p>")).getText());

        KteSyntheticKotlinFile importOnly = build(createFile("imports.kte", "@import com.example.Profile\n"));
        assertEquals("import com.example.Profile\n", importOnly.getText());
        assertNoSyntheticSyntaxErrors(importOnly);

        build(createFile("incomplete-output.kte", "@param profile: Profile\n${"));
        build(createFile("incomplete-if.kte", "@param profile: Profile\n@if("));
        build(createFile("incomplete-template.kte", "@param profile: Profile\n@template.card(profile = "));
    }

    private KteSyntheticKotlinFile build(PsiFile file) {
        return new KteSyntheticKotlinFileBuilder().build(file);
    }

    private KtFile parseSyntheticFile(KteSyntheticKotlinFile syntheticFile) {
        return KteSyntheticKotlinPsiFactory.createKtFile(getProject(), syntheticFile);
    }

    private void assertNoSyntheticSyntaxErrors(KteSyntheticKotlinFile syntheticFile) {
        KtFile ktFile = parseSyntheticFile(syntheticFile);
        List<PsiErrorElement> errors = new ArrayList<>(PsiTreeUtil.findChildrenOfType(ktFile, PsiErrorElement.class));
        assertTrue("Expected no syntax errors in:\n" + syntheticFile.getText() + "\nErrors: " + errors, errors.isEmpty());
    }

    private KteSyntheticKotlinRangeMapping assertMappedText(PsiFile file, KteSyntheticKotlinFile syntheticFile, Kind kind, String expectedText) {
        for (KteSyntheticKotlinRangeMapping mapping : syntheticFile.getMappings()) {
            if (mapping.getKind() == kind &&
                    expectedText.equals(substring(file.getText(), mapping.getTemplateRange())) &&
                    expectedText.equals(substring(syntheticFile.getText(), mapping.getKotlinRange()))) {
                return mapping;
            }
        }

        fail("Expected " + kind + " mapping for text: " + expectedText + "\n" + syntheticFile.getText());
        return null;
    }

    private void assertNoMappingForText(KteSyntheticKotlinFile syntheticFile, String generatedText) {
        int index = syntheticFile.getText().indexOf(generatedText);
        assertTrue("Expected generated text in synthetic file: " + generatedText, index >= 0);
        for (KteSyntheticKotlinRangeMapping mapping : syntheticFile.getMappings()) {
            assertFalse("Generated text should be unmapped: " + generatedText,
                    mapping.getKotlinRange().intersects(new TextRange(index, index + generatedText.length())));
        }
    }

    private void assertEveryMappingRoundTrips(PsiFile file, KteSyntheticKotlinFile syntheticFile) {
        for (KteSyntheticKotlinRangeMapping mapping : syntheticFile.getMappings()) {
            TextRange remappedTemplate = syntheticFile.mapKotlinRangeToTemplate(mapping.getKotlinRange());
            assertEquals(mapping.getTemplateRange(), remappedTemplate);

            TextRange remappedKotlin = syntheticFile.mapTemplateRangeToKotlin(mapping.getTemplateRange());
            assertEquals(mapping.getKotlinRange(), remappedKotlin);

            assertEquals(
                    substring(file.getText(), mapping.getTemplateRange()),
                    substring(syntheticFile.getText(), mapping.getKotlinRange())
            );
        }
    }

    private void assertNoOverlappingMappings(KteSyntheticKotlinFile syntheticFile) {
        List<KteSyntheticKotlinRangeMapping> mappings = new ArrayList<>(syntheticFile.getMappings());
        mappings.sort(Comparator.comparingInt(mapping -> mapping.getKotlinRange().getStartOffset()));
        for (int index = 1; index < mappings.size(); index++) {
            TextRange previous = mappings.get(index - 1).getKotlinRange();
            TextRange current = mappings.get(index).getKotlinRange();
            assertTrue("Mappings overlap in synthetic Kotlin: " + previous + " and " + current + "\n" + syntheticFile.getText(),
                    previous.getEndOffset() <= current.getStartOffset());
        }
    }

    private static String describeChild(PsiElement element) {
        return element.getClass().getSimpleName() + ":'" + element.getText().replace("\n", "\\n") + "'";
    }

    private String substring(String text, TextRange range) {
        return text.substring(range.getStartOffset(), range.getEndOffset());
    }
}
