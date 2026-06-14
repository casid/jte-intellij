package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class JteAddImportIntentionTest extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return JAVA_21;
    }

    public void testAddsImportWhenNoneExist() {
        myFixture.configureByText("test.jte", "@param String name\n${new java.util.concurrent.atomic.AtomicInteger().incrementAndGet() + <caret>HashMap.class.getName().length()}");

        IntentionAction action = myFixture.findSingleIntention("Add @import java.util.HashMap");
        myFixture.launchAction(action);

        myFixture.checkResult("@import java.util.HashMap\n@param String name\n${new java.util.concurrent.atomic.AtomicInteger().incrementAndGet() + HashMap.class.getName().length()}");
    }

    public void testAddsImportWhenOtherImportExists() {
        myFixture.configureByText("test.jte", "@import java.util.List\n@param String name\n${new java.util.concurrent.atomic.AtomicInteger().incrementAndGet() + <caret>HashMap.class.getName().length()}");

        IntentionAction action = myFixture.findSingleIntention("Add @import java.util.HashMap");
        myFixture.launchAction(action);

        myFixture.checkResult("@import java.util.HashMap\n@import java.util.List\n@param String name\n${new java.util.concurrent.atomic.AtomicInteger().incrementAndGet() + HashMap.class.getName().length()}");
    }

    public void testNotAvailableForResolvedReference() {
        myFixture.configureByText("test.jte", "@import java.util.HashMap\n@param String name\n${new <caret>HashMap<String, String>().size()}");

        assertNull(myFixture.getAvailableIntention("Add @import java.util.HashMap"));
    }

    public void testAvailableForAmbiguousName() {
        myFixture.addClass("package a; public class Foo {}");
        myFixture.addClass("package b; public class Foo {}");

        myFixture.configureByText("test.jte", "@param String name\n${new java.util.concurrent.atomic.AtomicInteger().incrementAndGet() + <caret>Foo.class.getName().length()}");

        assertNull(myFixture.getAvailableIntention("Add @import a.Foo"));
        assertNull(myFixture.getAvailableIntention("Add @import b.Foo"));
        assertNotNull(myFixture.getAvailableIntention("Add @import for Foo"));
    }
}
