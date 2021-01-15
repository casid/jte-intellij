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

    public void testIf() {
        reformatCode(
                "@if(true)\nIt is true\n@endif",
                "@if(true)\n    It is true\n@endif"
        );
    }

    public void testIfElse() {
        reformatCode(
                "@if(true)\nIt is <b>true</b>\n@else\nIt is <b>false</b>\n@endif",
                "@if(true)\n    It is <b>true</b>\n@else\n    It is <b>false</b>\n@endif"
        );
    }

    public void testIfElseIf() {
        reformatCode(
                "@if(true)\nIt is <b>true</b>\n@elseif(false)\nIt is <b>false</b>\n@endif",
                "@if(true)\n    It is <b>true</b>\n@elseif(false)\n    It is <b>false</b>\n@endif"
        );
    }

    public void testNestedIf() {
        reformatCode(
                "@if(true)\n" +
                "It is <b>true</b>\n" +
                "@else\n" +
                "@if(true)\n" +
                "True?\n" +
                "@else\n" +
                "It is <b>false</b>\n" +
                "@endif\n" +
                "@endif",

                "@if(true)\n" +
                "    It is <b>true</b>\n" +
                "@else\n" +
                "    @if(true)\n" +
                "        True?\n" +
                "    @else\n" +
                "        It is <b>false</b>\n" +
                "    @endif\n" +
                "@endif"
        );
    }

    public void testFor() {
        reformatCode("@for(int i = 0; i < 100; ++i)\n" +
                "i is ${i}\n" +
                "@endfor\n",

                "@for(int i = 0; i < 100; ++i)\n" +
                "    i is ${i}\n" +
                "@endfor\n"
        );
    }

    public void testContentBlock() {
        reformatCode("@tag.card(content = @`\n" +
                "This is <b>my content</b>!\n" +
                "`)",

                "@tag.card(content = @`\n" +
                "    This is <b>my content</b>!\n" +
                "`)"
        );
    }

    public void testOutputInDiv() {
        reformatCode(
                "<div>\n" +
                "x is ${x}\n" +
                "</div>\n",

                "<div>\n" +
                "    x is ${x}\n" +
                "</div>\n"
        );
    }

    public void testOutputInDiv_withoutOtherText() {
        reformatCode(
                "<div>\n" +
                "${x}\n" +
                "</div>\n",

                "<div>\n" +
                "    ${x}\n" +
                "</div>\n"
        );
    }

    public void testOutputInIf() {
        reformatCode(
                "@if(true)\n" +
                "${x}\n" +
                "@endif\n",

                "@if(true)\n" +
                "    ${x}\n" +
                "@endif\n"
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
