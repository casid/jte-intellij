package org.jusecase.kte.intellij.language;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.kotlin.idea.KotlinFileType;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.idea.UserDataModuleInfoKt;
import org.jetbrains.kotlin.psi.KtFile;
import org.jusecase.kte.intellij.language.psi.*;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class KteKotlinLanguageInjector implements MultiHostInjector {
    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        if (context instanceof KtePsiKotlinContent) {
            KtePsiKotlinContent host = (KtePsiKotlinContent)context;
            new Injector(host, registrar).inject();
        }
    }

    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Collections.singletonList(KtePsiKotlinContent.class);
    }

    private static class Injector {
        private final KtePsiKotlinContent host;
        private final MultiHostRegistrar registrar;

        private boolean hasWrittenClass;
        private boolean hasWrittenPackage;
        private boolean hasStartedInjection;

        public Injector(KtePsiKotlinContent host, MultiHostRegistrar registrar) {
            this.host = host;
            this.registrar = registrar;
        }

        public void inject() {
            for (PsiElement child : host.getChildren()) {
                if (child instanceof KtePsiImport) {
                    KtePsiKotlinInjection part = PsiTreeUtil.getChildOfType(child, KtePsiKotlinInjection.class);
                    if (part != null) {
                        if (!hasWrittenPackage) {
                            injectKotlinPart("package template.support\nimport ", "\n", part);
                            hasWrittenPackage = true;
                        } else {
                            injectKotlinPart("import ", "\n", part);
                        }
                    }
                } else if (child instanceof KtePsiParam) {
                    KtePsiKotlinInjection part = PsiTreeUtil.getChildOfType(child, KtePsiKotlinInjection.class);
                    if (part != null) {
                        if (!hasWrittenClass) {
                            String classPrefix = "object DummyTemplate {\nfun render(";
                            KtePsiParam nextParam = PsiTreeUtil.getNextSiblingOfType(child, KtePsiParam.class);
                            if (nextParam != null) {
                                injectKotlinPart(classPrefix, null, part);
                            } else {
                                injectKotlinPart(classPrefix, ") {\n", part);
                            }
                            hasWrittenClass = true;
                        } else {
                            KtePsiParam nextParam = PsiTreeUtil.getNextSiblingOfType(child, KtePsiParam.class);
                            if (nextParam != null) {
                                injectKotlinPart(", ", null, part);
                            } else {
                                injectKotlinPart(", ", ") {\n", part);
                            }
                        }
                    }
                } else {
                    processTemplateBody(child);
                }
            }

            if (hasWrittenClass) {
                getRegistrar().addPlace(null, "\n}\n}", host, new TextRange(host.getTextLength(), host.getTextLength()));
            }

            if (hasStartedInjection) {
                getRegistrar().doneInjecting();

                try {
                    Field resultFiles = getRegistrar().getClass().getDeclaredField("resultFiles");
                    resultFiles.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<PsiFile> files = (List<PsiFile>)resultFiles.get(getRegistrar());
                    KtFile injectedFile= (KtFile)files.get(0);
                    injectedFile.putUserData(UserDataModuleInfoKt.MODULE_ROOT_TYPE_KEY, JavaSourceRootType.SOURCE);
                }
                catch ( Exception e ) {
                    // noop
                }
            }
        }

        private void processTemplateBody(PsiElement child) {
            if (child instanceof KtePsiOutput) {
                KtePsiKotlinInjection part = PsiTreeUtil.getChildOfType(child, KtePsiKotlinInjection.class);
                injectKotlinPart("print(", ")\n", part);
            } else if (child instanceof KtePsiStatement) {
                KtePsiKotlinInjection part = PsiTreeUtil.getChildOfType(child, KtePsiKotlinInjection.class);
                injectKotlinPart(null, "\n", part);
            } else if (child instanceof KtePsiIf) {
                KtePsiKotlinInjection part = PsiTreeUtil.getChildOfType(child, KtePsiKotlinInjection.class);
                injectKotlinPart("if (", ") {\n", part);

                KtePsiConditionEnd conditionEnd = PsiTreeUtil.getNextSiblingOfType(part, KtePsiConditionEnd.class);
                if (conditionEnd != null) {
                    for (PsiElement conditionSibling = conditionEnd.getNextSibling(); conditionSibling != null; conditionSibling = conditionSibling.getNextSibling()) {
                        processTemplateBody(conditionSibling);
                    }
                }
            } else if (child instanceof KtePsiElseIf) {
                KtePsiKotlinInjection part = PsiTreeUtil.getChildOfType(child, KtePsiKotlinInjection.class);
                injectKotlinPart("} elseif (", ") {\n", part);
            } else if (child instanceof KtePsiElse) {
                injectEmptyKotlinPart("\n} else {\n", null, child);
            } else if (child instanceof KtePsiEndIf) {
                injectEmptyKotlinPart(null, "}\n", child);
            } else if (child instanceof KtePsiFor) {
                KtePsiKotlinInjection part = PsiTreeUtil.getChildOfType(child, KtePsiKotlinInjection.class);
                injectKotlinPart("for (", ") {\n", part);

                KtePsiConditionEnd conditionEnd = PsiTreeUtil.getNextSiblingOfType(part, KtePsiConditionEnd.class);
                if (conditionEnd != null) {
                    for (PsiElement conditionSibling = conditionEnd.getNextSibling(); conditionSibling != null; conditionSibling = conditionSibling.getNextSibling()) {
                        processTemplateBody(conditionSibling);
                    }
                }
            } else if (child instanceof KtePsiEndFor) {
                injectEmptyKotlinPart(null, "}\n", child);
            } else if (child instanceof KtePsiTag) {
                // TODO try to call real static tag method
                KtePsiKotlinInjection part = PsiTreeUtil.getChildOfType(child, KtePsiKotlinInjection.class);
                injectKotlinPart("print(", ")\n", part);
            }
        }

        private void injectEmptyKotlinPart(String prefix, String suffix, @NotNull PsiElement child) {
            int startOffsetInHost = getStartOffsetInHost(child);
            getRegistrar().addPlace(prefix, suffix, host, new TextRange(startOffsetInHost, startOffsetInHost));
        }

        private void injectKotlinPart(String prefix, String suffix, KtePsiKotlinInjection kotlinPart) {
            if (kotlinPart == null) {
                return;
            }

            int startOffsetInHost = getStartOffsetInHost(kotlinPart);
            getRegistrar().addPlace(prefix, suffix, host, new TextRange(startOffsetInHost, startOffsetInHost + kotlinPart.getTextLength()));
        }

        public MultiHostRegistrar getRegistrar() {
            if (!hasStartedInjection) {
                registrar.startInjecting(KotlinLanguage.INSTANCE, KotlinFileType.EXTENSION);
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
