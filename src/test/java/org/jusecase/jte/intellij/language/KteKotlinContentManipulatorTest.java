package org.jusecase.jte.intellij.language;

import org.junit.Assert;
import org.junit.Test;
import org.jusecase.jte.intellij.language.KteKotlinContentManipulator.ImportReplacement;

public class KteKotlinContentManipulatorTest {

    @Test
    public void testBrokenImport() {
        ImportReplacement importReplacement = KteKotlinContentManipulator.isRequiredToOptimizeImports(
                "@import java.lang.Packageimport java.util.*\n" +
                "\n" +
                "@param x:String = \"foo\"\n" +
                "@param foo2:test.Model");

        Assert.assertNotNull(importReplacement);
        Assert.assertEquals(importReplacement.oldText, "@import java.lang.Packageimport java.util.*\n");
        Assert.assertEquals(importReplacement.newText, "@import java.lang.Package\n@import java.util.*\n");
    }

    @Test
    public void testValidImport() {
        ImportReplacement importReplacement = KteKotlinContentManipulator.isRequiredToOptimizeImports(
                "@import java.lang.Package\n" +
                "@import java.util.*\n" +
                "@param x:String = \"foo\"\n" +
                "@param foo2:test.Model");

        Assert.assertNull(importReplacement);
    }

    @Test
    public void name() {
        String text = "@import org.example.AnotherDummyimport org.example.Dummy\n" +
                "\n" +
                "@param foo: String?\n" +
                "@param bar: Dummy\n" +
                "@param x: AnotherDummy\n" +
                "\n" +
                "@if(foo?.isBlank())\n" +
                "    Hello\n" +
                "@else\n" +
                "    ${foo}\n" +
                "@endif\n" +
                "\n" +
                "\n";
    }
}