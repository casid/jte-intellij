package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.macro.CompleteMacro;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.k2.KteTemplateSignatureService;

import java.util.List;

public class KteTemplateCompletionProvider extends AbstractTemplateCompletionProvider {

    protected KteTemplateCompletionProvider() {
        super(".kte");
    }

    @Override
    protected InsertHandler<LookupElement> createAfterCompletionInsertHandler(PsiFile file) {
        return new AfterCompletionInsertHandler(file);
    }

    private static class AfterCompletionInsertHandler implements InsertHandler<LookupElement> {
        private final PsiFile templateFile;

        private AfterCompletionInsertHandler(PsiFile templateFile) {
            this.templateFile = templateFile;
        }

        @SuppressWarnings("DuplicatedCode")
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

                KteTemplateSignatureService.TemplateSignature signature = KteTemplateSignatureService.resolve(templateFile);
                if (!signature.parameters().isEmpty()) {
                    int i = 0;
                    List<KteTemplateSignatureService.Parameter> parameters = signature.requiredParameters();
                    for (KteTemplateSignatureService.Parameter parameter : parameters) {
                        template.addTextSegment(parameter.name() + " = ");
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

    }
}
