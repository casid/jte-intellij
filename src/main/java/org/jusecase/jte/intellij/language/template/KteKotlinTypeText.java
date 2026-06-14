package org.jusecase.jte.intellij.language.template;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class KteKotlinTypeText {
    private KteKotlinTypeText() {
    }

    @NotNull
    public static String rawType(@NotNull String typeText) {
        String result = typeText.trim();
        int genericStart = result.indexOf('<');
        if (genericStart != -1) {
            result = result.substring(0, genericStart);
        }

        return result.replace("?", "").trim();
    }

    @NotNull
    public static String shortName(@NotNull String typeName) {
        int lastDot = typeName.lastIndexOf('.');
        return lastDot == -1 ? typeName : typeName.substring(lastDot + 1);
    }

    @Nullable
    public static String firstGenericArgument(@NotNull String typeText) {
        int start = typeText.indexOf('<');
        if (start == -1) {
            return null;
        }

        int depth = 0;
        for (int index = start + 1; index < typeText.length(); index++) {
            char c = typeText.charAt(index);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                if (depth == 0) {
                    return typeText.substring(start + 1, index).trim();
                }
                depth--;
            } else if (c == ',' && depth == 0) {
                return typeText.substring(start + 1, index).trim();
            }
        }

        return null;
    }

    @NotNull
    public static List<String> genericArguments(@NotNull String typeText) {
        int start = typeText.indexOf('<');
        if (start == -1) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        int depth = 0;
        int argumentStart = start + 1;
        for (int index = start + 1; index < typeText.length(); index++) {
            char current = typeText.charAt(index);
            if (current == '<') {
                depth++;
            } else if (current == '>') {
                if (depth == 0) {
                    addGenericArgument(result, typeText, argumentStart, index);
                    return result;
                }
                depth--;
            } else if (current == ',' && depth == 0) {
                addGenericArgument(result, typeText, argumentStart, index);
                argumentStart = index + 1;
            }
        }

        return result;
    }

    private static void addGenericArgument(@NotNull List<String> result,
                                           @NotNull String typeText,
                                           int start,
                                           int end) {
        String argument = typeText.substring(start, end).trim();
        if (!argument.isEmpty()) {
            result.add(argument);
        }
    }
}
