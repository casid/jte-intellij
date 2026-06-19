package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.daemon.impl.IntentionActionFilter;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.psi.JtePsiFile;

/**
 * Java's built-in "Remove unused imports" and "Optimize imports" quick fixes (offered for the
 * "unused import"/"imports are not sorted" warnings) operate on the injected Java file and try to
 * modify the guarded {@code "import "}/{@code ";"} text that jte adds around each {@code @import},
 * which either does nothing or crashes. {@link JteRemoveUnusedImportIntention} and
 * {@link JteImportOptimizer} already provide working replacements, so hide the built-in ones for
 * jte-injected files.
 */
public class JteUnusedImportFixFilter implements IntentionActionFilter {

    @Override
    public boolean accept(@NotNull IntentionAction intentionAction, @Nullable PsiFile file) {
        if (file == null) {
            return true;
        }

        PsiFile topLevelFile = InjectedLanguageManager.getInstance(file.getProject()).getTopLevelFile(file);
        if (!(topLevelFile instanceof JtePsiFile)) {
            return true;
        }

        String familyName = intentionAction.getFamilyName();
        return !familyName.equals("Remove unused imports")
                && !familyName.equals("Optimize imports");
    }
}
