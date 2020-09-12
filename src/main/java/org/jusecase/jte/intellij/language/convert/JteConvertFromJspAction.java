package org.jusecase.jte.intellij.language.convert;

import com.intellij.execution.PsiLocation;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.junit2.info.MethodLocation;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class JteConvertFromJspAction extends AnAction {

    public static final String ERROR_TITLE = "JSP Converter Error";
    public static final String CONVERTER_BASE_CLASS = "gg.jte.convert.jsp.JspToJteConverter";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null) {
            return;
        }

        Project project = psiFile.getProject();

        PsiClass converterBaseClass = JavaPsiFacade.getInstance(project).findClass(CONVERTER_BASE_CLASS, GlobalSearchScope.everythingScope(project));
        if (converterBaseClass == null) {
            JteConvertNotification.error(project, ERROR_TITLE, "Could not locate class '" + CONVERTER_BASE_CLASS + "' on the classpath. You're probably missing the jte-jsp-converter dependency.");
            return;
        }

        PsiClass converterClass = ClassInheritorsSearch.search(converterBaseClass).findFirst();
        if (converterClass == null) {
            JteConvertNotification.error(project, ERROR_TITLE, "Could not locate a class implementing '" + CONVERTER_BASE_CLASS + "'. You need to implement it, to customize JSP conversion for your specific project.");
            return;
        }

        @NotNull PsiMethod[] mainMethods = converterClass.findMethodsByName("main", false);
        if (mainMethods.length == 0) {
            JteConvertNotification.error(project, ERROR_TITLE, "You need to create a main method in '" + converterClass.getQualifiedName() + "'. In the main method you need to init the converter and customize JSP conversion for your specific project.");
            return;
        }

        MethodLocation location = new MethodLocation(project, mainMethods[0], new PsiLocation<>(converterClass));
        ConfigurationContext configurationContext = ConfigurationContext.createEmptyContextForLocation(location);

        ApplicationConfiguration configuration = (ApplicationConfiguration)Objects.requireNonNull(configurationContext.getConfiguration()).getConfiguration();
        configuration.setProgramParameters(psiFile.getOriginalFile().getVirtualFile().getPath());

        ExecutionUtil.runConfiguration(Objects.requireNonNull(configurationContext.getConfiguration()), DefaultDebugExecutor.getDebugExecutorInstance());
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null) {
            return;
        }

        e.getPresentation().setEnabledAndVisible("JSP".equals(psiFile.getFileType().getName()));
    }
}
