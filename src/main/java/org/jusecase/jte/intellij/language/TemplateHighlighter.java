package org.jusecase.jte.intellij.language;

import com.intellij.ide.highlighter.HtmlFileType;
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
import org.jusecase.jte.intellij.language.parsing.TokenTypes;

public class TemplateHighlighter extends LayeredLexerEditorHighlighter {
    public TemplateHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile, @NotNull EditorColorsScheme colors, LanguageFileType expressionLanguageFileType, TokenTypes tokenTypes, SyntaxHighlighter syntaxHighlighter) {
        super(syntaxHighlighter, colors);

        FileType type = null;
        if (project == null || virtualFile == null) {
            type = FileTypes.PLAIN_TEXT;
        }
        else {
            Language language = TemplateDataLanguageMappings.getInstance(project).getMapping(virtualFile);
            if (language != null) type = language.getAssociatedFileType();
            if (type == null) type = HtmlFileType.INSTANCE;
        }

        SyntaxHighlighter outerHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(type, project, virtualFile);
        if (outerHighlighter != null) {
            registerLayer(tokenTypes.HTML_CONTENT(), new LayerDescriptor(outerHighlighter, ""));
        }

        SyntaxHighlighter outerExpressionLanguageHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(expressionLanguageFileType, project, virtualFile);
        if (outerExpressionLanguageHighlighter != null) {
            registerLayer(tokenTypes.JAVA_INJECTION(), new LayerDescriptor(outerExpressionLanguageHighlighter, ""));
        }
    }
}
