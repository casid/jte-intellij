package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

public class JteReferenceImporterTest extends LightJavaCodeInsightFixtureTestCase {

    private final JteReferenceImporter importer = new JteReferenceImporter();

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return JAVA_21;
    }

    public void testOnTheFlyImportsEnabledOnlyWhenSettingIsOn() {
        myFixture.configureByText("test.jte", "@param String name\n${name}");

        CodeInsightSettings settings = CodeInsightSettings.getInstance();
        boolean previous = settings.ADD_UNAMBIGIOUS_IMPORTS_ON_THE_FLY;
        try {
            settings.ADD_UNAMBIGIOUS_IMPORTS_ON_THE_FLY = false;
            assertFalse(importer.isAddUnambiguousImportsOnTheFlyEnabled(myFixture.getFile()));

            settings.ADD_UNAMBIGIOUS_IMPORTS_ON_THE_FLY = true;
            assertTrue(importer.isAddUnambiguousImportsOnTheFlyEnabled(myFixture.getFile()));
        } finally {
            settings.ADD_UNAMBIGIOUS_IMPORTS_ON_THE_FLY = previous;
        }
    }

    public void testComputeAutoImportInsertsImportForSingleCandidate() {
        myFixture.configureByText("test.jte", "@param String name\n${new java.util.concurrent.atomic.AtomicInteger().incrementAndGet() + HashMap.class.getName().length()}");

        int offset = myFixture.getFile().getText().indexOf("HashMap");

        BooleanSupplier supplier = importer.computeAutoImportAtOffset(myFixture.getEditor(), myFixture.getFile(), offset, true);
        assertNotNull(supplier);
        assertTrue(supplier.getAsBoolean());

        myFixture.checkResult("@import java.util.HashMap\n\n@param String name\n${new java.util.concurrent.atomic.AtomicInteger().incrementAndGet() + HashMap.class.getName().length()}");
    }

    public void testComputeAutoImportReturnsNullForAmbiguousCandidate() {
        myFixture.addClass("package a; public class Foo {}");
        myFixture.addClass("package b; public class Foo {}");

        myFixture.configureByText("test.jte", "@param String name\n${new java.util.concurrent.atomic.AtomicInteger().incrementAndGet() + Foo.class.getName().length()}");

        int offset = myFixture.getFile().getText().indexOf("Foo");

        assertNull(importer.computeAutoImportAtOffset(myFixture.getEditor(), myFixture.getFile(), offset, true));
    }

    public void testComputeAutoImportReturnsNullForResolvedReference() {
        myFixture.configureByText("test.jte", "@import java.util.HashMap\n@param String name\n${new HashMap<String, String>().size()}");

        int offset = myFixture.getFile().getText().indexOf("HashMap", myFixture.getFile().getText().indexOf("new"));

        assertNull(importer.computeAutoImportAtOffset(myFixture.getEditor(), myFixture.getFile(), offset, true));
    }
}
