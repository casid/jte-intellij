package org.jusecase.kte.intellij.language.completion;

import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlText;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.NotNull;
import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

/**
 * Workaround to prevent HTML confidence from suppressing auto popup
 */
public class KteHtmlCompletionConfidence extends CompletionConfidence {
    @NotNull
    @Override
    public ThreeState shouldSkipAutopopup(@NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {
        if (psiFile.getFileElementType() != KteTokenTypes.FILE) {
            return ThreeState.UNSURE;
        }

        ASTNode node = contextElement.getNode();
        if (node != null && node.getElementType() == XmlTokenType.XML_DATA_CHARACTERS) {
            PsiElement parent = contextElement.getParent();
            if (parent instanceof XmlText || parent instanceof XmlDocument) {
                String contextElementText = contextElement.getText();
                int endOffset = offset - contextElement.getTextRange().getStartOffset();
                String prefix = contextElementText.substring(0, Math.min(contextElementText.length(), endOffset));
                if (StringUtil.startsWithChar(prefix, '@')) {
                    return ThreeState.NO;
                }
            }
        }

        return ThreeState.UNSURE;
    }
}
