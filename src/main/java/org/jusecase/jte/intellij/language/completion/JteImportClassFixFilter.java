package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.daemon.impl.IntentionActionFilter;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.IntentionActionDelegate;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.psi.JtePsiFile;

/**
 * The built-in "Import class" quick fix is broken for jte's injected Java fragments,
 * and JteAddImportIntention provides a working replacement. Offering both is confusing,
 * so hide the built-in one for jte-injected references.
 */
public class JteImportClassFixFilter implements IntentionActionFilter {

    @Override
    public boolean accept(@NotNull IntentionAction intentionAction, @Nullable PsiFile file) {
        if (file == null) {
            return true;
        }

        PsiFile topLevelFile = InjectedLanguageManager.getInstance(file.getProject()).getTopLevelFile(file);
        if (!(topLevelFile instanceof JtePsiFile)) {
            return true;
        }

        String className = IntentionActionDelegate.unwrap(intentionAction).getClass().getName();
        return !className.startsWith("com.intellij.codeInsight.daemon.impl.quickfix.ImportClassFix");
    }
}
