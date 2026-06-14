package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class KteSyntheticKotlinRangeMapping {
    public enum Kind {
        IMPORT,
        PARAMETER,
        PARAMETER_DEFAULT_VALUE,
        OUTPUT_EXPRESSION,
        UNSAFE_OUTPUT_EXPRESSION,
        STATEMENT,
        IF_CONDITION,
        ELSE_IF_CONDITION,
        FOR_CONDITION,
        TEMPLATE_CALL,
        TEMPLATE_ARGUMENT_NAME,
        TEMPLATE_ARGUMENT_VALUE
    }

    public enum EditPolicy {
        SOURCE_EDITABLE,
        IMPORT_EDITABLE
    }

    private final Kind kind;
    private final TextRange templateRange;
    private final TextRange kotlinRange;
    private final EditPolicy editPolicy;

    public KteSyntheticKotlinRangeMapping(@NotNull Kind kind, @NotNull TextRange templateRange, @NotNull TextRange kotlinRange) {
        this(kind, templateRange, kotlinRange, defaultEditPolicy(kind));
    }

    public KteSyntheticKotlinRangeMapping(@NotNull Kind kind,
                                          @NotNull TextRange templateRange,
                                          @NotNull TextRange kotlinRange,
                                          @NotNull EditPolicy editPolicy) {
        this.kind = kind;
        this.templateRange = templateRange;
        this.kotlinRange = kotlinRange;
        this.editPolicy = editPolicy;
    }

    @NotNull
    public Kind getKind() {
        return kind;
    }

    @NotNull
    public TextRange getTemplateRange() {
        return templateRange;
    }

    @NotNull
    public TextRange getKotlinRange() {
        return kotlinRange;
    }

    @NotNull
    public EditPolicy getEditPolicy() {
        return editPolicy;
    }

    @Nullable
    public Integer mapKotlinOffsetToTemplate(int kotlinOffset) {
        if (!contains(kotlinRange, kotlinOffset)) {
            return null;
        }

        return templateRange.getStartOffset() + kotlinOffset - kotlinRange.getStartOffset();
    }

    @Nullable
    public Integer mapTemplateOffsetToKotlin(int templateOffset) {
        if (!contains(templateRange, templateOffset)) {
            return null;
        }

        return kotlinRange.getStartOffset() + templateOffset - templateRange.getStartOffset();
    }

    @Nullable
    public TextRange mapKotlinRangeToTemplate(@NotNull TextRange range) {
        if (!containsRangeForEdit(kotlinRange, range)) {
            return null;
        }

        int startOffset = templateRange.getStartOffset() + range.getStartOffset() - kotlinRange.getStartOffset();
        return new TextRange(startOffset, startOffset + range.getLength());
    }

    @Nullable
    public TextRange mapTemplateRangeToKotlin(@NotNull TextRange range) {
        if (range.getStartOffset() < templateRange.getStartOffset() || range.getEndOffset() > templateRange.getEndOffset()) {
            return null;
        }

        int startOffset = kotlinRange.getStartOffset() + range.getStartOffset() - templateRange.getStartOffset();
        return new TextRange(startOffset, startOffset + range.getLength());
    }

    private boolean contains(TextRange range, int offset) {
        return offset >= range.getStartOffset() && offset < range.getEndOffset();
    }

    private static boolean containsRangeForEdit(@NotNull TextRange container, @NotNull TextRange range) {
        return range.getStartOffset() >= container.getStartOffset() &&
                range.getEndOffset() <= container.getEndOffset();
    }

    @NotNull
    private static EditPolicy defaultEditPolicy(@NotNull Kind kind) {
        return kind == Kind.IMPORT ? EditPolicy.IMPORT_EDITABLE : EditPolicy.SOURCE_EDITABLE;
    }
}
