package org.jusecase.jte.intellij.language.completion;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class JteImportOptimizerTest extends LightJavaCodeInsightFixtureTestCase {

    private final JteImportOptimizer optimizer = new JteImportOptimizer();

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return JAVA_21;
    }

    public void testSupportsJteFilesOnly() {
        myFixture.configureByText("test.jte", "@param String name\n${name}");
        assertTrue(optimizer.supports(myFixture.getFile()));

        myFixture.configureByText("Test.java", "class Test {}");
        assertFalse(optimizer.supports(myFixture.getFile()));
    }

    public void testRemovesUnusedImport() {
        myFixture.configureByText("test.jte", "@import java.util.HashMap\n@param String name\n${name}");

        optimize();

        myFixture.checkResult("@param String name\n${name}");
    }

    public void testKeepsUsedImportAndRemovesUnusedOne() {
        myFixture.configureByText("test.jte", "@import java.util.ArrayList\n@import java.util.HashMap\n\n@param String name\n${new HashMap<String, String>().size()}");

        optimize();

        myFixture.checkResult("@import java.util.HashMap\n\n@param String name\n${new HashMap<String, String>().size()}");
    }

    public void testDoesNotChangeFileWithOnlyUsedImports() {
        myFixture.configureByText("test.jte", "@import java.util.HashMap\n\n@param String name\n${new HashMap<String, String>().size()}");

        optimize();

        myFixture.checkResult("@import java.util.HashMap\n\n@param String name\n${new HashMap<String, String>().size()}");
    }

    public void testKeepsWildcardImportEvenIfUnused() {
        myFixture.configureByText("test.jte", "@import java.util.*\n\n@param String name\n${name}");

        optimize();

        myFixture.checkResult("@import java.util.*\n\n@param String name\n${name}");
    }

    public void testKeepsStaticImportEvenIfUnused() {
        myFixture.configureByText("test.jte", "@import static java.util.Map.entry\n\n@param String name\n${name}");

        optimize();

        myFixture.checkResult("@import static java.util.Map.entry\n\n@param String name\n${name}");
    }

    private void optimize() {
        Runnable runnable = optimizer.processFile(myFixture.getFile());
        WriteCommandAction.runWriteCommandAction(getProject(), runnable);
    }
}
