package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.template.KteKotlinTypeText;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class KteSyntheticKotlinTypeRenderer {
    private static final Pattern TYPE_REFERENCE = Pattern.compile("[A-Za-z_][A-Za-z0-9_.]*");
    private static final Set<String> KNOWN_TYPE_NAMES = Set.of(
            "Any", "Array", "Boolean", "Byte", "Char", "Collection", "Double", "Float", "Int", "Iterable",
            "List", "Long", "Map", "MutableList", "MutableMap", "MutableSet", "Nothing", "Sequence", "Set",
            "Short", "String", "Unit"
    );
    private static final Set<String> TYPE_KEYWORDS = Set.of("in", "out", "where");

    private final Project project;
    private final GlobalSearchScope scope;
    private final Imports parentImports;

    KteSyntheticKotlinTypeRenderer(@NotNull PsiFile parentTemplateFile) {
        this.project = parentTemplateFile.getProject();
        this.scope = KteSyntheticKotlinAnalysisContextService.getInstance(project).resolveSearchScope(parentTemplateFile);
        this.parentImports = Imports.from(parentTemplateFile);
    }

    @NotNull
    String renderChildSignatureType(@NotNull KteTemplateSignatureService.Parameter parameter) {
        PsiFile childTemplateFile = parameter.sourceElement().getContainingFile();
        Imports childImports = childTemplateFile == null ? Imports.EMPTY : Imports.from(childTemplateFile);
        return renderTypeText(parameter.typeText(), childImports);
    }

    @NotNull
    private String renderTypeText(@NotNull String typeText, @NotNull Imports childImports) {
        Matcher matcher = TYPE_REFERENCE.matcher(typeText);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(renderTypeReference(matcher.group(), childImports)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    @NotNull
    private String renderTypeReference(@NotNull String typeName, @NotNull Imports childImports) {
        if (typeName.contains(".") || TYPE_KEYWORDS.contains(typeName) || isKnownTypeName(typeName)) {
            return typeName;
        }

        if (parentImports.resolves(typeName, this::classExists)) {
            return typeName;
        }

        String childQualifiedName = childImports.qualifiedName(typeName, this::classExists);
        if (childQualifiedName != null) {
            return childQualifiedName;
        }

        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(typeName, scope);
        String qualifiedName = psiClass == null ? null : psiClass.getQualifiedName();
        return qualifiedName == null ? typeName : qualifiedName;
    }

    private boolean classExists(@NotNull String qualifiedName) {
        return JavaPsiFacade.getInstance(project).findClass(qualifiedName, scope) != null;
    }

    private boolean isKnownTypeName(@NotNull String typeName) {
        return KNOWN_TYPE_NAMES.contains(KteKotlinTypeText.shortName(typeName));
    }

    private interface ClassLookup {
        boolean exists(@NotNull String qualifiedName);
    }

    private record Imports(@NotNull List<KteKotlinImportResolver.ImportInfo> imports) {
        private static final Imports EMPTY = new Imports(List.of());

        @NotNull
        private static Imports from(@NotNull PsiFile file) {
            List<KteKotlinImportResolver.ImportInfo> result = new KteKotlinImportResolver(file).imports();
            return result.isEmpty() ? EMPTY : new Imports(result);
        }

        private boolean resolves(@NotNull String visibleName, @NotNull ClassLookup classLookup) {
            return qualifiedName(visibleName, classLookup) != null;
        }

        @Nullable
        private String qualifiedName(@NotNull String visibleName, @NotNull ClassLookup classLookup) {
            for (KteKotlinImportResolver.ImportInfo importInfo : imports) {
                if (!importInfo.star() && importInfo.visibleName().equals(visibleName)) {
                    return importInfo.qualifiedName();
                }
            }

            for (KteKotlinImportResolver.ImportInfo importInfo : imports) {
                if (importInfo.star()) {
                    String qualifiedName = importInfo.packageName() + "." + visibleName;
                    if (classLookup.exists(qualifiedName)) {
                        return qualifiedName;
                    }
                }
            }

            return null;
        }
    }
}
