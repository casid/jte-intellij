package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.template.KteTemplateSignatureSource;

import java.util.ArrayList;
import java.util.List;

public final class KteTemplateSignatureService {
    private static final ThreadLocal<Boolean> SEMANTIC_METADATA_DISABLED =
            ThreadLocal.withInitial(() -> false);

    private KteTemplateSignatureService() {
    }

    @NotNull
    public static TemplateSignature resolve(@NotNull PsiFile templateFile) {
        PsiFile kteFile = KteTemplateSignatureSource.resolveKteFile(templateFile);
        if (kteFile == null) {
            return TemplateSignature.EMPTY;
        }

        return CachedValuesManager.getCachedValue(kteFile, () ->
                CachedValueProvider.Result.create(resolveUncached(kteFile), PsiModificationTracker.MODIFICATION_COUNT)
        );
    }

    public static boolean isKteTemplate(@NotNull PsiFile templateFile) {
        return KteTemplateSignatureSource.isKteTemplate(templateFile);
    }

    @NotNull
    static TemplateSignature resolveForSyntheticStub(@NotNull PsiFile templateFile) {
        PsiFile kteFile = KteTemplateSignatureSource.resolveKteFile(templateFile);
        if (kteFile == null) {
            return TemplateSignature.EMPTY;
        }

        boolean previous = SEMANTIC_METADATA_DISABLED.get();
        SEMANTIC_METADATA_DISABLED.set(true);
        try {
            return resolveUncached(kteFile);
        } finally {
            SEMANTIC_METADATA_DISABLED.set(previous);
        }
    }

    @NotNull
    private static TemplateSignature resolveUncached(@NotNull PsiFile templateFile) {
        KteTemplateSignatureSource.Signature sourceSignature = KteTemplateSignatureSource.resolve(templateFile);
        KteKotlinImportResolver importResolver = new KteKotlinImportResolver(templateFile);
        List<Parameter> parameters = new ArrayList<>();
        for (KteTemplateSignatureSource.Parameter sourceParameter : sourceSignature.parameters()) {
            parameters.add(enrichParameter(sourceParameter, importResolver));
        }

        return new TemplateSignature(templateFile, parameters);
    }

    @NotNull
    private static Parameter enrichParameter(@NotNull KteTemplateSignatureSource.Parameter sourceParameter,
                                             @NotNull KteKotlinImportResolver importResolver) {
        PsiClass typeClass = importResolver.resolveClass(sourceParameter.rawType());
        SemanticParameterType semanticParameterType = semanticParameterType(
                sourceParameter.sourceElement(),
                sourceParameter.typeText(),
                sourceParameter.rawType(),
                importResolver,
                sourceParameter.typeRange()
        );

        return new Parameter(
                sourceParameter.name(),
                sourceParameter.typeText(),
                sourceParameter.rawType(),
                sourceParameter.genericArguments(),
                sourceParameter.vararg(),
                sourceParameter.required(),
                sourceParameter.defaulted(),
                sourceParameter.nullable(),
                sourceParameter.content(),
                typeClass,
                semanticParameterType.typeText(),
                semanticParameterType.qualifiedTypeText(),
                semanticParameterType.typeElement(),
                sourceParameter.sourceElement(),
                sourceParameter.declarationRange(),
                sourceParameter.nameRange(),
                sourceParameter.typeRange(),
                sourceParameter.defaultValueRange()
        );
    }

    @NotNull
    private static SemanticParameterType semanticParameterType(@NotNull PsiElement sourceElement,
                                                              @NotNull String sourceTypeText,
                                                              @NotNull String rawType,
                                                              @NotNull KteKotlinImportResolver importResolver,
                                                              @NotNull TextRange typeRange) {
        if (SEMANTIC_METADATA_DISABLED.get() || ApplicationManager.getApplication().isWriteAccessAllowed()) {
            return SemanticParameterType.EMPTY;
        }

        KteSyntheticKotlinSemanticService semanticService =
                KteSyntheticKotlinSemanticService.getInstance(sourceElement.getProject());
        PsiElement typeElement = importResolver.resolveClass(rawType);
        if (typeElement == null) {
            typeElement = semanticService.resolveReferenceAtTemplateOffset(
                    sourceElement.getContainingFile(),
                    typeRange.getStartOffset()
            );
        }

        KteSyntheticKotlinSemanticService.SemanticType semanticType =
                semanticService.declarationTypeAtTemplateRange(sourceElement.getContainingFile(), typeRange);
        if (!isUsableSemanticType(sourceTypeText, typeElement, semanticType)) {
            semanticType = null;
        }

        return new SemanticParameterType(
                semanticType == null ? null : semanticType.typeText(),
                semanticType == null ? null : semanticType.qualifiedTypeText(),
                typeElement
        );
    }

    private static boolean isUsableSemanticType(@NotNull String sourceTypeText,
                                                @Nullable PsiElement typeElement,
                                                @Nullable KteSyntheticKotlinSemanticService.SemanticType semanticType) {
        if (semanticType == null) {
            return false;
        }

        return typeElement != null ||
                !sourceTypeText.equals(semanticType.typeText()) ||
                !sourceTypeText.equals(semanticType.qualifiedTypeText());
    }

    public record TemplateSignature(@Nullable PsiFile templateFile,
                                    @NotNull List<Parameter> parameters) {
        public static final TemplateSignature EMPTY = new TemplateSignature(null, List.of());

        public TemplateSignature {
            parameters = List.copyOf(parameters);
        }

        @Nullable
        public Parameter parameter(@NotNull String name) {
            for (Parameter parameter : parameters) {
                if (name.equals(parameter.name())) {
                    return parameter;
                }
            }
            return null;
        }

        @NotNull
        public List<Parameter> requiredParameters() {
            return parameters.stream()
                    .filter(Parameter::required)
                    .toList();
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
                            @Nullable PsiClass typeClass,
                            @Nullable String semanticTypeText,
                            @Nullable String semanticQualifiedTypeText,
                            @Nullable PsiElement typeElement,
                            @NotNull PsiElement sourceElement,
                            @NotNull TextRange declarationRange,
                            @NotNull TextRange nameRange,
                            @NotNull TextRange typeRange,
                            @Nullable TextRange defaultValueRange) {
        public Parameter {
            genericArguments = List.copyOf(genericArguments);
        }

        @NotNull
        public String renderedTypeText() {
            if (vararg) {
                return typeText;
            }
            return semanticTypeText == null ? typeText : semanticTypeText;
        }
    }

    private record SemanticParameterType(@Nullable String typeText,
                                         @Nullable String qualifiedTypeText,
                                         @Nullable PsiElement typeElement) {
        private static final SemanticParameterType EMPTY = new SemanticParameterType(null, null, null);
    }
}
