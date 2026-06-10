package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtProperty;

import java.util.Collection;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class KteSyntheticKotlinReferenceSearchTest extends KteK2FixtureSupport {
    public void testReferencesSearchFindsOutputPropertyReference() throws Exception {
        myFixture.addFileToProject("src/com/example/Profile.kt", """
                package com.example

                data class Profile(val displayName: String)
                """);

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.display<caret>Name}
                """);

        PsiElement resolved = resolveReferenceAtCaret();
        Collection<PsiReference> references = searchReferencesInFileOffEdt(resolved, myFixture.getFile());

        assertTrue(references.stream().anyMatch(reference -> "displayName".equals(reference.getCanonicalText())));
    }

    public void testReferencesSearchFindsKteUsagesOfKotlinClass() throws Exception {
        PsiElement profileFile = myFixture.addFileToProject("src/com/example/Profile.kt", """
                package com.example

                data class Profile(val displayName: String)
                """);
        KtClass profileClass = PsiTreeUtil.findChildOfType(profileFile, KtClass.class);
        assertNotNull(profileClass);

        myFixture.configureByText("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.displayName}
                """);

        Collection<PsiReference> references = searchReferencesOffEdt(profileClass);

        assertTrue(references.stream()
                .filter(reference -> reference.getElement().getContainingFile() == myFixture.getFile())
                .map(PsiReference::getCanonicalText)
                .anyMatch("Profile"::equals));
    }

    public void testReferencesSearchFindsKteUsagesOfTopLevelFunction() throws Exception {
        addSupportHelpers();
        PsiFile helpersFile = findTempPsiFile("src/com/example/SupportHelpers.kt");
        KtNamedFunction i18nFunction = PsiTreeUtil.findChildOfType(helpersFile, KtNamedFunction.class);
        assertNotNull(i18nFunction);

        myFixture.configureByText("helpers.kte", """
                @import com.example.i18n
                ${i18n("common.ok")}
                """);

        Collection<PsiReference> references = searchReferencesOffEdt(i18nFunction);

        assertTrue(references.stream()
                .filter(reference -> reference.getElement().getContainingFile() == myFixture.getFile())
                .map(PsiReference::getCanonicalText)
                .anyMatch("i18n"::equals));
    }

    public void testReferencesSearchFindsKteUsagesOfObjectProperty() throws Exception {
        addSupportHelpers();
        PsiFile helpersFile = findTempPsiFile("src/com/example/SupportHelpers.kt");
        KtProperty titleProperty = PsiTreeUtil.findChildrenOfType(helpersFile, KtProperty.class).stream()
                .filter(property -> "title".equals(property.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing UiLabels.title fixture"));

        myFixture.configureByText("labels.kte", """
                @import com.example.UiLabels.title
                ${title}
                """);

        Collection<PsiReference> references = searchReferencesOffEdt(titleProperty);

        assertTrue(references.stream()
                .filter(reference -> reference.getElement().getContainingFile() == myFixture.getFile())
                .map(PsiReference::getCanonicalText)
                .anyMatch("title"::equals));
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

    private PsiFile findTempPsiFile(String path) {
        VirtualFile virtualFile = myFixture.findFileInTempDir(path);
        assertNotNull(virtualFile);
        PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(virtualFile);
        assertNotNull(psiFile);
        return psiFile;
    }
}
