package org.jusecase.jte.intellij.language;

import org.junit.Assert;
import org.junit.Test;
import org.jusecase.jte.intellij.language.KteKotlinContentManipulator.ImportReplacement;

public class KteKotlinContentManipulatorTest {

    @Test
    public void testBrokenImport() {
        ImportReplacement importReplacement = KteKotlinContentManipulator.isRequiredToOptimizeImports(
                """
                @import java.lang.Packageimport java.util.*

                @param x:String = "foo"
                @param foo2:test.Model""");

        Assert.assertNotNull(importReplacement);
        Assert.assertEquals("@import java.lang.Packageimport java.util.*\n", importReplacement.oldText());
        Assert.assertEquals("@import java.lang.Package\n@import java.util.*\n", importReplacement.newText());
    }

    @Test
    public void testValidImport() {
        ImportReplacement importReplacement = KteKotlinContentManipulator.isRequiredToOptimizeImports(
                """
                @import java.lang.Package
                @import java.util.*
                @param x:String = "foo"
                @param foo2:test.Model""");

        Assert.assertNull(importReplacement);
    }
}
