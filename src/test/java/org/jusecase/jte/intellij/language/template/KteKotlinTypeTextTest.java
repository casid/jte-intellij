package org.jusecase.jte.intellij.language.template;

import org.jusecase.jte.intellij.language.template.KteKotlinTypeText;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class KteKotlinTypeTextTest {
    @Test
    public void testRawAndShortTypes() {
        assertEquals("List", KteKotlinTypeText.rawType("List<Profile?>?"));
        assertEquals("Profile", KteKotlinTypeText.shortName("com.example.Profile"));
    }

    @Test
    public void testFirstGenericArgument() {
        assertEquals("String", KteKotlinTypeText.firstGenericArgument("List<String>"));
        assertEquals("String", KteKotlinTypeText.firstGenericArgument("Map<String, List<Profile>>"));
        assertNull(KteKotlinTypeText.firstGenericArgument("String"));
    }

    @Test
    public void testGenericArgumentsPreserveNestedTypes() {
        assertEquals(
                List.of("String", "List<Profile?>", "Map<String, Int>"),
                KteKotlinTypeText.genericArguments("Map<String, List<Profile?>, Map<String, Int>>")
        );
    }
}
