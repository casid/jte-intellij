package org.jusecase.jte.intellij.language;

import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.InjectedLanguagePlaces;
import com.intellij.psi.LanguageInjector;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.psi.JtePsiElement;
import org.jusecase.jte.intellij.language.psi.JtePsiJavaCodeElement;
import org.jusecase.jte.intellij.language.psi.JtePsiParam;

public class JteJavaInjector implements LanguageInjector {
    @Override
    public void getLanguagesToInject(@NotNull PsiLanguageInjectionHost host, @NotNull InjectedLanguagePlaces injectionPlacesRegistrar) {
        if (host instanceof JtePsiJavaCodeElement) {
            JtePsiJavaCodeElement javaCodeElement = (JtePsiJavaCodeElement)host;

            JtePsiElement parent = (JtePsiElement)javaCodeElement.getParent();

            if (parent instanceof JtePsiParam) {
                //if (isFirstParam((JtePsiParam)parent)) {
                    //injectionPlacesRegistrar.addPlace(StdFileTypes.JAVA.getLanguage(), new TextRange(0, javaCodeElement.getTextLength()), "import ", ";");
                //} else {
                 //   injectionPlacesRegistrar.addPlace(StdFileTypes.JAVA.getLanguage(), new TextRange(0, javaCodeElement.getTextLength()), ", ", ") {}");
                //}
            }
        }
    }

    private boolean isFirstParam(JtePsiParam param) {
        PsiElement prevSibling = param;

        while (true) {
            prevSibling = prevSibling.getPrevSibling();
            if (prevSibling == null) {
                break;
            }

            if (prevSibling instanceof JtePsiParam) {
                return false;
            }
        }

        return true;
    }
}
