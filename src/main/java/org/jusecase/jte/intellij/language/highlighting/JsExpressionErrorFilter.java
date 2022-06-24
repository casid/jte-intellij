package org.jusecase.jte.intellij.language.highlighting;

import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.JteLanguage;
import org.jusecase.jte.intellij.language.KteLanguage;

import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.templateLanguages.OuterLanguageElement;


final class JsExpressionErrorFilter extends HighlightErrorFilter {

   @Override
   public boolean shouldHighlightErrorElement( @NotNull final PsiErrorElement element ) {
      Language elementBaseLanguage = element.getLanguage().getBaseLanguage();
      if ( elementBaseLanguage == null ) {
         return true;
      }

      if ( !"JavaScript".equals(elementBaseLanguage.getID()) ) {
         return true;
      }

      final PsiFile psiFile = element.getContainingFile();
      if ( psiFile == null ) {
         return true;
      }

      Language baseLanguage = psiFile.getViewProvider().getBaseLanguage();
      if ( baseLanguage != JteLanguage.INSTANCE && baseLanguage != KteLanguage.INSTANCE ) {
         return true;
      }

      if ( !"Expression expected".equals(element.getErrorDescription()) ) {
         return true;
      }

      for ( PsiElement e = element.getParent(); e != null; e = e.getNextSibling() ) {
         if ( e instanceof PsiWhiteSpace ) {
            continue;
         }

         if ( e instanceof OuterLanguageElement ) {
            String elementText = e.getText();

            //noinspection RedundantIfStatement
            if ( elementText != null && (elementText.startsWith("${") || elementText.startsWith("$unsafe{")) ) {
               return false;
            } else {
               return true;
            }
         }
      }

      return true;
   }
}

