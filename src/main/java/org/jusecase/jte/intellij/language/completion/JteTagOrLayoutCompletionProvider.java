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
import org.jusecase.jte.intellij.language.psi.JtePsiExtraJavaInjection;
import org.jusecase.jte.intellij.language.psi.JtePsiParam;
import org.jusecase.jte.intellij.language.psi.JtePsiTagOrLayoutName;
import org.jusecase.jte.intellij.language.psi.JtePsiUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class JteTagOrLayoutCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        if (position.getParent() == null) {
            return;
        }

        if (!(position.getParent() instanceof JtePsiTagOrLayoutName)) {
            return;
        }
        JtePsiTagOrLayoutName nameElement = (JtePsiTagOrLayoutName) position.getParent();

        JtePsiTagOrLayoutName prevNameElement = PsiTreeUtil.getPrevSiblingOfType(nameElement, JtePsiTagOrLayoutName.class);
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
            int index = name.lastIndexOf(".jte");
            if (index == -1 || !name.endsWith(".jte")) {
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
                if (editor.getDocument().getCharsSequence().charAt(offset) == '(') {
                    offset += 1;
                    needsParenthesis = false;
                }

                editor.getCaretModel().moveToOffset(offset);

                TemplateManager manager = TemplateManager.getInstance(context.getProject());
                Template template = manager.createTemplate("", "");
                if (needsParenthesis) {
                    template.addTextSegment("(");
                }

                PsiParameterList parameterList = JtePsiUtil.resolveParameterList(tagOrLayoutFile);
                if (parameterList != null) {
                    int i = 0;
                    List<PsiParameter> parameters = resolveRequiredParams(parameterList);
                    for (PsiParameter parameter : parameters) {
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

        private List<PsiParameter> resolveRequiredParams(PsiParameterList parameterList) {
            PsiParameter[] parameters = parameterList.getParameters();
            if (parameters.length == 0) {
                return Collections.emptyList();
            }

            List<PsiParameter> result = new ArrayList<>();

            boolean[] defaultParams = resolveDefaultParams(parameters.length);
            for (int i = 0; i < parameters.length; i++) {
                if (!defaultParams[i]) {
                    PsiParameter parameter = parameters[i];
                    if (!parameter.isVarArgs()) {
                        result.add(parameter);
                    }
                }
            }

            return result;
        }

        private boolean[] resolveDefaultParams(int paramLength) {
            boolean[] defaultParams = new boolean[paramLength];
            AtomicInteger index = new AtomicInteger();

            SyntaxTraverser.psiTraverser(tagOrLayoutFile).filter(JtePsiParam.class).forEach(param -> {
                if (index.get() >= defaultParams.length) {
                    return;
                }

                defaultParams[index.getAndIncrement()] = param.getLastChild() instanceof JtePsiExtraJavaInjection;
            });

            return defaultParams;
        }
    }
}
