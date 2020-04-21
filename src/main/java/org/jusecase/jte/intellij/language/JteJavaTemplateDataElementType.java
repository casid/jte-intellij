package org.jusecase.jte.intellij.language;

import com.intellij.lang.Language;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.templateLanguages.TemplateDataElementType;
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.parsing.JteLexer;

import static org.jusecase.jte.intellij.language.parsing.JteTokenTypes.JAVA_CONTENT;
import static org.jusecase.jte.intellij.language.parsing.JteTokenTypes.OUTER_JAVA_ELEMENT_TYPE;

public class JteJavaTemplateDataElementType extends PatchedTemplateDataElementType {
    private final ThreadLocal<CurrentState> currentState = ThreadLocal.withInitial(CurrentState::new);

    public JteJavaTemplateDataElementType() {
        super("JTE_TEMPLATE_DATA_JAVA", StdFileTypes.JAVA.getLanguage(), JAVA_CONTENT, OUTER_JAVA_ELEMENT_TYPE);
    }

    @Override
    protected Language getTemplateFileLanguage(TemplateLanguageFileViewProvider viewProvider) {
        return StdFileTypes.JAVA.getLanguage();
    }

    @Override
    protected void appendCurrentTemplateToken(@NotNull StringBuilder result, @NotNull CharSequence buf, @NotNull Lexer lexer, @NotNull RangeCollector collector) {
        CurrentState currentState = getCurrentState();
        if (!currentState.hasWrittenPackage) {
            // TODO find out real package of file
            currentState.prefix.append("package todo;\n");
            currentState.hasWrittenPackage = true;
        }

        System.out.println("Appending " + lexer.getTokenText());

        switch (lexer.getState()) {
            case JteLexer.CONTENT_STATE_JAVA_IMPORT_END:
                currentState.prefix.append("import ");
                currentState.suffix.append(";");
                break;
            case JteLexer.CONTENT_STATE_JAVA_PARAM_END:
                ensureClassIsWritten(currentState);
                ensureRenderMethodStartIsWritten(currentState);

                currentState.prefix.append(", ");

                break;
            case JteLexer.CONTENT_STATE_JAVA_OUTPUT_BEGIN:
                ensureClassIsWritten(currentState);
                ensureRenderMethodIsWritten(currentState);

                //currentState.prefix.append("output = ");
                currentState.suffix.append(";\n");
                break;
        }

        if (currentState.prefix.length() == 0 && currentState.suffix.length() == 0) {
            super.appendCurrentTemplateToken(result, buf, lexer, collector);
        } else {
            wrapOutput(currentState, lexer, collector, result);
        }
    }

    @Override
    protected CharSequence createTemplateText(@NotNull CharSequence sourceCode, @NotNull Lexer lexer, @NotNull RangeCollector collector) {
        StringBuilder result = (StringBuilder)super.createTemplateText(sourceCode, lexer, collector);

        CurrentState currentState = getCurrentState();
        if (currentState.hasWrittenClass) {
            currentState.prefix.append("}");
        }

        if (currentState.hasWrittenRenderMethodEnd) {
            currentState.prefix.append("}");
        }

        String closingStatements = currentState.prefix.toString();
        collector.addRangeToRemove(new TextRange(lexer.getTokenEnd(), lexer.getTokenEnd() + closingStatements.length()));
        result.append(closingStatements);

        currentState.reset();

        System.out.println(result);
        return result;
    }

    @Override
    protected PsiFile createPsiFileFromSource(Language language, CharSequence sourceCode, PsiManager manager) {
        System.out.println("Creating file from source code:");
        System.out.println(sourceCode);
        return super.createPsiFileFromSource(language, sourceCode, manager);
    }

    private void ensureClassIsWritten(CurrentState currentState) {
        if (!currentState.hasWrittenClass) {
            // TODO find out real name of generated class
            currentState.prefix.append("\nclass DummyTemplate {\n");
            currentState.hasWrittenClass = true;
        }
    }

    private void ensureRenderMethodStartIsWritten(CurrentState currentState) {
        if (!currentState.hasWrittenRenderMethodStart) {
            currentState.prefix.append("\nstatic void render(");
            currentState.hasWrittenRenderMethodStart = true;
        }
    }

    private void ensureRenderMethodEndIsWritten(CurrentState currentState) {
        if (!currentState.hasWrittenRenderMethodEnd) {
            currentState.prefix.append(") {\n");
            currentState.hasWrittenRenderMethodEnd = true;
        }
    }

    private void ensureRenderMethodIsWritten(CurrentState currentState) {
        ensureRenderMethodStartIsWritten(currentState);
        ensureRenderMethodEndIsWritten(currentState);
    }

    private void wrapOutput(CurrentState currentState, @NotNull Lexer lexer, @NotNull RangeCollector collector, StringBuilder result) {
        String nextSequence = lexer.getTokenText();

        /*
        if (currentState.prefix.length() > 0) {
            result.append(currentState.prefix);
            collector.addRangeToRemove(new TextRange(lexer.getTokenStart(), lexer.getTokenStart() + currentState.prefix.length()));
        }

        result.append(nextSequence);

        if (currentState.suffix.length() > 0) {
            result.append(currentState.suffix);
            collector.addRangeToRemove(new TextRange(lexer.getTokenEnd(), lexer.getTokenEnd() + currentState.suffix.length()));
        }

        currentState.resetBuffers();*/

        if (currentState.prefix.length() > 0) {
            result.append(currentState.prefix);
            collector.addRangeToRemove(new TextRange(lexer.getTokenStart(), lexer.getTokenStart() + currentState.prefix.length()));
        }

        result.append(nextSequence);

        currentState.swapBuffers();
    }

    private CurrentState getCurrentState() {
        return currentState.get();
    }

    private static class CurrentState {
        boolean hasWrittenPackage;
        boolean hasWrittenClass;
        boolean hasWrittenRenderMethodStart;
        boolean hasWrittenRenderMethodEnd;

        int previousTokenEnd;

        StringBuilder prefix = new StringBuilder();
        StringBuilder suffix = new StringBuilder();

        void reset() {
            hasWrittenPackage = false;
            hasWrittenClass = false;
            hasWrittenRenderMethodStart = false;
            hasWrittenRenderMethodEnd = false;

            resetBuffers();
        }

        void resetBuffers() {
            prefix.setLength(0);
            suffix.setLength(0);
        }

        void swapBuffers() {
            prefix.setLength(0);
            StringBuilder tmp = prefix;
            prefix = suffix;
            suffix = tmp;
        }
    }
}
