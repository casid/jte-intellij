package org.jusecase.jte.intellij.language.k2;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jusecase.jte.intellij.language.psi.JtePsiFor;
import org.jusecase.jte.intellij.language.psi.JtePsiJavaInjection;
import org.jusecase.jte.intellij.language.psi.JtePsiParam;
import org.jusecase.jte.intellij.language.psi.JtePsiTemplate;
import org.jusecase.jte.intellij.language.psi.JtePsiTemplateName;

import java.util.Collection;

public final class KteSyntheticKotlinDocumentationProvider extends AbstractDocumentationProvider {
    @Override
    public @Nullable PsiElement getCustomDocumentationElement(Editor editor,
                                                             PsiFile file,
                                                             @Nullable PsiElement contextElement,
                                                             int targetOffset) {
        PsiElement declarationAtOffset = resolveDeclarationAtOffset(file, targetOffset);
        if (declarationAtOffset != null) {
            return declarationAtOffset;
        }

        if (isForLoopVariableDeclarationAtOffset(file, targetOffset)) {
            return null;
        }

        return resolveReferenceNearOffset(file, targetOffset);
    }

    @Override
    public @Nullable String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        String elementDeclaration = templateParamText(element);
        if (elementDeclaration != null) {
            return elementDeclaration;
        }

        PsiElement target = element == originalElement ? resolveTarget(originalElement) : element;

        String templateSignature = templateSignatureText(target, originalElement);
        if (templateSignature != null) {
            return templateSignature;
        }

