package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JteRemoveUnusedImportIntentionTest extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return JAVA_21;
    }

    public void testRemovesUnusedImportUnderCaret() {
        myFixture.configureByText("test.jte", "@import java.util.Has<caret>hMap\n@param String name\n${name}");

        IntentionAction intention = findIntention();
        myFixture.launchAction(intention);

        myFixture.checkResult("@param String name\n${name}");
    }

    public void testKeepsUsedImportAndRemovesOthers() {
        myFixture.configureByText("test.jte", "@import java.util.ArrayList\n@import java.util.Has<caret>hMap\n\n@param String name\n${name}");

        IntentionAction intention = findIntention();
        myFixture.launchAction(intention);

        myFixture.checkResult("@param String name\n${name}");
    }

    public void testRemovesDuplicateImport() {
        myFixture.configureByText("test.jte", "@import java.util.Date\n@import java.util.D<caret>ate\n\n@param String name\n${new Date()}");

        IntentionAction intention = findIntention();
        myFixture.launchAction(intention);

        myFixture.checkResult("@import java.util.Date\n\n@param String name\n${new Date()}");
    }

    public void testNotAvailableForUsedImport() {
        myFixture.configureByText("test.jte", "@import java.util.Has<caret>hMap\n\n@param String name\n${new HashMap<String, String>().size()}");

        assertNull(findIntentionOrNull());
    }

    public void testNotAvailableOutsideImport() {
        myFixture.configureByText("test.jte", "@import java.util.HashMap\n@param String na<caret>me\n${name}");

        assertNull(findIntentionOrNull());
    }

    private IntentionAction findIntention() {
        IntentionAction intention = findIntentionOrNull();
        assertNotNull("Expected 'Remove unused @imports' intention to be available", intention);
        return intention;
    }

    private IntentionAction findIntentionOrNull() {
        List<IntentionAction> actions = myFixture.filterAvailableIntentions("Remove unused @imports");
        return actions.isEmpty() ? null : actions.get(0);
    }
}
