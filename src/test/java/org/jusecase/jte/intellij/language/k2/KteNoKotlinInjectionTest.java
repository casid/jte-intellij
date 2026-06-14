package org.jusecase.jte.intellij.language.k2;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.List;

public class KteNoKotlinInjectionTest extends KteK2FixtureSupport {
    public void testKteExpressionDoesNotCreateInjectedKotlinFile() {
        addProfileClassWithKotlinProperties();

        PsiFile configuredFile = myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.<caret>}
                """);

        assertNoInjectedElementAtCaret(configuredFile);
        assertDoesNotContainImportDirectiveCodeFragmentError();
    }

    public void testKteImportDoesNotCreateInjectedKotlinFile() {
        PsiFile configuredFile = myFixture.configureByText("imports.kte", """
                @import com.example.Profile<caret>
                """);

        assertNoInjectedElementAtCaret(configuredFile);
        assertDoesNotContainImportDirectiveCodeFragmentError();
    }

    public void testKteParamDoesNotCreateInjectedKotlinFile() {
        PsiFile configuredFile = myFixture.configureByText("params.kte", """
                @import com.example.Profile
                @param profile: Profile<caret>
                """);

        assertNoInjectedElementAtCaret(configuredFile);
        assertDoesNotContainImportDirectiveCodeFragmentError();
    }

    private void assertNoInjectedElementAtCaret(PsiFile configuredFile) {
        InjectedLanguageManager manager = InjectedLanguageManager.getInstance(getProject());
        PsiElement injectedElement = manager.findInjectedElementAt(configuredFile, myFixture.getCaretOffset());

        assertNull(injectedElement);
    }

    private void assertDoesNotContainImportDirectiveCodeFragmentError() {
        List<String> descriptions = errorDescriptions();
        assertFalse(
                "Did not expect Kotlin code-fragment import/package errors in " + descriptions,
                descriptions.stream().anyMatch(description ->
                        description.contains("Package directive and imports are forbidden in code fragments"))
        );
    }
}
