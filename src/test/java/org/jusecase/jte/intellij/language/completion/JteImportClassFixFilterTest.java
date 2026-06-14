package org.jusecase.jte.intellij.language.completion;

import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class JteImportClassFixFilterTest extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return JAVA_21;
    }

    public void testBuiltInImportClassFixIsHiddenForUnresolvedReferenceInJteFile() {
        myFixture.configureByText("test.jte", "@param String name\n${new java.util.concurrent.atomic.AtomicInteger().incrementAndGet() + <caret>HashMap.class.getName().length()}");

        assertNotNull(myFixture.getAvailableIntention("Add @import java.util.HashMap"));
        assertNull(myFixture.getAvailableIntention("Import class"));
    }

    public void testBuiltInImportClassFixIsStillAvailableForUnresolvedReferenceInJavaFile() {
        myFixture.configureByText("Test.java", "class Test { void m() { Object o = new <caret>HashMap<String, String>(); } }");

        assertNotNull(myFixture.getAvailableIntention("Import class"));
    }
}
