package org.jusecase.kte.intellij.language;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

public class KteTemplateHighlighter extends LayeredLexerEditorHighlighter {
    public KteTemplateHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile, @NotNull EditorColorsScheme colors) {
        super(new KteHighlighter(), colors);

        FileType type = null;
        if (project == null || virtualFile == null) {
            type = FileTypes.PLAIN_TEXT;
        }
        else {
            Language language = TemplateDataLanguageMappings.getInstance(project).getMapping(virtualFile);
            if (language != null) type = language.getAssociatedFileType();
            if (type == null) type = StdFileTypes.HTML;
        }

        SyntaxHighlighter outerHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(type, project, virtualFile);
        if (outerHighlighter != null) {
            registerLayer(KteTokenTypes.HTML_CONTENT, new LayerDescriptor(outerHighlighter, ""));
        }

        SyntaxHighlighter outerKotlinHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(KotlinLanguage.INSTANCE, project, virtualFile);
        if (outerKotlinHighlighter != null) {
            registerLayer(KteTokenTypes.KOTLIN_INJECTION, new LayerDescriptor(outerKotlinHighlighter, ""));
        }
    }
}
