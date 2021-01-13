package org.jusecase.jte.intellij.language.format;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.testFramework.LightIdeaTestCase;
import com.intellij.util.IncorrectOperationException;

import java.util.Objects;

public class JteFormatterTest extends LightIdeaTestCase {

    public void testEmptyFile() {
        reformatCode("", "");
    }

    public void testHtmlCode() {
        reformatCode(
                "<a href   =    \"url\">foo</a>",
                "<a href=\"url\">foo</a>"
        );
    }

    public void testImports() {
        reformatCode(
                "@import java.lang.String\n     @import java.lang.Object\nHello",
                "@import java.lang.String\n@import java.lang.Object\nHello"
        );
    }

    public void testParams() {
        reformatCode(
                "@param String x\n      @param String y\nHello ${x}",
                "@param String x\n@param String y\nHello ${x}"
        );
    }

    @SuppressWarnings("Convert2Lambda")
    private void reformatCode(final String code, String expectedResult) throws IncorrectOperationException {
        final PsiFile file = createFile("test.jte", code);

        final PsiDocumentManager manager = PsiDocumentManager.getInstance(getProject());
        final Document document = manager.getDocument(file);

        CommandProcessor.getInstance().executeCommand(getProject(), new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        Objects.requireNonNull(document).replaceString(0, document.getTextLength(), code);
                        manager.commitDocument(document);
                        try {
                            TextRange rangeToUse = file.getTextRange();
                            CodeStyleManager.getInstance(getProject()).reformatText(file, rangeToUse.getStartOffset(), rangeToUse.getEndOffset());
                        } catch (IncorrectOperationException e) {
                            fail(e.getLocalizedMessage());
                        }
                    }
                });
            }
        }, "", "");

        if (document == null) {
            fail("Don't expect the document to be null");
            return;
        }
        assertEquals("Reformat Code failed", prepareText(expectedResult), prepareText(document.getText()));
        manager.commitDocument(document);
        assertEquals("Reformat Code failed", prepareText(expectedResult), prepareText(file.getText()));
    }

    protected String prepareText(String text) {
        return text;
    }
}
