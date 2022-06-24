package org.jusecase.jte.intellij.language.completion;

import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.ProcessingContext;


public class TemplateCompletionProvider extends CompletionProvider<CompletionParameters> {

   @Override
   protected void addCompletions( @NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result ) {
      result.addElement(LookupElementBuilder.create("@template.").withInsertHandler(( ctx, item ) -> {
         if ( ctx.getStartOffset() > 0 && "@".equals(ctx.getDocument().getText(TextRange.create(ctx.getStartOffset() - 1, ctx.getStartOffset()))) ) {
            ctx.getDocument().deleteString(ctx.getStartOffset() - 1, ctx.getStartOffset());
         }
         AutoPopupController.getInstance(ctx.getProject()).autoPopupMemberLookup(ctx.getEditor(), file -> true);
      }).withTypeText("jte template"));
   }
}
