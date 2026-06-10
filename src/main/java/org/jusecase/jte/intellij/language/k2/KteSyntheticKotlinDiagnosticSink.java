package org.jusecase.jte.intellij.language.k2;

import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

final class KteSyntheticKotlinDiagnosticSink {
    private final List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics = new ArrayList<>();

    void add(@NotNull HighlightSeverity severity, @NotNull String message, @NotNull TextRange range) {
        add(new KteSyntheticKotlinDiagnosticCollector.Diagnostic(severity, message, range));
    }

    void add(@NotNull KteSyntheticKotlinDiagnosticCollector.Diagnostic diagnostic) {
        if (diagnostic.templateRange().isEmpty()) {
            return;
        }

        String category = category(diagnostic.message());
        for (int index = 0; index < diagnostics.size(); index++) {
            KteSyntheticKotlinDiagnosticCollector.Diagnostic existing = diagnostics.get(index);
            if (isDuplicate(existing, diagnostic, category(existing.message()), category)) {
                int existingPriority = priority(existing.origin());
                int candidatePriority = priority(diagnostic.origin());
                if (existingPriority > candidatePriority) {
                    return;
                }
                if (candidatePriority > existingPriority) {
                    diagnostics.set(index, diagnostic);
                    return;
                }
                if (isMoreSpecific(diagnostic.templateRange(), existing.templateRange())) {
                    diagnostics.set(index, diagnostic);
                }
                return;
            }
        }

        diagnostics.add(diagnostic);
    }

    private int priority(@NotNull KteSyntheticKotlinDiagnosticCollector.Origin origin) {
        return switch (origin) {
            case TEMPLATE_DIRECTIVE, TEMPLATE_STRUCTURE -> 3;
            case SYNTHETIC_KOTLIN -> 2;
            case FALLBACK -> 1;
        };
    }

    @NotNull
    List<KteSyntheticKotlinDiagnosticCollector.Diagnostic> diagnostics() {
        return List.copyOf(diagnostics);
    }

    private boolean isDuplicate(@NotNull KteSyntheticKotlinDiagnosticCollector.Diagnostic existing,
                                @NotNull KteSyntheticKotlinDiagnosticCollector.Diagnostic candidate,
                                @NotNull String existingCategory,
                                @NotNull String candidateCategory) {
        if (!existing.severity().equals(candidate.severity()) ||
                !existingCategory.equals(candidateCategory) ||
                (!existing.message().equals(candidate.message()) && !allowsDifferentMessages(existingCategory))) {
            return false;
        }

        return existing.templateRange().equals(candidate.templateRange()) ||
                existing.templateRange().intersects(candidate.templateRange());
    }

    private boolean isMoreSpecific(@NotNull TextRange candidate, @NotNull TextRange existing) {
        return existing.contains(candidate) && candidate.getLength() < existing.getLength();
    }

    private boolean allowsDifferentMessages(@NotNull String category) {
        return category.startsWith("Unresolved reference:") ||
                "Invalid condition".equals(category) ||
                "Nullable receiver".equals(category) ||
                "Missing required parameters".equals(category) ||
                "Duplicate parameter".equals(category) ||
                "Type mismatch".equals(category);
    }

    @NotNull
    private String category(@NotNull String message) {
        if (message.startsWith("Kotlin syntax error:")) {
            return "Kotlin syntax error";
        }
        if (message.contains("Unresolved reference:")) {
            return message.substring(message.indexOf("Unresolved reference:"));
        }
        if (message.startsWith("Unresolved import:")) {
            return "Unresolved import";
        }
        if (message.startsWith("Unresolved type:")) {
            return "Unresolved type";
        }
        if (message.startsWith("Condition must be Boolean") ||
                message.contains("Condition type mismatch")) {
            return "Invalid condition";
        }
        if (message.startsWith("For-loop range")) {
            return "Invalid for-loop range";
        }
        if (message.startsWith("Only safe (?.) or non-null asserted") ||
                message.contains("Only safe (?.) or non-null asserted")) {
            return "Nullable receiver";
        }
        if (message.startsWith("Missing required parameters:") ||
                message.contains("No value passed for parameter")) {
            return "Missing required parameters";
        }
        if (message.startsWith("Duplicate parameter ") ||
                message.contains("argument is already passed") ||
                message.contains("already passed for this parameter")) {
            return "Duplicate parameter";
        }
        if (message.startsWith("Unknown parameter ") ||
                message.contains("Cannot find a parameter with this name") ||
                message.contains("No parameter with name")) {
            return "Unknown parameter";
        }
        if (message.startsWith("Missing parameter assignment")) {
            return "Missing parameter assignment";
        }
        if (message.startsWith("Too many positional arguments") ||
                message.contains("Too many arguments")) {
            return "Too many positional arguments";
        }
        if (message.contains(" cannot be cast to ") ||
                message.contains("Argument type mismatch") ||
                message.contains("actual type is") && message.contains("but") && message.contains("was expected")) {
            return "Type mismatch";
        }
        if (message.startsWith("Argument '") && message.contains(" is nullable for non-null parameter ")) {
            return "Nullable argument";
        }

        return message;
    }
}
