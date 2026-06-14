package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtPsiFactoryKt;

import java.io.IOException;

public class KteSyntheticKotlinModelServiceTest extends LightJavaCodeInsightFixtureTestCase {
    public void testCachesModelUntilTemplatePsiChanges() {
        PsiFile file = configureKteFile("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.name}
                """);
        KteSyntheticKotlinModelService service = KteSyntheticKotlinModelService.getInstance(getProject());

        KteSyntheticKotlinModel first = service.getModel(file);
        KteSyntheticKotlinModel second = service.getModel(file);

        assertSame(first, second);

        replaceText(file, "profile.name", "profile.email");
        KteSyntheticKotlinModel afterEdit = service.getModel(file);

        assertNotSame(first, afterEdit);
        assertTrue(afterEdit.getSyntheticFile().getText().contains("profile.email"));
    }

    public void testModelProvidesSyntheticKotlinPsiAndMappings() {
        PsiFile file = configureKteFile("profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.name}
                """);

        KteSyntheticKotlinModel model = KteSyntheticKotlinModelService.getInstance(getProject()).getModel(file);

        assertEquals(1, model.getKtFile().getImportDirectives().size());
        assertNotNull(PsiTreeUtil.findChildOfType(model.getKtFile(), KtClass.class));
        assertFalse(model.getSyntheticFile().getMappings().isEmpty());
    }

    public void testSyntheticKotlinPsiUsesSourceRootAnalysisContext() {
        myFixture.addFileToProject("src/main/kotlin/com/example/Profile.kt", """
                package com.example

                data class Profile(val name: String)
                """);
        PsiFile file = configureKteFile("src/main/jte/profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.name}
                """);

        KteSyntheticKotlinModel model = KteSyntheticKotlinModelService.getInstance(getProject()).getModel(file);

        assertNotNull(model.getSourceRoot());
        PsiElement analysisContext = KtPsiFactoryKt.getAnalysisContext(model.getKtFile());
        assertTrue(analysisContext instanceof PsiDirectory);
        assertEquals(model.getSourceRoot(), ((PsiDirectory) analysisContext).getVirtualFile());
    }

    public void testWritesPhysicalSyntheticFile() throws IOException {
        PsiFile file = configureKteFile("templates/profile.kte", """
                @import com.example.Profile
                @param profile: Profile
                ${profile.name}
                """);

        KteSyntheticKotlinModelService.PhysicalFile physicalFile =
                KteSyntheticKotlinModelService.getInstance(getProject()).writePhysicalSyntheticFile(file);

        assertNotNull(physicalFile);
        assertEquals("profile.synthetic.kt", physicalFile.virtualFile().getName());
        assertEquals(physicalFile.model().getSyntheticFile().getText(), VfsUtilCore.loadText(physicalFile.virtualFile()));
    }

    public void testRecursiveTemplateCallDoesNotRecurseBuildingSyntheticModel() {
        myFixture.addFileToProject(".jteroot", "");
        PsiFile file = configureKteFile("components/node.kte", """
                @param title: String
                @template.components.node(title = title)
                """);

        KteSyntheticKotlinModel model = KteSyntheticKotlinModelService.getInstance(getProject()).getModel(file);

        assertTrue(model.getSyntheticFile().getText().contains("fun __jte_template_"));
        assertTrue(model.getSyntheticFile().getText().contains("title = title"));
    }

    public void testCyclicTemplateCallsDoNotRecurseBuildingSyntheticModel() {
        myFixture.addFileToProject(".jteroot", "");
        myFixture.addFileToProject("components/b.kte", """
                @param title: String
                @template.components.a(title = title)
                """);
        PsiFile file = configureKteFile("components/a.kte", """
                @param title: String
                @template.components.b(title = title)
                """);

        KteSyntheticKotlinModel model = KteSyntheticKotlinModelService.getInstance(getProject()).getModel(file);

        assertTrue(model.getSyntheticFile().getText().contains("fun __jte_template_"));
        assertTrue(model.getSyntheticFile().getText().contains("title = title"));
    }

    private PsiFile configureKteFile(String fileName, String text) {
        PsiFile file = myFixture.addFileToProject(fileName, text);
        myFixture.configureFromExistingVirtualFile(file.getVirtualFile());
        return myFixture.getFile();
    }

    private void replaceText(PsiFile file, String oldText, String newText) {
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(getProject());
        Document document = documentManager.getDocument(file);
        assertNotNull(document);

        int startOffset = document.getText().indexOf(oldText);
        assertTrue(startOffset >= 0);
        WriteCommandAction.runWriteCommandAction(getProject(), () ->
                document.replaceString(startOffset, startOffset + oldText.length(), newText)
        );
        documentManager.commitDocument(document);
    }
}
