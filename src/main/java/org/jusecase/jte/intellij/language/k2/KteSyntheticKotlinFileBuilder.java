package org.jusecase.jte.intellij.language.k2;

import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.psi.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class KteSyntheticKotlinFileBuilder {
    private static final String COMPLETION_DUMMY_IDENTIFIER = CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED;
    private static final String CLASS_PREFIX = "@Suppress(\"unused\", \"UNUSED_PARAMETER\")\n" +
            "class DummyTemplate { val jteOutput = gg.jte.TemplateOutput()\n" +
            " @Suppress(\"unused\")\n" +
            " fun render(";
    private static final String CLASS_SUFFIX = "\n}\nfun dummyCall(vararg o: Any?) { o.hashCode() }\n}\n";

    @NotNull
    public KteSyntheticKotlinFile build(@NotNull PsiFile templateFile) {
        return build(templateFile, null);
    }

    @NotNull
    public KteSyntheticKotlinFile buildForCompletion(@NotNull PsiFile templateFile, int templateOffset) {
        return build(templateFile, templateOffset);
    }

    @NotNull
    private KteSyntheticKotlinFile build(@NotNull PsiFile templateFile, @Nullable Integer completionTemplateOffset) {
        KtePsiJavaContent host = PsiTreeUtil.findChildOfType(templateFile, KtePsiJavaContent.class);
        if (host == null) {
            return new KteSyntheticKotlinFile(syntheticFileName(templateFile), "", List.of());
        }

        Builder builder = new Builder(templateFile, host, completionTemplateOffset);
        return builder.build();
    }

    private static String syntheticFileName(@NotNull PsiFile templateFile) {
        String fileName = templateFile.getName();
        if (fileName.endsWith(".kte")) {
            fileName = fileName.substring(0, fileName.length() - ".kte".length());
        }

        return fileName.replaceAll("[^A-Za-z0-9_.$-]", "_") + ".synthetic.kt";
    }

    private static final class Builder {
        private final PsiFile templateFile;
        private final KtePsiJavaContent host;
        private final StringBuilder text = new StringBuilder();
        private final List<KteSyntheticKotlinRangeMapping> mappings = new ArrayList<>();
        private final Map<String, TemplateStub> templateStubs = new LinkedHashMap<>();
        private final KteSyntheticKotlinTypeRenderer typeRenderer;
        @Nullable
        private final Integer completionTemplateOffset;
        private int forElseCounter;

        private Builder(PsiFile templateFile, KtePsiJavaContent host, @Nullable Integer completionTemplateOffset) {
            this.templateFile = templateFile;
            this.host = host;
            this.completionTemplateOffset = completionTemplateOffset;
            this.typeRenderer = new KteSyntheticKotlinTypeRenderer(templateFile);
        }

        private KteSyntheticKotlinFile build() {
            List<JtePsiParam> params = directChildren(JtePsiParam.class);

            appendImports();
            collectTemplateStubs();
            appendTemplateStubs();

            if (!params.isEmpty() || hasSupportedBody(host)) {
                appendGenerated(CLASS_PREFIX);
                appendParameters(params);
                appendGenerated(") {\n");
                appendTemplateBody();
                appendGenerated(CLASS_SUFFIX);
            }

            return new KteSyntheticKotlinFile(syntheticFileName(templateFile), text.toString(), mappings);
        }

        private void appendImports() {
            for (JtePsiImport importElement : directChildren(JtePsiImport.class)) {
                JtePsiJavaInjection part = PsiTreeUtil.getChildOfType(importElement, JtePsiJavaInjection.class);
                appendMappedPart("import ", "\n", part, KteSyntheticKotlinRangeMapping.Kind.IMPORT);
            }
        }

        private void collectTemplateStubs() {
            for (JtePsiTemplate template : PsiTreeUtil.findChildrenOfType(host, JtePsiTemplate.class)) {
                resolveTemplateStub(template);
            }
        }

        private void appendTemplateStubs() {
            for (TemplateStub stub : templateStubs.values()) {
                appendGenerated("@Suppress(\"unused\", \"UNUSED_PARAMETER\")\n");
                appendGenerated("fun " + stub.functionName() + "(");
                List<KteTemplateSignatureService.Parameter> parameters = stub.signature().parameters();
                for (int index = 0; index < parameters.size(); index++) {
                    appendGenerated(stubParameter(parameters.get(index)));
                    appendGenerated(index + 1 < parameters.size() ? ", " : "");
                }
                appendGenerated(") {}\n");
            }
        }

        @NotNull
        private String stubParameter(@NotNull KteTemplateSignatureService.Parameter parameter) {
            StringBuilder result = new StringBuilder();
            if (parameter.vararg()) {
                result.append("vararg ");
            }
            result.append(parameter.name()).append(": ").append(typeRenderer.renderChildSignatureType(parameter));
            if (parameter.defaulted() && !parameter.vararg()) {
                result.append(" = ").append(generatedDefaultValue(parameter));
            }
            return result.toString();
        }

        @NotNull
        private String generatedDefaultValue(@NotNull KteTemplateSignatureService.Parameter parameter) {
            if (parameter.nullable()) {
                return "null";
            }
            if (parameter.content()) {
                return "object : gg.jte.Content { override fun writeTo() {} }";
            }

            String rawType = parameter.rawType();
            String shortName = rawType.substring(rawType.lastIndexOf('.') + 1);
            return switch (shortName) {
                case "Boolean" -> "false";
                case "Byte", "Short", "Int", "Long", "Float", "Double" -> "0";
                case "String" -> "\"\"";
                case "List", "MutableList", "Iterable", "Collection", "MutableCollection" -> "emptyList()";
                case "Set", "MutableSet" -> "emptySet()";
                case "Map", "MutableMap" -> "emptyMap()";
                default -> "TODO()";
            };
        }

        private void appendParameters(List<JtePsiParam> params) {
            for (int index = 0; index < params.size(); index++) {
                appendParameter(params.get(index));
                String suffix = index + 1 < params.size() ? ", " : "";
                appendGenerated(suffix);
            }
        }

        private void appendParameter(@NotNull JtePsiParam param) {
            JtePsiJavaInjection part = PsiTreeUtil.getChildOfType(param, JtePsiJavaInjection.class);
            if (part == null) {
                return;
            }

            String partText = part.getText();
            int defaultValueOffset = findDefaultValueOffset(partText);
            if (defaultValueOffset == -1) {
                appendMappedPart(null, null, part, KteSyntheticKotlinRangeMapping.Kind.PARAMETER);
                return;
            }

            int declarationEnd = trimEnd(partText, defaultValueOffset);
            appendMappedPartRange(null, null, part, KteSyntheticKotlinRangeMapping.Kind.PARAMETER, 0, declarationEnd);
            appendGenerated(partText.substring(declarationEnd, defaultValueOffset + 1));

            int valueStart = skipWhitespace(partText, defaultValueOffset + 1);
            appendGenerated(partText.substring(defaultValueOffset + 1, valueStart));

            int valueEnd = trimEnd(partText, partText.length());
            appendMappedPartRange(null, null, part, KteSyntheticKotlinRangeMapping.Kind.PARAMETER_DEFAULT_VALUE, valueStart, valueEnd);
            appendGenerated(partText.substring(valueEnd));
        }

        private void appendTemplateBody() {
            for (PsiElement child : host.getChildren()) {
                if (!(child instanceof JtePsiImport) && !(child instanceof JtePsiParam)) {
                    processTemplateBody(child);
                }
            }
        }

        private void processTemplateBody(PsiElement child) {
            if (isAfterCompletionCaret(child)) {
                return;
            }

            if (child instanceof JtePsiOutput output) {
                appendContentAwareKotlinPart("jteOutput.writeUserContent(", ")\n", output, outputKind(output));
            } else if (child instanceof JtePsiStatement) {
                appendContentAwareKotlinPart(null, "\n", child, KteSyntheticKotlinRangeMapping.Kind.STATEMENT);
            } else if (child instanceof JtePsiIf) {
                JtePsiJavaInjection part = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
                appendMappedPart("if (", ") {\n", part, KteSyntheticKotlinRangeMapping.Kind.IF_CONDITION);

                if (part != null) {
                    for (PsiElement sibling = part.getNextSibling(); sibling != null; sibling = sibling.getNextSibling()) {
                        processTemplateBody(sibling);
                    }
                }
            } else if (child instanceof JtePsiElseIf) {
                JtePsiJavaInjection part = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
                appendMappedPart("} else if (", ") {\n", part, KteSyntheticKotlinRangeMapping.Kind.ELSE_IF_CONDITION);
            } else if (child instanceof JtePsiElse) {
                appendGenerated("\n} else {\n");
            } else if (child instanceof JtePsiEndIf) {
                appendGenerated("}\n");
            } else if (child instanceof JtePsiFor forElement) {
                appendFor(forElement);
            } else if (child instanceof JtePsiEndFor) {
                appendGenerated("}\n");
            } else if (child instanceof JtePsiTemplate) {
                appendTemplateCall((JtePsiTemplate) child);
            } else if (child instanceof JtePsiBlock) {
                for (PsiElement element : child.getChildren()) {
                    processTemplateBody(element);
                }
            }
        }

        private boolean isAfterCompletionCaret(@NotNull PsiElement child) {
            return completionTemplateOffset != null &&
                    child.getTextRange().getStartOffset() > completionTemplateOffset &&
                    !(child instanceof JtePsiEndIf) &&
                    !(child instanceof JtePsiEndFor);
        }

        private void appendTemplateCall(@NotNull JtePsiTemplate template) {
            TemplateStub stub = resolveTemplateStub(template);
            if (stub == null) {
                appendContentAwareKotlinPart("dummyCall(", ")\n", template, KteSyntheticKotlinRangeMapping.Kind.TEMPLATE_ARGUMENT_VALUE);
                return;
            }

            appendTemplateCallTarget(template, stub.functionName());
            appendGenerated("(");
            for (PsiElement element : template.getChildren()) {
                if (element instanceof KtePsiParamName paramName) {
                    String name = paramName.getName();
                    if (name != null && !name.isBlank()) {
                        appendMappedGenerated(name, paramName.getTextRange(),
                                KteSyntheticKotlinRangeMapping.Kind.TEMPLATE_ARGUMENT_NAME);
                        appendGenerated(" = ");
                    }
                } else if (element instanceof JtePsiJavaInjection javaInjection) {
                    appendMappedPart(null, null, javaInjection, KteSyntheticKotlinRangeMapping.Kind.TEMPLATE_ARGUMENT_VALUE);
                } else if (element instanceof JtePsiContent content) {
                    appendContent(null, null, content);
                } else if (element instanceof JtePsiComma) {
                    appendGenerated(",");
                }
            }
            appendGenerated(")\n");
        }

        private void appendMappedGenerated(@NotNull String generatedText,
                                           @NotNull TextRange templateRange,
                                           @NotNull KteSyntheticKotlinRangeMapping.Kind kind) {
            int kotlinStartOffset = text.length();
            appendGenerated(generatedText);
            int mappedLength = Math.min(generatedText.length(), templateRange.getLength());
            if (mappedLength > 0) {
                mappings.add(new KteSyntheticKotlinRangeMapping(
                        kind,
                        TextRange.from(templateRange.getStartOffset(), mappedLength),
                        TextRange.from(kotlinStartOffset, mappedLength)
                ));
            }
        }

        private void appendTemplateCallTarget(@NotNull JtePsiTemplate template, @NotNull String functionName) {
            int kotlinStartOffset = text.length();
            appendGenerated(functionName);

            JtePsiTemplateName templateName = JtePsiUtil.getLastChildOfType(template, JtePsiTemplateName.class);
            if (templateName == null || templateName.getTextLength() == 0) {
                return;
            }

            int mappedLength = Math.min(functionName.length(), templateName.getTextLength());
            mappings.add(new KteSyntheticKotlinRangeMapping(
                    KteSyntheticKotlinRangeMapping.Kind.TEMPLATE_CALL,
                    TextRange.from(templateName.getTextRange().getStartOffset(), mappedLength),
                    TextRange.from(kotlinStartOffset, mappedLength)
            ));
        }

        @Nullable
        private TemplateStub resolveTemplateStub(@NotNull JtePsiTemplate template) {
            JtePsiTemplateName templateName = JtePsiUtil.getLastChildOfType(template, JtePsiTemplateName.class);
            if (templateName == null) {
                return null;
            }

            PsiFile targetTemplate = templateName.resolveFile();
            if (targetTemplate == null || !KteTemplateSignatureService.isKteTemplate(targetTemplate)) {
                return null;
            }

            KteTemplateSignatureService.TemplateSignature signature =
                    KteTemplateSignatureService.resolveForSyntheticStub(targetTemplate);
            String key = templateKey(targetTemplate);
            TemplateStub existing = templateStubs.get(key);
            if (existing != null) {
                return existing;
            }

            TemplateStub stub = new TemplateStub(templateFunctionName(key), signature);
            templateStubs.put(key, stub);
            return stub;
        }

        @NotNull
        private String templateKey(@NotNull PsiFile targetTemplate) {
            VirtualFile virtualFile = targetTemplate.getVirtualFile();
            if (virtualFile != null) {
                return virtualFile.getPath();
            }
            return targetTemplate.getName();
        }

        @NotNull
        private String templateFunctionName(@NotNull String key) {
            return "__jte_template_" + key.replaceAll("[^A-Za-z0-9_]", "_");
        }

        private void appendFor(@NotNull JtePsiFor forElement) {
            JtePsiJavaInjection part = PsiTreeUtil.getChildOfType(forElement, JtePsiJavaInjection.class);
            if (part == null) {
                return;
            }

            ForBody body = forBody(part);
            if (body.elseElements().isEmpty()) {
                appendMappedPart("for (", ") {\n", part, KteSyntheticKotlinRangeMapping.Kind.FOR_CONDITION);
                processTemplateBody(body.loopElements());
                appendGenerated("}\n");
                return;
            }

            String emptyFlagName = "__jteForElse" + forElseCounter++;
            appendGenerated("var " + emptyFlagName + " = true\n");
            appendMappedPart("for (", ") {\n", part, KteSyntheticKotlinRangeMapping.Kind.FOR_CONDITION);
            appendGenerated(emptyFlagName + " = false\n");
            processTemplateBody(body.loopElements());
            appendGenerated("}\nif (" + emptyFlagName + ") {\n");
            processTemplateBody(body.elseElements());
            appendGenerated("}\n");
        }

        private ForBody forBody(@NotNull JtePsiJavaInjection part) {
            List<PsiElement> loopElements = new ArrayList<>();
            List<PsiElement> elseElements = new ArrayList<>();
            boolean inElse = false;
            for (PsiElement sibling = part.getNextSibling(); sibling != null; sibling = sibling.getNextSibling()) {
                if (sibling instanceof JtePsiEndFor) {
                    break;
                }
                if (sibling instanceof JtePsiElse) {
                    inElse = true;
                    continue;
                }

                if (inElse) {
                    elseElements.add(sibling);
                } else {
                    loopElements.add(sibling);
                }
            }

            return new ForBody(loopElements, elseElements);
        }

        private void processTemplateBody(@NotNull List<PsiElement> elements) {
            for (PsiElement element : elements) {
                processTemplateBody(element);
            }
        }

        private void appendContentAwareKotlinPart(String prefix, String suffix, PsiElement child, KteSyntheticKotlinRangeMapping.Kind kind) {
            List<PsiElement> children = kotlinArgumentChildren(child);
            if (children.isEmpty()) {
                appendGenerated((prefix == null ? "" : prefix) + (suffix == null ? "" : suffix));
                return;
            }

            boolean prefixWritten = false;
            PsiElement last = children.isEmpty() ? null : children.get(children.size() - 1);
            for (PsiElement element : children) {
                String currentPrefix = null;
                if (!prefixWritten) {
                    currentPrefix = prefix;
                    prefixWritten = true;
                }

                String currentSuffix = null;
                if (element == last) {
                    currentSuffix = suffix;
                } else if (hasCommaAfter(element) && !hasCompletionRecoveryCommaAfter(element)) {
                    currentSuffix = ",";
                }

                if (element instanceof JtePsiJavaInjection javaInjection) {
                    if (kind == KteSyntheticKotlinRangeMapping.Kind.STATEMENT) {
                        appendTrimmedMappedPart(currentPrefix, currentSuffix, javaInjection, kind);
                    } else {
                        appendMappedPart(currentPrefix, currentSuffix, javaInjection, kind);
                    }
                } else if (element instanceof JtePsiContent content) {
                    appendContent(currentPrefix, currentSuffix, content);
                }
            }
        }

        @NotNull
        private List<PsiElement> kotlinArgumentChildren(@NotNull PsiElement child) {
            List<PsiElement> result = new ArrayList<>();
            for (PsiElement element : child.getChildren()) {
                if (element instanceof JtePsiJavaInjection || element instanceof JtePsiContent) {
                    result.add(element);
                }
            }

            return result;
        }

        private boolean hasCommaAfter(@NotNull PsiElement element) {
            for (PsiElement sibling = element.getNextSibling(); sibling != null; sibling = sibling.getNextSibling()) {
                if (sibling instanceof JtePsiComma) {
                    return true;
                }
                if (sibling instanceof JtePsiJavaInjection ||
                        sibling instanceof JtePsiContent ||
                        sibling instanceof JtePsiParamName) {
                    return false;
                }
            }

            return false;
        }

        private void appendContent(String prefix, String suffix, JtePsiContent element) {
            appendGenerated((prefix == null ? "" : prefix) + "object : gg.jte.Content { override fun writeTo() {");

            JtePsiBlock block = PsiTreeUtil.getChildOfType(element, JtePsiBlock.class);
            if (block != null) {
                for (PsiElement child : block.getChildren()) {
                    processTemplateBody(child);
                }
            }

            appendGenerated("}}" + (suffix == null ? "" : suffix));
        }

        private void appendMappedPart(String prefix, String suffix, JtePsiJavaInjection part, KteSyntheticKotlinRangeMapping.Kind kind) {
            if (part == null) {
                return;
            }

            appendMappedPartRange(prefix, suffix, part, kind, 0, part.getTextLength());
        }

        private void appendTrimmedMappedPart(String prefix, String suffix, JtePsiJavaInjection part, KteSyntheticKotlinRangeMapping.Kind kind) {
            String partText = part.getText();
            int startOffset = skipWhitespace(partText, 0);
            int endOffset = trimEnd(partText, partText.length());
            appendMappedPartRange(prefix, suffix, part, kind, startOffset, endOffset);
        }

        private void appendMappedPartRange(String prefix,
                                           String suffix,
                                           JtePsiJavaInjection part,
                                           KteSyntheticKotlinRangeMapping.Kind kind,
                                           int partStartOffset,
                                           int partEndOffset) {
            if (partStartOffset >= partEndOffset) {
                return;
            }

            appendGenerated(prefix);
            int kotlinStartOffset = text.length();
            String mappedText = normalizedMappedText(part.getText(), partStartOffset, partEndOffset);
            if (mappedText.isEmpty()) {
                return;
            }

            text.append(mappedText);
            TextRange kotlinRange = new TextRange(kotlinStartOffset, text.length());
            TextRange templateRange = new TextRange(
                    part.getTextRange().getStartOffset() + partStartOffset,
                    part.getTextRange().getStartOffset() + partStartOffset + mappedText.length()
            );
            mappings.add(new KteSyntheticKotlinRangeMapping(kind, templateRange, kotlinRange));
            appendGenerated(suffix);
        }

        @NotNull
        private String normalizedMappedText(@NotNull String partText, int partStartOffset, int partEndOffset) {
            String mappedText = partText.substring(partStartOffset, partEndOffset);
            int dummyOffset = mappedText.indexOf(COMPLETION_DUMMY_IDENTIFIER);
            if (dummyOffset < 0) {
                return mappedText;
            }

            int dummyEndOffset = dummyOffset + COMPLETION_DUMMY_IDENTIFIER.length();
            return mappedText.substring(0, Math.min(dummyEndOffset, mappedText.length()));
        }

        private boolean hasCompletionRecoveryCommaAfter(@NotNull PsiElement element) {
            if (!element.getText().contains(COMPLETION_DUMMY_IDENTIFIER) ||
                    PsiTreeUtil.getParentOfType(element, JtePsiOutput.class, false) == null) {
                return false;
            }

            PsiElement sibling = nextNonWhitespace(element.getNextSibling());
            if (!(sibling instanceof JtePsiComma)) {
                return false;
            }

            return nextNonWhitespace(sibling.getNextSibling()) instanceof JtePsiOutputEnd;
        }

        @Nullable
        private PsiElement nextNonWhitespace(@Nullable PsiElement element) {
            PsiElement current = element;
            while (current instanceof PsiWhiteSpace) {
                current = current.getNextSibling();
            }
            return current;
        }

        private void appendGenerated(String generatedText) {
            if (generatedText != null) {
                text.append(generatedText);
            }
        }

        private boolean hasSupportedBody(PsiElement element) {
            for (PsiElement child : element.getChildren()) {
                if (child instanceof JtePsiOutput ||
                        child instanceof JtePsiStatement ||
                        child instanceof JtePsiIf ||
                        child instanceof JtePsiElseIf ||
                        child instanceof JtePsiElse ||
                        child instanceof JtePsiEndIf ||
                        child instanceof JtePsiFor ||
                        child instanceof JtePsiEndFor ||
                        child instanceof JtePsiTemplate) {
                    return true;
                }

                if (child instanceof JtePsiBlock && hasSupportedBody(child)) {
                    return true;
                }
            }

            return false;
        }

        private KteSyntheticKotlinRangeMapping.Kind outputKind(@NotNull JtePsiOutput output) {
            JtePsiOutputBegin begin = PsiTreeUtil.getChildOfType(output, JtePsiOutputBegin.class);
            if (begin != null && begin.getText().startsWith("$unsafe")) {
                return KteSyntheticKotlinRangeMapping.Kind.UNSAFE_OUTPUT_EXPRESSION;
            }

            return KteSyntheticKotlinRangeMapping.Kind.OUTPUT_EXPRESSION;
        }

        private int findDefaultValueOffset(@NotNull String value) {
            boolean insideString = false;
            int angleDepth = 0;
            int parenthesisDepth = 0;
            for (int index = 0; index < value.length(); index++) {
                char current = value.charAt(index);
                if (current == '"' && (index == 0 || value.charAt(index - 1) != '\\')) {
                    insideString = !insideString;
                }
                if (insideString) {
                    continue;
                }
                if (current == '<') {
                    angleDepth++;
                } else if (current == '>' && angleDepth > 0) {
                    angleDepth--;
                } else if (current == '(') {
                    parenthesisDepth++;
                } else if (current == ')' && parenthesisDepth > 0) {
                    parenthesisDepth--;
                } else if (current == '=' && angleDepth == 0 && parenthesisDepth == 0) {
                    return index;
                }
            }

            return -1;
        }

        private int skipWhitespace(@NotNull String value, int offset) {
            int result = offset;
            while (result < value.length() && Character.isWhitespace(value.charAt(result))) {
                result++;
            }

            return result;
        }

        private int trimEnd(@NotNull String value, int endOffset) {
            int result = endOffset;
            while (result > 0 && Character.isWhitespace(value.charAt(result - 1))) {
                result--;
            }

            return result;
        }

        private <T extends PsiElement> List<T> directChildren(Class<T> childType) {
            List<T> result = new ArrayList<>();
            for (PsiElement child : host.getChildren()) {
                if (childType.isInstance(child)) {
                    result.add(childType.cast(child));
                }
            }

            return result;
        }

        private record ForBody(@NotNull List<PsiElement> loopElements, @NotNull List<PsiElement> elseElements) {
        }

        private record TemplateStub(@NotNull String functionName,
                                    @NotNull KteTemplateSignatureService.TemplateSignature signature) {
        }
    }
}
