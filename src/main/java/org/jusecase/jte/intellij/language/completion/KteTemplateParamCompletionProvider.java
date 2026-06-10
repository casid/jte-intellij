package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.parsing.KteTokenTypes;
import org.jusecase.jte.intellij.language.k2.KteTemplateSignatureService;
import org.jusecase.jte.intellij.language.psi.*;
import org.jusecase.jte.intellij.language.template.KteTemplateCallArguments;

import java.util.Set;

public class KteTemplateParamCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
        PsiElement jteElement = parameters.getOriginalPosition();
        if (jteElement == null) {
            return;
        }

        if (jteElement.getNode().getElementType() == KteTokenTypes.PARAM_NAME) {
            // The user already started typing, that's okay
            jteElement = jteElement.getParent();
        } else if (isAfterParamSeparator(jteElement, parameters.getOffset())) {
            // The KTE lexer can keep "value, <caret>" inside the previous JAVA_INJECTION token.
        } else {
            PsiElement prevSibling = JtePsiUtil.getPrevSiblingIgnoring(jteElement, KteTokenTypes.WHITESPACE);
            if (prevSibling == null) {
                return;
            }
            IElementType elementType = prevSibling.getNode().getElementType();
            if (elementType != KteTokenTypes.PARAMS_BEGIN && elementType != KteTokenTypes.COMMA && elementType != KteTokenTypes.JAVA_INJECTION) {
                return;
            }
        }

        JtePsiTemplate template = PsiTreeUtil.getParentOfType(jteElement, JtePsiTemplate.class);
        if (template == null) {
            return;
        }

        JtePsiTemplateName templateName = JtePsiUtil.getLastChildOfType(template, JtePsiTemplateName.class);
        if (templateName == null) {
            return;
        }

        PsiFile templateFile = templateName.resolveFile();
        if (templateFile == null) {
            return;
        }

        KteTemplateSignatureService.TemplateSignature signature = KteTemplateSignatureService.resolve(templateFile);
        Set<String> usedNames = KteTemplateCallArguments.usedNamedParameters(template);
        for (KteTemplateSignatureService.Parameter parameter : signature.parameters()) {
            if (!usedNames.contains(parameter.name())) {
                result.addElement(LookupElementBuilder.create(parameter.name() + " =").withTypeText(parameter.typeText()));
            }
        }
    }

    private boolean isAfterParamSeparator(@NotNull PsiElement element, int caretOffset) {
        if (element.getNode().getElementType() != KteTokenTypes.JAVA_INJECTION) {
            return false;
        }

        int offsetInElement = Math.clamp(
                caretOffset - element.getTextRange().getStartOffset(),
                0,
                element.getTextLength()
        );
        String text = element.getText();
        for (int index = offsetInElement - 1; index >= 0; index--) {
            char current = text.charAt(index);
            if (!Character.isWhitespace(current)) {
                return current == ',';
            }
        }

        return false;
    }
}
