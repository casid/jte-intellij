package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.unusedImport.UnusedImportInspection;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JteUnusedImportFixFilterTest extends LightJavaCodeInsightFixtureTestCase {

    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return JAVA_21;
    }

    public void testHidesBuiltInRemoveUnusedImportsFix() {
        myFixture.enableInspections(new UnusedImportInspection());
        myFixture.configureByText("test.jte", "@import java.util.Has<caret>hMap\n@param String name\n${name}");
        myFixture.doHighlighting();

        assertNoIntention("Remove unused imports");
    }

    private void assertNoIntention(String text) {
        List<IntentionAction> actions = myFixture.filterAvailableIntentions(text);
        assertTrue("Did not expect to find intention '" + text + "', but found: " + actions, actions.isEmpty());
    }
}
