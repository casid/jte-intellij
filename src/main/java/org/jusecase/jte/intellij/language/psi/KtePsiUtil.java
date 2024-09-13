package org.jusecase.jte.intellij.language.psi;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.kotlin.psi.*;
import org.jusecase.jte.intellij.language.KteKotlinLanguageInjector;

public class KtePsiUtil {

    public static KtParameterList resolveParameterList(PsiFile templateFile) {
        KtFile kotlinFile = templateFile.getUserData(KteKotlinLanguageInjector.KOTLIN_FILE_KEY);
        if (kotlinFile == null) {
            // Try to trigger injection and check if kotlin file is there afterwards
            InjectedLanguageManager.getInstance(templateFile.getProject()).findInjectedElementAt(templateFile, 0);
            kotlinFile = templateFile.getUserData(KteKotlinLanguageInjector.KOTLIN_FILE_KEY);
            if (kotlinFile == null) {
                return null;
            }
        }

        KtClass kotlinClass = PsiTreeUtil.findChildOfType(kotlinFile, KtClass.class);
        if (kotlinClass == null) {
            return null;
        }

        KtFunction renderMethod = PsiTreeUtil.findChildOfType(kotlinClass, KtFunction.class);
        if (renderMethod == null) {
            return null;
        }

        return PsiTreeUtil.getChildOfType(renderMethod, KtParameterList.class);
    }
}
