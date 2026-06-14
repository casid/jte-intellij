package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class KteSyntheticKotlinFile {
    private final String fileName;
    private final String text;
    private final List<KteSyntheticKotlinRangeMapping> mappings;

    public KteSyntheticKotlinFile(@NotNull String fileName, @NotNull String text, @NotNull List<KteSyntheticKotlinRangeMapping> mappings) {
        this.fileName = fileName;
        this.text = text;
        this.mappings = List.copyOf(mappings);
    }

    @NotNull
    public String getFileName() {
        return fileName;
    }

    @NotNull
    public String getText() {
        return text;
    }

    @NotNull
    public List<KteSyntheticKotlinRangeMapping> getMappings() {
        return mappings;
    }

    @Nullable
    public Integer mapKotlinOffsetToTemplate(int kotlinOffset) {
        for (KteSyntheticKotlinRangeMapping mapping : mappings) {
            Integer templateOffset = mapping.mapKotlinOffsetToTemplate(kotlinOffset);
            if (templateOffset != null) {
                return templateOffset;
            }
        }

        return null;
    }

    @Nullable
    public Integer mapTemplateOffsetToKotlin(int templateOffset) {
        for (KteSyntheticKotlinRangeMapping mapping : mappings) {
            Integer kotlinOffset = mapping.mapTemplateOffsetToKotlin(templateOffset);
            if (kotlinOffset != null) {
                return kotlinOffset;
            }
        }

        return null;
    }

    @Nullable
    public TextRange mapKotlinRangeToTemplate(@NotNull TextRange range) {
        for (KteSyntheticKotlinRangeMapping mapping : mappings) {
            TextRange templateRange = mapping.mapKotlinRangeToTemplate(range);
            if (templateRange != null) {
                return templateRange;
            }
        }

        return null;
    }

    @Nullable
    public TextRange mapKotlinSourceEditRangeToTemplate(@NotNull TextRange range) {
        for (KteSyntheticKotlinRangeMapping mapping : mappings) {
            if (mapping.getEditPolicy() != KteSyntheticKotlinRangeMapping.EditPolicy.SOURCE_EDITABLE) {
                continue;
            }

            TextRange templateRange = mapping.mapKotlinRangeToTemplate(range);
            if (templateRange != null) {
                return templateRange;
            }
        }

        return null;
    }

    public boolean isKotlinRangeFullySourceEditable(@NotNull TextRange range) {
        return mapKotlinSourceEditRangeToTemplate(range) != null;
    }

    public boolean touchesGeneratedKotlin(@NotNull TextRange range) {
        return mapKotlinRangeToTemplate(range) == null && !isKotlinImportRange(range);
    }

    @Nullable
    public TextRange mapKotlinErrorRangeToTemplate(@NotNull TextRange range) {
        if (range.isEmpty()) {
            Integer templateOffset = mapKotlinOffsetToTemplate(range.getStartOffset());
            return templateOffset == null ? null : TextRange.from(templateOffset, 1);
        }

        for (KteSyntheticKotlinRangeMapping mapping : mappings) {
            TextRange overlap = overlap(range, mapping.getKotlinRange());
            if (overlap == null) {
                continue;
            }

            TextRange templateRange = mapping.mapKotlinRangeToTemplate(overlap);
            if (templateRange != null && !templateRange.isEmpty()) {
                return templateRange;
            }
        }

        return null;
    }

    @Nullable
    public TextRange mapTemplateRangeToKotlin(@NotNull TextRange range) {
        for (KteSyntheticKotlinRangeMapping mapping : mappings) {
            TextRange kotlinRange = mapping.mapTemplateRangeToKotlin(range);
            if (kotlinRange != null) {
                return kotlinRange;
            }
        }

        return null;
    }

    private boolean isKotlinImportRange(@NotNull TextRange range) {
        for (KteSyntheticKotlinRangeMapping mapping : mappings) {
            if (mapping.getEditPolicy() != KteSyntheticKotlinRangeMapping.EditPolicy.IMPORT_EDITABLE) {
                continue;
            }

            if (mapping.mapKotlinRangeToTemplate(range) != null) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    private TextRange overlap(@NotNull TextRange first, @NotNull TextRange second) {
        int start = Math.max(first.getStartOffset(), second.getStartOffset());
        int end = Math.min(first.getEndOffset(), second.getEndOffset());
        return start < end ? new TextRange(start, end) : null;
    }
}
