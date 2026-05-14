package org.jusecase.jte.intellij.language.parsing;

import java.util.ArrayList;
import java.util.List;

public class JteRootParser {

    public static final String IMPORT = "@import";

    public static List<String> parse(String content) {
        int currentIndex = 0;
        List<String> result = new ArrayList<>();

        while (currentIndex < content.length()) {
            int index = content.indexOf(IMPORT, currentIndex);
            if (index == -1) {
                break;
            }

            int startIndex = index + IMPORT.length();

            int endIndex = content.indexOf('\n', startIndex);
            if (endIndex == -1) {
                endIndex = content.length();
            }

            String path = content.substring(startIndex, endIndex).trim();
            if (!path.isEmpty()) {
                result.add(path);
            }

            currentIndex = endIndex;
        }

        return result;
    }
}
