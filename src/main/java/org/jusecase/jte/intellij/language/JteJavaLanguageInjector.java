package org.jusecase.jte.intellij.language;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.psi.*;

import java.util.Collections;
import java.util.List;

public class JteJavaLanguageInjector implements MultiHostInjector {
    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        if (context instanceof JtePsiJavaContent) {
            JtePsiJavaContent host = (JtePsiJavaContent)context;

            registrar.startInjecting(StdFileTypes.JAVA.getLanguage());

            boolean hasWrittenClass = false;

            for (PsiElement child : host.getChildren()) {
                if (child instanceof JtePsiImport) {
                    JtePsiJavaInjection javaPart = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
                    if (javaPart != null) {
                        injectJavaPart("import ", ";\n", registrar, host, javaPart);
                    }
                } else if (child instanceof JtePsiParam) {
                    JtePsiJavaInjection javaPart = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
                    if (javaPart != null) {
                        if (!hasWrittenClass) {
                            String classPrefix = "class DummyTemplate { public void render(String output, ";
                            JtePsiParam nextParam = PsiTreeUtil.getNextSiblingOfType(child, JtePsiParam.class);
                            if (nextParam != null) {
                                injectJavaPart(classPrefix, null, registrar, host, javaPart);
                            } else {
                                injectJavaPart(classPrefix, ") {\n", registrar, host, javaPart);
                            }
                            hasWrittenClass = true;
                        } else {
                            JtePsiParam nextParam = PsiTreeUtil.getNextSiblingOfType(child, JtePsiParam.class);
                            if (nextParam != null) {
                                injectJavaPart(", ", null, registrar, host, javaPart);
                            } else {
                                injectJavaPart(", ", ") {\n", registrar, host, javaPart);
                            }
                        }
                    }
                } else {
                    processTemplateBody(child, host, registrar);
                }
            }

            if (hasWrittenClass) {
                registrar.addPlace(null, "\n}}", host, new TextRange(host.getTextLength(), host.getTextLength()));
            }

            registrar.doneInjecting();
        }
    }

    private void processTemplateBody(PsiElement child, JtePsiJavaContent host, MultiHostRegistrar registrar) {
        if (child instanceof JtePsiOutput) {
            JtePsiJavaInjection javaPart = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
            injectJavaPart("output = ", ";\n", registrar, host, javaPart);
        } else if (child instanceof JtePsiStatement) {
            JtePsiJavaInjection javaPart = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
            injectJavaPart(null, ";\n", registrar, host, javaPart);
        } else if (child instanceof JtePsiIf) {
            JtePsiJavaInjection javaPart = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
            injectJavaPart("if (", ") {\n", registrar, host, javaPart);

            JtePsiConditionEnd conditionEnd = PsiTreeUtil.getNextSiblingOfType(javaPart, JtePsiConditionEnd.class);
            if (conditionEnd != null) {
                for (PsiElement conditionSibling = conditionEnd.getNextSibling(); conditionSibling != null; conditionSibling = conditionSibling.getNextSibling()) {
                    processTemplateBody(conditionSibling, host, registrar);
                }
            }
        } else if (child instanceof JtePsiElseIf) {
            JtePsiJavaInjection javaPart = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
            injectJavaPart("} elseif (", ") {\n", registrar, host, javaPart);
        } else if (child instanceof JtePsiElse) {
            int startOffsetInHost = getStartOffsetInHost(host, child);
            registrar.addPlace("\n} else {\n", null, host, new TextRange(startOffsetInHost, startOffsetInHost));
        } else if (child instanceof JtePsiEndIf) {
            int startOffsetInHost = getStartOffsetInHost(host, child);
            registrar.addPlace(null, "}\n", host, new TextRange(startOffsetInHost, startOffsetInHost));
        } else if (child instanceof JtePsiFor) {
            JtePsiJavaInjection javaPart = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
            injectJavaPart("for (", ") {\n", registrar, host, javaPart);

            JtePsiConditionEnd conditionEnd = PsiTreeUtil.getNextSiblingOfType(javaPart, JtePsiConditionEnd.class);
            if (conditionEnd != null) {
                for (PsiElement conditionSibling = conditionEnd.getNextSibling(); conditionSibling != null; conditionSibling = conditionSibling.getNextSibling()) {
                    processTemplateBody(conditionSibling, host, registrar);
                }
            }
        } else if (child instanceof JtePsiEndFor) {
            int startOffsetInHost = getStartOffsetInHost(host, child);
            registrar.addPlace(null, "}\n", host, new TextRange(startOffsetInHost, startOffsetInHost));
        } else if (child instanceof JtePsiTag) {
            // TODO try to call real static tag method
            JtePsiJavaInjection javaPart = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
            injectJavaPart("System.out.println(", ");\n", registrar, host, javaPart);
        }
    }

    private void injectJavaPart(String prefix, String suffix, MultiHostRegistrar registrar, JtePsiJavaContent host, PsiElement javaPart) {
        if (javaPart == null) {
            return;
        }

        int startOffsetInHost = getStartOffsetInHost(host, javaPart);
        registrar.addPlace(prefix, suffix, host, new TextRange(startOffsetInHost, startOffsetInHost + javaPart.getTextLength()));
    }

    private int getStartOffsetInHost(JtePsiJavaContent host, PsiElement node) {
        int result = node.getStartOffsetInParent();
        while (node != host) {
            node = node.getParent();
            result += node.getStartOffsetInParent();
        }
        return result;
    }

    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Collections.singletonList(JtePsiJavaContent.class);
    }
}
