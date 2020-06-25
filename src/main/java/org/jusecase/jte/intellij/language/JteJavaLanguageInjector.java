package org.jusecase.jte.intellij.language;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.psi.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class JteJavaLanguageInjector implements MultiHostInjector {
    public static final Key<PsiJavaFile> JAVA_FILE_KEY = Key.create("JteJavaLanguageInjector.PsiJavaFile");

    private static final List<? extends Class<? extends PsiElement>> ELEMENTS = Arrays.asList(
            JtePsiJavaContent.class,
            JtePsiExtraJavaInjection.class
    );

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        if (context instanceof JtePsiJavaContent) {
            JtePsiJavaContent host = (JtePsiJavaContent) context;
            new Injector(host, registrar).inject();
        } else if (context instanceof JtePsiExtraJavaInjection) {
            JtePsiJavaInjection param = PsiTreeUtil.getPrevSiblingOfType(context, JtePsiJavaInjection.class);
            if (param != null) {
                registrar.startInjecting(StdFileTypes.JAVA.getLanguage());
                registrar.addPlace("class Dummy{" + param.getText() + "=", ";}", (JtePsiExtraJavaInjection) context, new TextRange(0, context.getTextLength()));
                registrar.doneInjecting();
            }
        }
    }

    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return ELEMENTS;
    }

    private static class Injector {
        private final JtePsiJavaContent host;
        private final MultiHostRegistrar registrar;

        private boolean hasWrittenClass;
        private boolean hasStartedInjection;

        public Injector(JtePsiJavaContent host, MultiHostRegistrar registrar) {
            this.host = host;
            this.registrar = registrar;
        }

        public void inject() {
            for (PsiElement child : host.getChildren()) {
                if (child instanceof JtePsiImport) {
                    JtePsiJavaInjection part = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
                    if (part != null) {
                        injectJavaPart("import ", ";\n", part);
                    }
                } else if (child instanceof JtePsiParam) {
                    JtePsiJavaInjection part = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
                    if (part != null) {
                        if (!hasWrittenClass) {
                            String classPrefix = "class DummyTemplate { public void render(";
                            JtePsiParam nextParam = PsiTreeUtil.getNextSiblingOfType(child, JtePsiParam.class);
                            if (nextParam != null) {
                                injectJavaPart(classPrefix, " ", part);
                            } else {
                                injectJavaPart(classPrefix, ") {\n", part);
                            }
                            hasWrittenClass = true;
                        } else {
                            JtePsiParam nextParam = PsiTreeUtil.getNextSiblingOfType(child, JtePsiParam.class);
                            if (nextParam != null) {
                                injectJavaPart(", ", " ", part);
                            } else {
                                injectJavaPart(", ", ") {\n", part);
                            }
                        }
                    }
                } else {
                    processTemplateBody(child);
                }
            }

            if (hasWrittenClass) {
                getRegistrar().addPlace(null, "\n}}", host, new TextRange(host.getTextLength(), host.getTextLength()));
            }

            if (hasStartedInjection) {
                getRegistrar().doneInjecting();

                try {
                    Field resultFiles = getRegistrar().getClass().getDeclaredField("resultFiles");
                    resultFiles.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<PsiFile> files = (List<PsiFile>) resultFiles.get(getRegistrar());
                    PsiJavaFile injectedFile = (PsiJavaFile) files.get(0);

                    host.getContainingFile().putUserData(JAVA_FILE_KEY, injectedFile);
                } catch (Exception e) {
                    // noop
                }
            }
        }

        private void processTemplateBody(PsiElement child) {
            if (child instanceof JtePsiOutput) {
                JtePsiJavaInjection part = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
                injectJavaPart("System.out.print(", ");\n", part);
            } else if (child instanceof JtePsiStatement) {
                JtePsiJavaInjection part = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
                injectJavaPart(null, "\n", part);
            } else if (child instanceof JtePsiIf) {
                JtePsiJavaInjection part = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
                injectJavaPart("if (", ") {\n", part);

                if (part != null) {
                    for (PsiElement sibling = part.getNextSibling(); sibling != null; sibling = sibling.getNextSibling()) {
                        processTemplateBody(sibling);
                    }
                }
            } else if (child instanceof JtePsiElseIf) {
                JtePsiJavaInjection part = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
                injectJavaPart("} elseif (", ") {\n", part);
            } else if (child instanceof JtePsiElse) {
                injectEmptyJavaPart("\n} else {\n", null, child);
            } else if (child instanceof JtePsiEndIf) {
                injectEmptyJavaPart(null, "}\n", child);
            } else if (child instanceof JtePsiFor) {
                JtePsiJavaInjection part = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
                injectJavaPart("for (", ") {\n", part);

                if (part != null) {
                    for (PsiElement sibling = part.getNextSibling(); sibling != null; sibling = sibling.getNextSibling()) {
                        processTemplateBody(sibling);
                    }
                }
            } else if (child instanceof JtePsiEndFor) {
                injectEmptyJavaPart(null, "}\n", child);
            } else if (child instanceof JtePsiTag) {
                injectTagOrLayoutParams(child);
            } else if (child instanceof JtePsiLayout) {
                JtePsiJavaInjection part = injectTagOrLayoutParams(child);
                if (part != null) {
                    for (PsiElement sibling = part.getNextSibling(); sibling != null; sibling = sibling.getNextSibling()) {
                        processTemplateBody(sibling);
                    }
                }
            } else if (child instanceof JtePsiDefine) {
                JtePsiDefineName name = PsiTreeUtil.getChildOfType(child, JtePsiDefineName.class);

                if (name != null) {
                    for (PsiElement sibling = name.getNextSibling(); sibling != null; sibling = sibling.getNextSibling()) {
                        processTemplateBody(sibling);
                    }
                }
            }
        }

        private JtePsiJavaInjection injectTagOrLayoutParams(PsiElement child) {
            JtePsiJavaInjection result = null;

            for (PsiElement element : child.getChildren()) {
                if (element instanceof JtePsiJavaInjection) {
                    injectJavaPart("System.out.print(", ");\n", result = (JtePsiJavaInjection) element);
                }
            }

            return result;
        }

        private void injectEmptyJavaPart(String prefix, String suffix, @NotNull PsiElement child) {
            int startOffsetInHost = getStartOffsetInHost(child);
            getRegistrar().addPlace(prefix, suffix, host, new TextRange(startOffsetInHost, startOffsetInHost));
        }

        private void injectJavaPart(String prefix, String suffix, JtePsiJavaInjection part) {
            if (part == null) {
                return;
            }

            int startOffsetInHost = getStartOffsetInHost(part);
            getRegistrar().addPlace(prefix, suffix, host, new TextRange(startOffsetInHost, startOffsetInHost + part.getTextLength()));
        }

        public MultiHostRegistrar getRegistrar() {
            if (!hasStartedInjection) {
                registrar.startInjecting(StdFileTypes.JAVA.getLanguage());
                hasStartedInjection = true;
            }
            return registrar;
        }

        private int getStartOffsetInHost(PsiElement node) {
            int result = node.getStartOffsetInParent();
            while (node != host) {
                node = node.getParent();
                result += node.getStartOffsetInParent();
            }
            return result;
        }
    }
}
