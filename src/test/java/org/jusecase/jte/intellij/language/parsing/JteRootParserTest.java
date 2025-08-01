package org.jusecase.jte.intellij.language.parsing;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class JteRootParserTest {
    @Test
    public void empty() {
        assertEquals(0, JteRootParser.parse("").size());
    }

    @Test
    public void importWithoutPath() {
        assertEquals(0, JteRootParser.parse("@import").size());
    }

    @Test
    public void importWithPath() {
        List<String> imports = JteRootParser.parse("@import ../../foo/bar");
        assertEquals(1, imports.size());
        assertEquals("../../foo/bar", imports.get(0));
    }

    @Test
    public void importWithPaths() {
        List<String> imports = JteRootParser.parse("@import ../../foo/bar\n@import ../../bar/foo");
        assertEquals(2, imports.size());
        assertEquals("../../foo/bar", imports.get(0));
        assertEquals("../../bar/foo", imports.get(1));
    }

    @Test
    public void importWithPaths_windowsLineBreaks() {
        List<String> imports = JteRootParser.parse("@import ../../foo/bar\r\n@import ../../bar/foo");
        assertEquals(2, imports.size());
        assertEquals("../../foo/bar", imports.get(0));
        assertEquals("../../bar/foo", imports.get(1));
    }
}