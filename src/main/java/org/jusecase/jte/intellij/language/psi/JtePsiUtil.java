package org.jusecase.jte.intellij.language.psi;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.JteJavaLanguageInjector;

public class JtePsiUtil {
    public static void rename(JtePsiElement element, String name) throws IncorrectOperationException {
        LeafPsiElement leaf = PsiTreeUtil.getChildOfType(element, LeafPsiElement.class);
        if (leaf == null) {
            throw new IncorrectOperationException("Could not rename, no leaf found!");
        }

        leaf.replaceWithText(name);
    }

    @NotNull
    public static <T extends PsiElement> T getFirstSiblingOfSameType(T element, Class<T> clazz) {
        T sibling = PsiTreeUtil.getPrevSiblingOfType(element, clazz);
        if (sibling == null) {
            return element;
        } else {
            return getFirstSiblingOfSameType(sibling, clazz);
        }
    }

    @Nullable
    public static <T extends PsiElement> T getFirstSiblingOfType(PsiElement element, Class<T> clazz) {
        T result = null;
        T sibling = PsiTreeUtil.getPrevSiblingOfType(element, clazz);
        while (sibling != null) {
            result = sibling;
            sibling = PsiTreeUtil.getPrevSiblingOfType(sibling, clazz);
        }

        return result;
    }

    @Nullable
    public static PsiElement getPrevSiblingIgnoring(PsiElement element, IElementType elementType) {
        for (PsiElement child = element.getPrevSibling(); child != null; child = child.getPrevSibling()) {
            if (child.getNode().getElementType() != elementType) {
                return child;
            }
        }
        return null;
    }

    public static PsiParameterList resolveParameterList(PsiFile tagOrLayoutFile) {
        PsiJavaFile javaFile = tagOrLayoutFile.getUserData(JteJavaLanguageInjector.JAVA_FILE_KEY);
        if (javaFile == null) {
            // Try to trigger injection and check if java file is there afterwards
            InjectedLanguageManager.getInstance(tagOrLayoutFile.getProject()).findInjectedElementAt(tagOrLayoutFile, 0);
            javaFile = tagOrLayoutFile.getUserData(JteJavaLanguageInjector.JAVA_FILE_KEY);
            if (javaFile == null) {
                return null;
            }
        }

        PsiClass javaClass = PsiTreeUtil.getChildOfType(javaFile, PsiClass.class);
        if (javaClass == null) {
            return null;
        }

        PsiMethod javaMethod = PsiTreeUtil.getChildOfType(javaClass, PsiMethod.class);
        if (javaMethod == null) {
            return null;
        }

        return PsiTreeUtil.getChildOfType(javaMethod, PsiParameterList.class);
    }

    @SuppressWarnings("unused")
    public static <T extends PsiElement> T getLastChildOfType(PsiElement element, Class<T> clazz) {
        T result = PsiTreeUtil.getChildOfType(element, clazz);
        if (result == null) {
            return null;
        }

        T sibling = result;
        while (sibling != null) {
            result = sibling;
            sibling = PsiTreeUtil.getNextSiblingOfType(result, clazz);
        }

        return result;
    }
}
