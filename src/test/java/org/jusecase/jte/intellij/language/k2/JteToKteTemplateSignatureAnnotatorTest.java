package org.jusecase.jte.intellij.language.k2;

public class JteToKteTemplateSignatureAnnotatorTest extends KteK2FixtureSupport {
    public void testJteCallerReportsMissingRequiredKteTemplateParams() {
        addTemplateRoot();
        addSignatureTemplate();

        myFixture.configureByText("caller.jte", """
                @template.components.signatureKitchenSink(title = "Profile")
                """);

        assertContainsDescription("Missing required parameters: profile, tags, content");
    }

    public void testJteCallerReportsUnknownKteTemplateParam() {
        addTemplateRoot();
        addSignatureTemplate();

        myFixture.configureByText("caller.jte", """
                @template.components.signatureKitchenSink(profile = null, tags = null, content = null, unknown = null)
                """);

        assertContainsDescription("Unknown parameter unknown");
    }

}
