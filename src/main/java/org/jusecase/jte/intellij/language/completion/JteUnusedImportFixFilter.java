package org.jusecase.jte.intellij.language.completion;

import com.intellij.codeInsight.daemon.impl.IntentionActionFilter;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.IntentionActionDelegate;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.impl.modcommand.ModCommandActionWrapper;
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

        IntentionAction delegate = IntentionActionDelegate.unwrap(intentionAction);
        String className = delegate instanceof ModCommandActionWrapper wrapper
                ? wrapper.asModCommandAction().getClass().getName()
                : delegate.getClass().getName();

        return !className.startsWith("com.intellij.codeInsight.daemon.impl.analysis.RemoveAllUnusedImportsFix")
                && !className.startsWith("com.intellij.codeInsight.intention.impl.config.QuickFixFactoryImpl$OptimizeImportsFix");
    }
}
