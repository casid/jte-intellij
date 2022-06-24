package org.jusecase.jte.intellij.language.completion;

import static com.intellij.patterns.PlatformPatterns.psiElement;

import com.intellij.codeInsight.completion.CompletionContributor;


public class HtmlCompletionContributor extends CompletionContributor {

   public HtmlCompletionContributor() {
      extend(null, psiElement(), new TemplateCompletionProvider());
   }
}