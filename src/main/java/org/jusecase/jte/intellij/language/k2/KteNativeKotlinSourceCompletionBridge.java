package org.jusecase.jte.intellij.language.k2;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class KteNativeKotlinSourceCompletionBridge {
    private static final List<String> DEBUG_EVENTS = new CopyOnWriteArrayList<>();
    private static volatile boolean debugEnabled;

    public boolean complete(@NotNull CompletionParameters parameters,
                            @NotNull CompletionResultSet result,
                            @NotNull PsiFile templateFile,
                            int templateOffset) {
        try {
            return new KteNativeKotlinCompletionBridge(KteNativeKotlinSourceCompletionBridge::debug)
                    .complete(parameters, result, templateFile, templateOffset);
        } catch (LinkageError exception) {
            debug("nativeK2 unavailable: " + exception.getClass().getName() + ": " + exception.getMessage());
            return false;
        }
    }

    @TestOnly
    public static void enableDebug() {
        DEBUG_EVENTS.clear();
        debugEnabled = true;
    }

    @TestOnly
    public static void disableDebug() {
        debugEnabled = false;
        DEBUG_EVENTS.clear();
    }

    @TestOnly
    public static List<String> debugEvents() {
        return List.copyOf(DEBUG_EVENTS);
    }

    private static void debug(@NotNull String event) {
        if (debugEnabled) {
            DEBUG_EVENTS.add(event);
        }
    }
}
