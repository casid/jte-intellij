package org.jusecase.jte.intellij.language.completion;

import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.completion.HtmlInTextCompletionPopupExtension;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;


@SuppressWarnings("UnstableApiUsage")
public class JteHtmlInTextCompletionPopupExtension implements HtmlInTextCompletionPopupExtension {

   @Override
   public boolean isDeselectingFirstItemDisabled( @NotNull PsiElement element ) {
      PsiFile containingFile = element.getContainingFile();
      if (containingFile == null) {
         return false;
      }

      if (!containingFile.getName().endsWith(".jte") && !containingFile.getName().endsWith(".kte")) {
         return false;
      }

      if (element.getTextLength() != 1) {
         return false;
      }

      return element.textContains('$') || element.textContains('@') || element.textContains('!');
   }
}
