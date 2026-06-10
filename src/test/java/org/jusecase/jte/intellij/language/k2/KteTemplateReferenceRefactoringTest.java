package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.refactoring.rename.RenameProcessor;

import java.util.Collection;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class KteTemplateReferenceRefactoringTest extends KteK2FixtureSupport {
    public void testKteTemplateReferenceResolvesToTemplateFile() {
        addTemplateRoot();
        addSignatureTemplate();

        myFixture.configureByText("caller.kte", """
                @template.components.signatureKitchen<caret>Sink(profile = broken, tags = broken, content = broken)
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertTrue(resolved instanceof PsiFile);
        assertEquals("signatureKitchenSink.kte", ((PsiFile) resolved).getName());
    }

    public void testReferencesSearchFindsKteTemplateCall() throws Exception {
        addTemplateRoot();
        PsiFile child = (PsiFile) myFixture.addFileToProject("components/card.kte", """
                @param title: String
                """);
        PsiFile caller = (PsiFile) myFixture.addFileToProject("caller.kte", """
                @template.components.card(title = "Title")
                """);

        Collection<PsiReference> references = searchReferencesInFileOffEdt(child, caller);

        assertTrue(references.stream().anyMatch(reference -> "card".equals(reference.getCanonicalText())));
    }

    public void testTemplateReferenceBindUpdatesWholeRootRelativePath() {
        addTemplateRoot();
        myFixture.addFileToProject("components/card.kte", """
                @param title: String
                """);
        PsiFile target = (PsiFile) myFixture.addFileToProject("layouts/card.kte", """
                @param title: String
                """);

        myFixture.configureByText("caller.kte", """
                @template.components.car<caret>d(title = "Title")
                """);

        bindReferenceAtCaret(target);

        assertEquals("""
                @template.layouts.card(title = "Title")
                """, myFixture.getFile().getText());
    }

    public void testKteChildParamNameReferenceResolvesToKteSignatureParam() {
        addTemplateRoot();
        addSignatureTemplate();

        myFixture.configureByText("caller.kte", """
                @template.components.signatureKitchenSink(pro<caret>file = broken, tags = broken, content = broken)
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertEquals("signatureKitchenSink.kte", resolved.getContainingFile().getName());
        assertTrue(resolved.getText().contains("profile: Profile"));
    }

    public void testJteChildParamNameReferenceResolvesToKteSignatureParam() {
        addTemplateRoot();
        addSignatureTemplate();

        myFixture.configureByText("caller.jte", """
                @template.components.signatureKitchenSink(pro<caret>file = broken, tags = broken, content = broken)
                """);

        PsiElement resolved = resolveReferenceAtCaret();

        assertEquals("signatureKitchenSink.kte", resolved.getContainingFile().getName());
        assertTrue(resolved.getText().contains("profile: Profile"));
    }

    public void testReferencesSearchFindsKteAndJteChildParamUsages() throws Exception {
        addTemplateRoot();
        PsiFile child = (PsiFile) myFixture.addFileToProject("components/signatureKitchenSink.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param tags: List<String>
                @param content: gg.jte.Content
                """);
        myFixture.addFileToProject("caller.kte", """
                @template.components.signatureKitchenSink(profile = broken, tags = broken, content = broken)
                """);
        myFixture.addFileToProject("caller.jte", """
                @template.components.signatureKitchenSink(profile = broken, tags = broken, content = broken)
                """);

        KteTemplateSignatureService.Parameter parameter =
                KteTemplateSignatureService.resolve(child).parameter("profile");
        assertNotNull(parameter);

        Collection<PsiReference> references = searchReferencesOffEdt(parameter.sourceElement());

        String referenceSummary = references.stream()
                .map(reference -> reference.getElement().getContainingFile().getName() + ":" + reference.getCanonicalText())
                .toList()
                .toString();
        assertTrue(referenceSummary, references.stream().anyMatch(reference ->
                "caller.kte".equals(reference.getElement().getContainingFile().getName()) &&
                        "profile".equals(reference.getCanonicalText())));
        assertTrue(referenceSummary, references.stream().anyMatch(reference ->
                "caller.jte".equals(reference.getElement().getContainingFile().getName()) &&
                        "profile".equals(reference.getCanonicalText())));
    }

    public void testChildParamNameRenameUpdatesKteCallSite() {
        addTemplateRoot();
        addSignatureTemplate();

        myFixture.configureByText("caller.kte", """
                @template.components.signatureKitchenSink(pro<caret>file = broken, tags = broken, content = broken)
                """);

        renameReferenceAtCaret("account");

        assertEquals("""
                @template.components.signatureKitchenSink(account = broken, tags = broken, content = broken)
                """, myFixture.getFile().getText());
    }

    public void testChildParamNameRenameUpdatesJteCallSiteForKteChild() {
        addTemplateRoot();
        addSignatureTemplate();

        myFixture.configureByText("caller.jte", """
                @template.components.signatureKitchenSink(pro<caret>file = broken, tags = broken, content = broken)
                """);

        renameReferenceAtCaret("account");

        assertEquals("""
                @template.components.signatureKitchenSink(account = broken, tags = broken, content = broken)
                """, myFixture.getFile().getText());
    }

    public void testSourceChildParamRenameUpdatesKteAndJteCallSites() {
        addTemplateRoot();
        PsiFile child = (PsiFile) myFixture.addFileToProject("components/signatureKitchenSink.kte", """
                @import com.example.Profile
                @param profile: Profile
                @param tags: List<String>
                @param content: gg.jte.Content
                """);
        PsiFile kteCaller = (PsiFile) myFixture.addFileToProject("caller.kte", """
                @template.components.signatureKitchenSink(profile = broken, tags = broken, content = broken)
                """);
        PsiFile jteCaller = (PsiFile) myFixture.addFileToProject("caller.jte", """
                @template.components.signatureKitchenSink(profile = broken, tags = broken, content = broken)
                """);

        KteTemplateSignatureService.Parameter parameter =
                KteTemplateSignatureService.resolve(child).parameter("profile");
        assertNotNull(parameter);

        new RenameProcessor(getProject(), parameter.sourceElement(), "account", false, false).run();

        assertEquals("""
                @import com.example.Profile
                @param account: Profile
                @param tags: List<String>
                @param content: gg.jte.Content
                """, child.getText());
        assertEquals("""
                @template.components.signatureKitchenSink(account = broken, tags = broken, content = broken)
                """, kteCaller.getText());
        assertEquals("""
                @template.components.signatureKitchenSink(account = broken, tags = broken, content = broken)
                """, jteCaller.getText());
    }

    private Collection<PsiReference> searchReferencesInFileOffEdt(PsiElement resolved, PsiFile file) throws Exception {
        Future<Collection<PsiReference>> future = ApplicationManager.getApplication().executeOnPooledThread(
                () -> ApplicationManager.getApplication().runReadAction(
                        (Computable<Collection<PsiReference>>) () ->
                                ReferencesSearch.search(resolved, new LocalSearchScope(file)).findAll()
                )
        );
        return future.get(30, TimeUnit.SECONDS);
    }

    private Collection<PsiReference> searchReferencesOffEdt(PsiElement resolved) throws Exception {
        Future<Collection<PsiReference>> future = ApplicationManager.getApplication().executeOnPooledThread(
                () -> ApplicationManager.getApplication().runReadAction(
                        (Computable<Collection<PsiReference>>) () -> ReferencesSearch.search(resolved).findAll()
                )
        );
        return future.get(30, TimeUnit.SECONDS);
    }

    private void renameReferenceAtCaret(String newName) {
        WriteCommandAction.runWriteCommandAction(
                getProject(),
                (Runnable) () -> referenceAtCaret().handleElementRename(newName)
        );
    }

    private void bindReferenceAtCaret(PsiElement element) {
        WriteCommandAction.runWriteCommandAction(
                getProject(),
                (Runnable) () -> referenceAtCaret().bindToElement(element)
        );
    }
}
