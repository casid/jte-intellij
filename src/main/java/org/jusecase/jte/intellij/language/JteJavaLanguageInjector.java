package org.jusecase.jte.intellij.language;

import com.intellij.codeInsight.folding.impl.FoldingUpdate;
import com.intellij.ide.highlighter.JavaFileType;
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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.psi.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JteJavaLanguageInjector implements MultiHostInjector {
    public static final Key<PsiJavaFile> JAVA_FILE_KEY = Key.create("JteJavaLanguageInjector.PsiJavaFile");
    private static final List<? extends Class<? extends PsiElement>> ELEMENTS = Arrays.asList(
            JtePsiJavaContent.class,
            JtePsiExtraJavaInjection.class
    );
    private static Key<Object> LAST_UPDATE_INJECTED_STAMP_KEY;

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        if (context instanceof JtePsiJavaContent) {
            JtePsiJavaContent host = (JtePsiJavaContent) context;
            new Injector(host, registrar, false).inject();
        } else if (context instanceof JtePsiExtraJavaInjection) {
            JtePsiExtraJavaInjection host = (JtePsiExtraJavaInjection) context;

            JtePsiJavaInjection param = PsiTreeUtil.getPrevSiblingOfType(context, JtePsiJavaInjection.class);
            if (param != null) {
                JtePsiContent content = PsiTreeUtil.findChildOfType(host, JtePsiContent.class);
                if (content == null) {
                    registrar.startInjecting(JavaFileType.INSTANCE.getLanguage());
                    registrar.addPlace("class Dummy{" + param.getText() + "=", ";}", host, new TextRange(0, context.getTextLength()));
                } else {
                    new Injector(host, registrar, true).injectContent("class Dummy{ gg.jte.TemplateOutput jteOutput;" + param.getText() + "=", ";}", content);
                }
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
        private static final String CLASS_PREFIX = "@SuppressWarnings(\"Convert2Lambda\")\nclass DummyTemplate { gg.jte.TemplateOutput jteOutput; void render(";

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
                        injectJavaPartWithoutClassCheck("import ", ";\n", part);
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
                getRegistrar().addPlace(null, "\n}\nvoid dummyCall(Object ... o) {}\n}", host, new TextRange(host.getTextLength(), host.getTextLength()));
            }

            if (hasStartedInjection) {
                preventJavaFolding();

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
                if (fileEditor.getFile() == null || !fileEditor.getFile().getName().endsWith(".jte")) {
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
                injectContentAwareJavaPart("jteOutput.writeUserContent(", ");\n", child);
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
                injectTagOrLayoutParams(child);
            } else if (child instanceof JtePsiBlock) {
                for (PsiElement element : child.getChildren()) {
                    processTemplateBody(element);
                }
            }
        }

        private void injectTagOrLayoutParams(PsiElement child) {
            injectContentAwareJavaPart("dummyCall(", ");\n", child);
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
            // Super ugly hack: We do not override writeTo(TemplateOutput), otherwise line markers for override will be generated and cause an assertion error!
            prefix = (prefix == null ? "" : prefix) + "new gg.jte.Content() { void writeTo() {";
            suffix = "}}" + (suffix == null ? "" : suffix);

            JtePsiBlock block = PsiTreeUtil.getChildOfType(element, JtePsiBlock.class);

            if (block == null || block.getChildren().length == 0) {
                injectEmptyJavaPart(prefix, suffix, element);
            } else {
                injectEmptyJavaPart(prefix, null, element);

                processTemplateBody(block);

                JtePsiEndContent endContent = PsiTreeUtil.findChildOfType(element, JtePsiEndContent.class);
                if (endContent != null) {
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
                registrar.startInjecting(JavaFileType.INSTANCE.getLanguage());
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
