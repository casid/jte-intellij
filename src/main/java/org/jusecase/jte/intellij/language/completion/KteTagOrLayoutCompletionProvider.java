package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.macro.CompleteMacro;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.psi.KtParameter;
import org.jetbrains.kotlin.psi.KtParameterList;
import org.jusecase.jte.intellij.language.psi.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KteTagOrLayoutCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        if (position.getParent() == null) {
            return;
        }

        if (!(position.getParent() instanceof JtePsiTagName)) {
            return;
        }
        JtePsiTagName nameElement = (JtePsiTagName) position.getParent();

        JtePsiTagName prevNameElement = PsiTreeUtil.getPrevSiblingOfType(nameElement, JtePsiTagName.class);
        if (prevNameElement == null) {
            PsiDirectory directory = nameElement.findRootDirectory();
            if (directory != null) {
                addSuggestionsForDirectory(directory, result);
            }
        } else {
            PsiReference reference = prevNameElement.getReference();
            if (reference == null) {
                return;
            }

            PsiElement prevReferenceElement = reference.resolve();
            if (prevReferenceElement instanceof PsiDirectory) {
                addSuggestionsForDirectory((PsiDirectory) prevReferenceElement, result);
            }
        }
    }

    private void addSuggestionsForDirectory(PsiDirectory directory, @NotNull CompletionResultSet result) {
        for (PsiDirectory subdirectory : directory.getSubdirectories()) {
            result.addElement(LookupElementBuilder.create(subdirectory));
        }

        for (PsiFile file : directory.getFiles()) {
            String name = file.getName();
            int index = name.lastIndexOf(".kte");
            if (index == -1 || !name.endsWith(".kte")) {
                continue;
            }

            String referenceName = name.substring(0, index);
            result.addElement(LookupElementBuilder.create(referenceName).withInsertHandler(new AfterCompletionInsertHandler(file)));
        }
    }

    private static class AfterCompletionInsertHandler implements InsertHandler<LookupElement> {
        private final PsiFile tagOrLayoutFile;

        private AfterCompletionInsertHandler(PsiFile tagOrLayoutFile) {
            this.tagOrLayoutFile = tagOrLayoutFile;
        }

        @Override
        public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
            context.setLaterRunnable(() -> {
                Editor editor = context.getEditor();
                int offset = context.getTailOffset();
                boolean needsParenthesis = true;
                CharSequence documentText = editor.getDocument().getImmutableCharSequence();
                if (offset < documentText.length() && documentText.charAt(offset) == '(') {
                    offset += 1;
                    needsParenthesis = false;
                }

                editor.getCaretModel().moveToOffset(offset);

                TemplateManager manager = TemplateManager.getInstance(context.getProject());
                Template template = manager.createTemplate("", "");
                if (needsParenthesis) {
                    template.addTextSegment("(");
                }

                KtParameterList parameterList = KtePsiUtil.resolveParameterList(tagOrLayoutFile);
                if (parameterList != null) {
                    int i = 0;
                    List<KtParameter> parameters = resolveRequiredParams(parameterList);
                    for (KtParameter parameter : parameters) {
                        template.addTextSegment(parameter.getName() + " = ");
                        MacroCallNode param = new MacroCallNode(new CompleteMacro());
                        template.addVariable("param" + i, param, param, true);

                        if (++i < parameters.size()) {
                            template.addTextSegment(", ");
                        }
                    }
                }

                if (needsParenthesis) {
                    template.addTextSegment(")");
                }

                manager.startTemplate(editor, template);
            });
        }

        private List<KtParameter> resolveRequiredParams(KtParameterList parameterList) {
            List<KtParameter> parameters = parameterList.getParameters();
            if (parameters.isEmpty()) {
                return Collections.emptyList();
            }

            List<KtParameter> result = new ArrayList<>();

            for (KtParameter parameter : parameters) {
                if (!parameter.hasDefaultValue() && !parameter.isVarArg()) {
                    result.add(parameter);
                }
            }

            return result;
        }

    }
}
