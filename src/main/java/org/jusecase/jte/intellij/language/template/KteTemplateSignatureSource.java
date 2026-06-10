package org.jusecase.jte.intellij.language.template;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.KteLanguage;
import org.jusecase.jte.intellij.language.psi.JtePsiExtraJavaInjection;
import org.jusecase.jte.intellij.language.psi.JtePsiJavaInjection;
import org.jusecase.jte.intellij.language.psi.JtePsiParam;
import org.jusecase.jte.intellij.language.psi.KtePsiFile;

import java.util.ArrayList;
import java.util.List;

public final class KteTemplateSignatureSource {
    private KteTemplateSignatureSource() {
    }

    public static boolean isKteTemplate(@NotNull PsiFile templateFile) {
        return resolveKteFile(templateFile) != null;
    }

    @Nullable
    public static PsiFile resolveKteFile(@NotNull PsiFile templateFile) {
        if (templateFile instanceof KtePsiFile) {
            return templateFile;
        }

        PsiFile kteFile = templateFile.getViewProvider().getPsi(KteLanguage.INSTANCE);
        return kteFile instanceof KtePsiFile ? kteFile : null;
    }

    @NotNull
    public static Signature resolve(@NotNull PsiFile templateFile) {
        List<Parameter> parameters = new ArrayList<>();
        for (JtePsiParam param : PsiTreeUtil.findChildrenOfType(templateFile, JtePsiParam.class)) {
            JtePsiJavaInjection injection = PsiTreeUtil.getChildOfType(param, JtePsiJavaInjection.class);
            if (injection == null) {
                continue;
            }

            Parameter parameter = parseParameter(param, injection);
            if (parameter != null) {
                parameters.add(parameter);
            }
        }

        return new Signature(templateFile, parameters);
    }

    @Nullable
    private static Parameter parseParameter(@NotNull JtePsiParam param,
                                            @NotNull JtePsiJavaInjection injection) {
        String parameterText = injection.getText();
        ParamDeclaration declaration = ParamDeclaration.parse(parameterText);
        if (declaration == null) {
            return null;
        }

        JtePsiExtraJavaInjection extraDefaultValue = PsiTreeUtil.getChildOfType(param, JtePsiExtraJavaInjection.class);
        boolean hasDefaultValue = declaration.defaultValueRange() != null || extraDefaultValue != null;
        String typeText = declaration.typeText();
        String rawType = KteKotlinTypeText.rawType(typeText);
        TextRange injectionRange = injection.getTextRange();

        TextRange defaultValueRange = declaration.defaultValueRange();
        if (defaultValueRange == null && extraDefaultValue != null) {
            defaultValueRange = extraDefaultValue.getTextRange();
        } else if (defaultValueRange != null) {
            defaultValueRange = defaultValueRange.shiftRight(injectionRange.getStartOffset());
        }

        return new Parameter(
                declaration.name(),
                typeText,
                rawType,
                KteKotlinTypeText.genericArguments(typeText),
                declaration.vararg(),
                !hasDefaultValue && !declaration.vararg(),
                hasDefaultValue,
                typeText.endsWith("?"),
                isContentType(rawType),
                param,
                declaration.declarationRange().shiftRight(injectionRange.getStartOffset()),
                declaration.nameRange().shiftRight(injectionRange.getStartOffset()),
                declaration.typeRange().shiftRight(injectionRange.getStartOffset()),
                defaultValueRange
        );
    }

    private static boolean isContentType(@NotNull String rawType) {
        return "gg.jte.Content".equals(rawType) || "Content".equals(KteKotlinTypeText.shortName(rawType));
    }

