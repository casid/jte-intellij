package org.jusecase.jte.intellij.language;

import com.intellij.codeInsight.folding.impl.FoldingUpdate;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.idea.UserDataModuleInfoKt;
import org.jetbrains.kotlin.psi.KtFile;
import org.jusecase.jte.intellij.language.psi.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class KteKotlinLanguageInjector implements MultiHostInjector {
    public static final Key<KtFile> KOTLIN_FILE_KEY = Key.create("KteJavaLanguageInjector.KtFile");
    private static final List<? extends Class<? extends PsiElement>> ELEMENTS = Collections.singletonList(KtePsiJavaContent.class);
    private static Key<Object> LAST_UPDATE_INJECTED_STAMP_KEY;

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        KtePsiJavaContent host = (KtePsiJavaContent) context;
        new Injector(host, registrar, false).inject();
    }

    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return ELEMENTS;
    }

    private static class Injector {
        private static final String CLASS_PREFIX = "class DummyTemplate { val jteOutput = gg.jte.TemplateOutput()\n fun render(";

        private final PsiLanguageInjectionHost host;
        private final MultiHostRegistrar registrar;

        private boolean hasWrittenClass;
        private boolean hasStartedInjection;

        public Injector(PsiLanguageInjectionHost host, MultiHostRegistrar registrar, boolean hasWrittenClass) {
            this.host = host;
            this.registrar = registrar;
            this.hasWrittenClass = hasWrittenClass;
        }

        public void inject() {
            for (PsiElement child : host.getChildren()) {
                if (child instanceof JtePsiImport) {
                    JtePsiJavaInjection part = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
                    if (part != null) {
                        injectJavaPartWithoutClassCheck("import ", "\n", part);
                    }
                } else if (child instanceof JtePsiParam) {
                    JtePsiJavaInjection part = PsiTreeUtil.getChildOfType(child, JtePsiJavaInjection.class);
                    if (part != null) {
                        if (!hasWrittenClass) {
                            JtePsiParam nextParam = PsiTreeUtil.getNextSiblingOfType(child, JtePsiParam.class);
                            if (nextParam != null) {
                                injectJavaPartWithoutClassCheck(CLASS_PREFIX, " ", part);
                            } else {
                                injectJavaPartWithoutClassCheck(CLASS_PREFIX, ") {\n", part);
                            }
                            hasWrittenClass = true;
                        } else {
                            JtePsiParam nextParam = PsiTreeUtil.getNextSiblingOfType(child, JtePsiParam.class);
                            if (nextParam != null) {
                                injectJavaPartWithoutClassCheck(", ", " ", part);
                            } else {
                                injectJavaPartWithoutClassCheck(", ", ") {\n", part);
                            }
                        }
                    }
                } else {
                    processTemplateBody(child);
                }
            }

            if (hasWrittenClass) {
                getRegistrar().addPlace(null, "\n}\nfun dummyCall(varargs o:Object) {}\n}", host, new TextRange(host.getTextLength(), host.getTextLength()));
            }

            if (hasStartedInjection) {
                preventJavaFolding();

                getRegistrar().doneInjecting();

                try {
                    Field resultFiles = getRegistrar().getClass().getDeclaredField("resultFiles");
                    resultFiles.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<PsiFile> files = (List<PsiFile>) resultFiles.get(getRegistrar());
                    KtFile injectedFile = (KtFile) files.get(0);

                    //Module module = ProjectFileIndex.getInstance(host.getProject()).getModuleForFile(host.getContainingFile().getVirtualFile());
                    //injectedFile.putUserData(ModuleUtilCore.KEY_MODULE, module);
                    injectedFile.clearCaches();
                    injectedFile.putUserData(UserDataModuleInfoKt.MODULE_ROOT_TYPE_KEY, JavaSourceRootType.SOURCE);
                    injectedFile.clearCaches();


                    List<PsiReference[]> psiReferences = SyntaxTraverser
                            .psiTraverser(injectedFile)
                            .traverse()
                            .transform(ReferenceProvidersRegistry::getReferencesFromProviders)
                            .filter(Objects::nonNull)
                            .filter(it -> it.length > 0)
                            .toList();

                    host.getContainingFile().putUserData(KOTLIN_FILE_KEY, injectedFile);
                } catch (Exception e) {
                    // noop
                }
            }
        }

        private void preventJavaFolding() {
            // Super ugly hack to prevent folding for injected java code - we set the current document timestamp, so that folding thinks it was already done.
            if (LAST_UPDATE_INJECTED_STAMP_KEY == null) {
                try {
                    Field keyField = FoldingUpdate.class.getDeclaredField("LAST_UPDATE_INJECTED_STAMP_KEY");
                    keyField.setAccessible(true);

                    //noinspection unchecked
                    LAST_UPDATE_INJECTED_STAMP_KEY = (Key<Object>) keyField.get(null);
                } catch (Exception e) {
                    // noop
                }
            }

            VirtualFile virtualFile = host.getContainingFile().getVirtualFile();
            if (virtualFile == null) {
                return;
            }

            FileEditor[] editors = FileEditorManager.getInstance(host.getProject()).getAllEditors(virtualFile);
            for (FileEditor fileEditor : editors) {
                if (fileEditor.getFile() == null || !fileEditor.getFile().getName().endsWith(".kte")) {
                    continue;
                }

                if (fileEditor instanceof TextEditor) {
                    TextEditor textEditor = (TextEditor) fileEditor;
                    Editor editor = textEditor.getEditor();
                    Document document = editor.getDocument();
                    editor.putUserData(LAST_UPDATE_INJECTED_STAMP_KEY, document.getModificationStamp());
                }
            }
        }

        private void processTemplateBody(PsiElement child) {
            if (child instanceof JtePsiOutput) {
                injectContentAwareJavaPart("jteOutput.writeUserContent(", ")\n", child);
            } else if (child instanceof JtePsiStatement) {
                injectContentAwareJavaPart(null, "\n", child);
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
                injectJavaPart("} else if (", ") {\n", part);
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
            } else if (child instanceof JtePsiTemplate) {
                injectTemplateParams(child);
            } else if (child instanceof JtePsiBlock) {
                for (PsiElement element : child.getChildren()) {
                    processTemplateBody(element);
                }
            }
        }

        private void injectTemplateParams(PsiElement child) {
            injectContentAwareJavaPart("dummyCall(", ")\n", child);
        }

        private void injectContentAwareJavaPart(String prefix, String suffix, PsiElement child) {
            List<PsiElement> children = Arrays.stream(child.getChildren()).filter(c -> c instanceof JtePsiJavaInjection || c instanceof JtePsiContent).collect(Collectors.toList());

            boolean prefixWritten = false;
            PsiElement last = children.isEmpty() ? null : children.get(children.size() - 1);
            for (PsiElement element : children) {
                String currentPrefix = null;
                if (!prefixWritten) {
                    currentPrefix = prefix;
                    prefixWritten = true;
                }

                String currentSuffix = null;
                if (element == last) {
                    currentSuffix = suffix;
                } else if (element.getNextSibling() instanceof JtePsiComma) {
                    currentSuffix = ",";
                }

                if (element instanceof JtePsiJavaInjection) {
                    injectJavaPart(currentPrefix, currentSuffix, (JtePsiJavaInjection) element);
                } else if (element instanceof JtePsiContent) {
                    injectContent(currentPrefix, currentSuffix, (JtePsiContent) element);
                }
            }
        }

        void injectContent(String prefix, String suffix, JtePsiContent element) {
            prefix = (prefix == null ? "" : prefix) + "object : gg.jte.Content { override fun writeTo() {";
            suffix = "}}" + (suffix == null ? "" : suffix);

            JtePsiBlock block = PsiTreeUtil.getChildOfType(element, JtePsiBlock.class);

            if (block == null || block.getChildren().length == 0) {
                injectEmptyJavaPart(prefix, suffix, element);
            } else {
                injectEmptyJavaPart(prefix, null, element);

                processTemplateBody(block);

                PsiElement endContent = element.getLastChild();
                if (endContent instanceof JtePsiEndContent) {
                    injectEmptyJavaPart(null, suffix, endContent);
                }
            }
        }

        private void injectEmptyJavaPart(String prefix, String suffix, @NotNull PsiElement child) {
            int startOffsetInHost = getStartOffsetInHost(child);
            getRegistrar().addPlace(prefix, suffix, host, new TextRange(startOffsetInHost, startOffsetInHost));
        }

        private void injectJavaPart(String prefix, String suffix, JtePsiJavaInjection part) {
            if (part == null) {
                return;
            }

            if (!hasWrittenClass) {
                String classPrefix = CLASS_PREFIX + ") {\n";
                if (prefix == null) {
                    prefix = classPrefix;
                } else {
                    prefix = classPrefix + prefix;
                }
                hasWrittenClass = true;
            }

            injectJavaPartWithoutClassCheck(prefix, suffix, part);
        }

        private void injectJavaPartWithoutClassCheck(String prefix, String suffix, JtePsiJavaInjection part) {
            if (part == null) {
                return;
            }

            int startOffsetInHost = getStartOffsetInHost(part);
            getRegistrar().addPlace(prefix, suffix, host, new TextRange(startOffsetInHost, startOffsetInHost + part.getTextLength()));
        }

        public MultiHostRegistrar getRegistrar() {
            if (!hasStartedInjection) {
                registrar.startInjecting(KotlinLanguage.INSTANCE);
                hasStartedInjection = true;
            }
            return registrar;
        }

        private int getStartOffsetInHost(PsiElement node) {
            int result = node.getStartOffsetInParent();
            while (node != host) {
                node = node.getParent();
                if (node != host) {
                    result += node.getStartOffsetInParent();
                }
            }
            return result;
        }
    }
}
