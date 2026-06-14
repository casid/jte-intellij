package org.jusecase.jte.intellij.language.k2;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.psi.JtePsiTemplate;
import org.jusecase.jte.intellij.language.psi.JtePsiTemplateName;
import org.jusecase.jte.intellij.language.psi.JtePsiUtil;
import org.jusecase.jte.intellij.language.refactoring.KteAddMissingTemplateArgumentsIntention;
import org.jusecase.jte.intellij.language.refactoring.KteInsertMissingAssignmentIntention;
import org.jusecase.jte.intellij.language.refactoring.KteRemoveDuplicateTemplateArgumentIntention;
import org.jusecase.jte.intellij.language.template.KteTemplateCallArguments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class KteTemplateContractDiagnosticChecker {
    @NotNull
    List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> collect(@NotNull PsiFile templateFile) {
        List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (JtePsiTemplate template : PsiTreeUtil.findChildrenOfType(templateFile, JtePsiTemplate.class)) {
            JtePsiTemplateName templateName = JtePsiUtil.getLastChildOfType(template, JtePsiTemplateName.class);
            if (templateName == null) {
                continue;
            }

            PsiFile targetTemplate = templateName.resolveFile();
            if (targetTemplate == null) {
                continue;
            }

            KteTemplateSignatureService.TemplateSignature signature = KteTemplateSignatureService.resolve(targetTemplate);
            collectTemplateCallDiagnostics(template, signature, result, seen);
        }

        return result;
    }

    private void collectTemplateCallDiagnostics(@NotNull JtePsiTemplate template,
                                                @NotNull KteTemplateSignatureService.TemplateSignature signature,
                                                @NotNull List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> result,
                                                @NotNull Set<String> seen) {
        Map<String, KteTemplateCallArguments.Argument> namedArguments = new LinkedHashMap<>();
        List<KteTemplateCallArguments.Argument> arguments = KteTemplateCallArguments.parse(template);
        for (KteTemplateCallArguments.Argument argument : arguments) {
            String argumentName = argument.name();
            if (argumentName != null && !argument.hasAssignment()) {
                add(result, seen,
                        "Missing parameter assignment",
                        argument.range(),
                        List.of(new KteInsertMissingAssignmentIntention(
                                argument.range(),
                                argument.argumentRange(),
                                argumentName)));
            }

            if (argumentName != null) {
                if (namedArguments.putIfAbsent(argumentName, argument) != null) {
                    add(result, seen,
                            "Duplicate parameter " + argumentName,
                            argument.range(),
                            List.of(new KteRemoveDuplicateTemplateArgumentIntention(argument.argumentRange())));
                }
            }
        }

        List<String> missingRequiredNames = missingRequiredNames(signature, arguments);
        if (!missingRequiredNames.isEmpty()) {
            add(result, seen,
                    "Missing required parameters: " + String.join(", ", missingRequiredNames),
                    template.getTextRange(),
                    List.of(new KteAddMissingTemplateArgumentsIntention(template.getTextRange(), missingRequiredNames)));
        }
    }

    @NotNull
    private List<String> missingRequiredNames(@NotNull KteTemplateSignatureService.TemplateSignature signature,
                                              @NotNull List<KteTemplateCallArguments.Argument> arguments) {
        Set<String> usedNames = new HashSet<>();
        int positionalIndex = 0;
        List<KteTemplateSignatureService.Parameter> parameters = signature.parameters();
        for (KteTemplateCallArguments.Argument argument : arguments) {
            if (argument.positional()) {
                if (positionalIndex < parameters.size()) {
                    usedNames.add(parameters.get(positionalIndex).name());
                }
                positionalIndex++;
            } else if (argument.name() != null) {
                usedNames.add(argument.name());
            }
        }

        return signature.requiredParameters()
                .stream()
                .map(KteTemplateSignatureService.Parameter::name)
                .filter(name -> !usedNames.contains(name))
                .toList();
    }

    private void add(@NotNull List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> result,
                     @NotNull Set<String> seen,
                     @NotNull String message,
                     @NotNull TextRange range,
                     @NotNull List<IntentionAction> fixes) {
        HighlightSeverity severity = HighlightSeverity.ERROR;
        String key = severity + ":" + message + ":" + range;
        if (seen.add(key)) {
            result.add(new KteSyntheticKotlinDiagnosticCollector.Diagnostic(
                    severity,
                    message,
                    range,
                    KteSyntheticKotlinDiagnosticCollector.Origin.TEMPLATE_STRUCTURE,
                    fixes
            ));
        }
    }

}