        return templateParamText(target);
    }

    @Override
    public @Nullable String generateDoc(PsiElement element, PsiElement originalElement) {
        String quickInfo = getQuickNavigateInfo(element, originalElement);
        if (quickInfo == null) {
            return null;
        }

        return "<pre>" + highlightedKotlinSnippet(element, quickInfo) + "</pre>";
    }

    @NotNull
    private String highlightedKotlinSnippet(@NotNull PsiElement element, @NotNull String text) {
        StringBuilder builder = new StringBuilder();
        HtmlSyntaxInfoUtil.appendHighlightedByLexerAndEncodedAsHtmlCodeSnippet(
                builder,
                element.getProject(),
                KotlinLanguage.INSTANCE,
                text,
                false,
                1.0f
        );
        if (builder.isEmpty()) {
            return StringUtil.escapeXmlEntities(text);
        }
        return builder.toString();
    }

    @Nullable
    private String templateSignatureText(@Nullable PsiElement target, @Nullable PsiElement originalElement) {
        if (!(target instanceof PsiFile templateFile) || !KteTemplateSignatureService.isKteTemplate(templateFile)) {
            return null;
        }

        KteTemplateSignatureService.TemplateSignature signature = KteTemplateSignatureService.resolve(templateFile);
        String templateName = templateName(templateFile, originalElement);
        StringBuilder builder = new StringBuilder("template ").append(templateName).append('(');
        if (signature.parameters().isEmpty()) {
            return builder.append(')').toString();
        }

        builder.append('\n');
        for (KteTemplateSignatureService.Parameter parameter : signature.parameters()) {
            builder.append("    ")
                    .append(parameterText(parameter))
                    .append(",\n");
        }
        builder.setLength(builder.length() - 2);
        builder.append('\n').append(')');
        return builder.toString();
    }

    @NotNull
    private String parameterText(@NotNull KteTemplateSignatureService.Parameter parameter) {
        StringBuilder builder = new StringBuilder();
        if (parameter.vararg()) {
            builder.append("vararg ");
        }
        builder.append(parameter.name()).append(": ").append(parameter.renderedTypeText());
        if (parameter.defaulted()) {
            builder.append(" = ").append(defaultValueText(parameter));
        }
        return builder.toString();
    }

    @NotNull
    private String defaultValueText(@NotNull KteTemplateSignatureService.Parameter parameter) {
        TextRange defaultValueRange = parameter.defaultValueRange();
        PsiFile file = parameter.sourceElement().getContainingFile();
        if (defaultValueRange == null || file == null || defaultValueRange.getEndOffset() > file.getTextLength()) {
            return "...";
        }

        String text = defaultValueRange.substring(file.getText()).trim();
        return text.isEmpty() ? "..." : text;
    }

    @NotNull
    private String templateName(@NotNull PsiFile templateFile, @Nullable PsiElement originalElement) {
        String callName = templateCallName(originalElement);
        if (callName != null) {
            return callName;
        }

        String rootRelativeName = rootRelativeTemplateName(templateFile);
        if (rootRelativeName != null) {
            return rootRelativeName;
        }

        String fileName = templateFile.getName();
        int extensionOffset = fileName.lastIndexOf('.');
        return extensionOffset == -1 ? fileName : fileName.substring(0, extensionOffset);
    }

    @Nullable
    private String templateCallName(@Nullable PsiElement originalElement) {
        JtePsiTemplate template = PsiTreeUtil.getParentOfType(originalElement, JtePsiTemplate.class, false);
        if (template == null) {
            return null;
        }

        Collection<JtePsiTemplateName> names = PsiTreeUtil.findChildrenOfType(template, JtePsiTemplateName.class);
        if (names.isEmpty()) {
            return null;
        }

        return names.stream()
                .map(JtePsiTemplateName::getName)
                .filter(name -> name != null && !name.isBlank())
                .reduce((left, right) -> left + "." + right)
                .orElse(null);
    }

    @Nullable
    private String rootRelativeTemplateName(@NotNull PsiFile templateFile) {
        PsiDirectory rootDirectory = templateRoot(templateFile.getParent());
        VirtualFile templateVirtualFile = templateFile.getVirtualFile();
        if (rootDirectory == null || templateVirtualFile == null) {
            return null;
        }

        String relativePath = VfsUtilCore.getRelativePath(
                templateVirtualFile,
                rootDirectory.getVirtualFile(),
                '/'
        );
        if (relativePath == null) {
            return null;
        }

        int extensionOffset = relativePath.lastIndexOf('.');
        String templatePath = extensionOffset == -1 ? relativePath : relativePath.substring(0, extensionOffset);
        return templatePath.replace('/', '.');
    }

    @Nullable
    private PsiDirectory templateRoot(@Nullable PsiDirectory directory) {
        PsiDirectory current = directory;
        while (current != null) {
            if (current.findFile(JtePsiTemplateName.JTE_ROOT) != null) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }

    @Nullable
    private PsiElement resolveTarget(@Nullable PsiElement originalElement) {
        if (originalElement == null) {
            return null;
        }

        PsiReference referenceAtElement = originalElement.getContainingFile()
                .findReferenceAt(originalElement.getTextRange().getStartOffset() + originalElement.getTextLength() / 2);
        if (referenceAtElement != null) {
            PsiElement resolved = referenceAtElement.resolve();
            if (resolved != null) {
                return resolved;
            }
        }

        for (PsiReference reference : originalElement.getReferences()) {
            PsiElement resolved = reference.resolve();
            if (resolved != null) {
                return resolved;
            }
        }

        JtePsiJavaInjection injection = PsiTreeUtil.getParentOfType(originalElement, JtePsiJavaInjection.class, false);
        if (injection == null && originalElement instanceof JtePsiJavaInjection javaInjection) {
            injection = javaInjection;
        }

        if (injection == null) {
            return null;
        }

        for (PsiReference reference : injection.getReferences()) {
            PsiElement resolved = reference.resolve();
            if (resolved != null) {
                return resolved;
            }
        }

        return null;
    }

    @Nullable
    private PsiElement resolveDeclarationAtOffset(@NotNull PsiFile file, int targetOffset) {
        JtePsiJavaInjection injection = injectionAtOffset(file, targetOffset);
        if (injection == null) {
            return null;
        }

        return PsiTreeUtil.getParentOfType(injection, JtePsiParam.class, false) != null &&
                paramNameContainsOffset(injection, targetOffset)
                ? injection
                : null;
    }

    private boolean paramNameContainsOffset(@NotNull JtePsiJavaInjection injection, int targetOffset) {
        KteTemplateSignatureService.TemplateSignature signature =
                KteTemplateSignatureService.resolve(injection.getContainingFile());
        JtePsiParam param = PsiTreeUtil.getParentOfType(injection, JtePsiParam.class, false);
        for (KteTemplateSignatureService.Parameter parameter : signature.parameters()) {
            if (parameter.sourceElement() == param && parameter.nameRange().contains(targetOffset)) {
                return true;
            }
        }

        return false;
    }

    private boolean isForLoopVariableDeclarationAtOffset(@NotNull PsiFile file, int targetOffset) {
        JtePsiJavaInjection injection = injectionAtOffset(file, targetOffset);
        if (injection == null || PsiTreeUtil.getParentOfType(injection, JtePsiFor.class, false) == null) {
            return false;
        }

        String text = injection.getText();
        int inOffset = text.indexOf(" in ");
        if (inOffset <= 0) {
            return false;
        }

        String variableName = text.substring(0, inOffset).trim();
        if (!isIdentifier(variableName)) {
            return false;
        }

        int variableStart = text.indexOf(variableName);
        int relativeOffset = targetOffset - injection.getTextRange().getStartOffset();
        return relativeOffset >= variableStart && relativeOffset < variableStart + variableName.length();
    }

    private boolean isIdentifier(@NotNull String text) {
        if (text.isEmpty() || !Character.isJavaIdentifierStart(text.charAt(0))) {
            return false;
        }

        for (int i = 1; i < text.length(); i++) {
            if (!Character.isJavaIdentifierPart(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    private PsiElement resolveReferenceNearOffset(@NotNull PsiFile file, int targetOffset) {
        int[] offsets = {targetOffset, targetOffset + 1, targetOffset - 1};
        KteSyntheticKotlinSemanticService semanticService =
                KteSyntheticKotlinSemanticService.getInstance(file.getProject());
        for (int offset : offsets) {
            if (offset < 0 || offset >= file.getTextLength()) {
                continue;
            }

            PsiReference reference = file.findReferenceAt(offset);
            if (reference != null) {
                PsiElement resolved = reference.resolve();
                if (resolved != null) {
                    return resolved;
                }
            }

            PsiElement resolved = semanticService.resolveReferenceAtTemplateOffset(file, offset);
            if (resolved != null) {
                return resolved;
            }
        }

        return null;
    }

    @Nullable
    private JtePsiJavaInjection injectionAtOffset(@NotNull PsiFile file, int targetOffset) {
        for (JtePsiJavaInjection candidate : PsiTreeUtil.findChildrenOfType(file, JtePsiJavaInjection.class)) {
            if (candidate.getTextRange().contains(targetOffset)) {
                return candidate;
            }
        }
        return null;
    }

    @Nullable
    private String templateParamText(@Nullable PsiElement target) {
        if (target == null) {
            return null;
        }

        JtePsiParam param = PsiTreeUtil.getParentOfType(target, JtePsiParam.class, false);
        if (param != null) {
            KteTemplateSignatureService.TemplateSignature signature =
                    KteTemplateSignatureService.resolve(target.getContainingFile());
            for (KteTemplateSignatureService.Parameter parameter : signature.parameters()) {
                if (parameter.sourceElement() == param) {
                    return parameter.name() + ": " + parameter.renderedTypeText();
                }
            }
        }

        return null;
    }
}