    private static int findDefaultValueOffset(@NotNull String text, int startOffset) {
        boolean insideString = false;
        char stringDelimiter = 0;
        int genericDepth = 0;
        int parenthesisDepth = 0;
        int bracketDepth = 0;
        int braceDepth = 0;
        for (int index = startOffset; index < text.length(); index++) {
            char current = text.charAt(index);
            if ((current == '"' || current == '\'') && (index == 0 || text.charAt(index - 1) != '\\')) {
                if (!insideString) {
                    insideString = true;
                    stringDelimiter = current;
                } else if (stringDelimiter == current) {
                    insideString = false;
                    stringDelimiter = 0;
                }
            }
            if (insideString) {
                continue;
            }
            if (current == '<') {
                genericDepth++;
            } else if (current == '>' && genericDepth > 0) {
                genericDepth--;
            } else if (current == '(') {
                parenthesisDepth++;
            } else if (current == ')' && parenthesisDepth > 0) {
                parenthesisDepth--;
            } else if (current == '[') {
                bracketDepth++;
            } else if (current == ']' && bracketDepth > 0) {
                bracketDepth--;
            } else if (current == '{') {
                braceDepth++;
            } else if (current == '}' && braceDepth > 0) {
                braceDepth--;
            } else if (current == '=' &&
                    genericDepth == 0 &&
                    parenthesisDepth == 0 &&
                    bracketDepth == 0 &&
                    braceDepth == 0) {
                return index;
            }
        }

        return -1;
    }

    public record Signature(@NotNull PsiFile templateFile,
                            @NotNull List<Parameter> parameters) {
        public Signature {
            parameters = List.copyOf(parameters);
        }
    }

    public record Parameter(@NotNull String name,
                            @NotNull String typeText,
                            @NotNull String rawType,
                            @NotNull List<String> genericArguments,
                            boolean vararg,
                            boolean required,
                            boolean defaulted,
                            boolean nullable,
                            boolean content,
                            @NotNull PsiElement sourceElement,
                            @NotNull TextRange declarationRange,
                            @NotNull TextRange nameRange,
                            @NotNull TextRange typeRange,
                            @Nullable TextRange defaultValueRange) {
        public Parameter {
            genericArguments = List.copyOf(genericArguments);
        }
    }

    private record ParamDeclaration(@NotNull String name,
                                    @NotNull String typeText,
                                    boolean vararg,
                                    @NotNull TextRange declarationRange,
                                    @NotNull TextRange nameRange,
                                    @NotNull TextRange typeRange,
                                    @Nullable TextRange defaultValueRange) {
        @Nullable
        private static ParamDeclaration parse(@NotNull String text) {
            int index = skipWhitespace(text, 0);
            boolean vararg = false;
            if (startsWithVarargKeyword(text, index)) {
                vararg = true;
                index = skipWhitespace(text, index + "vararg".length());
            }

            int nameStart = index;
            if (nameStart >= text.length() || !Character.isJavaIdentifierStart(text.charAt(nameStart))) {
                return null;
            }

            do {
                index++;
            } while (index < text.length() && Character.isJavaIdentifierPart(text.charAt(index)));
            int nameEnd = index;

            index = skipWhitespace(text, index);
            if (index >= text.length() || text.charAt(index) != ':') {
                return null;
            }

            int typeStart = skipWhitespace(text, index + 1);
            int defaultValueOffset = findDefaultValueOffset(text, typeStart);
            int typeEnd = trimTrailingWhitespace(text, defaultValueOffset == -1 ? text.length() : defaultValueOffset);
            if (typeStart >= typeEnd) {
                return null;
            }

            TextRange defaultValueRange = null;
            if (defaultValueOffset != -1) {
                int defaultValueStart = skipWhitespace(text, defaultValueOffset + 1);
                int defaultValueEnd = trimTrailingWhitespace(text, text.length());
                defaultValueRange = TextRange.create(defaultValueStart, Math.max(defaultValueStart, defaultValueEnd));
            }

            return new ParamDeclaration(
                    text.substring(nameStart, nameEnd),
                    text.substring(typeStart, typeEnd),
                    vararg,
                    TextRange.create(0, defaultValueOffset == -1 ? typeEnd : defaultValueOffset),
                    TextRange.create(nameStart, nameEnd),
                    TextRange.create(typeStart, typeEnd),
                    defaultValueRange
            );
        }

        private static boolean startsWithVarargKeyword(@NotNull String text, int index) {
            String keyword = "vararg";
            int end = index + keyword.length();
            if (end > text.length() || !text.regionMatches(index, keyword, 0, keyword.length())) {
                return false;
            }

            return end == text.length() || !Character.isJavaIdentifierPart(text.charAt(end));
        }

        private static int skipWhitespace(@NotNull String text, int index) {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
            return index;
        }

        private static int trimTrailingWhitespace(@NotNull String text, int index) {
            while (index > 0 && Character.isWhitespace(text.charAt(index - 1))) {
                index--;
            }
            return index;
        }
    }
}
