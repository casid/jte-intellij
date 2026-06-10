package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.kotlin.analysis.api.platform.projectStructure.KaDanglingFileModuleImpl;
import org.jetbrains.kotlin.analysis.api.projectStructure.ContextModuleKt;
import org.jetbrains.kotlin.analysis.api.projectStructure.DanglingFilesKt;
import org.jetbrains.kotlin.analysis.api.projectStructure.KaDanglingFileResolutionMode;
import org.jetbrains.kotlin.analysis.api.projectStructure.KaModule;
import org.jetbrains.kotlin.analysis.api.projectStructure.KaModuleProvider;
import org.jetbrains.kotlin.idea.base.projectStructure.ApiKt;
import org.jetbrains.kotlin.idea.base.projectStructure.KotlinProjectStructureCustomizationUtils;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtPsiFactoryKt;

import java.util.List;

final class KteSyntheticKotlinModuleContext {
    private KteSyntheticKotlinModuleContext() {
    }

    static void configure(@NotNull Project project,
                          @NotNull KtFile ktFile,
                          @Nullable PsiElement analysisContext) {
        // Boundary for K2 project-structure hooks: synthetic .kte files must analyze as source in the real module.
        KotlinProjectStructureCustomizationUtils.setCustomSourceRootType(ktFile, JavaSourceRootType.SOURCE);
        if (analysisContext == null) {
            return;
        }

        KaModule contextModule = contextModule(project, analysisContext);
        if (ktFile.getVirtualFile() != null) {
            ContextModuleKt.setAnalysisContextModule(ktFile.getVirtualFile(), contextModule);
        }
        KtPsiFactoryKt.setAnalysisContext(ktFile, analysisContext);
        DanglingFilesKt.setContextModule(ktFile, contextModule);
        setPreferSelfDanglingModule(ktFile, contextModule);
    }

    @NotNull
    private static KaModule contextModule(@NotNull Project project, @NotNull PsiElement analysisContext) {
        KaModule contextModule = KaModuleProvider.Companion.getInstance(project).getModule(analysisContext, null);
        Module openApiModule = ModuleUtilCore.findModuleForPsiElement(analysisContext);
        KaModule sourceModule = openApiModule == null ? null : ApiKt.toKaSourceModuleForProductionOrTest(openApiModule);
        return sourceModule == null ? contextModule : sourceModule;
    }

    private static void setPreferSelfDanglingModule(@NotNull KtFile ktFile, @NotNull KaModule contextModule) {
        // Native completion needs the synthetic file as use-site; the real module only supplies dependencies.
        KaDanglingFileModuleImpl module = new KaDanglingFileModuleImpl(
                List.of(ktFile),
                contextModule,
                KaDanglingFileResolutionMode.PREFER_SELF
        );
        try {
            DanglingFilesKt.setExplicitModule(ktFile, module);
        } catch (IllegalArgumentException ignored) {
            // Physical synthetic files already have a project module; in-memory completion copies accept this module.
        }
    }
}
