package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootModificationTracker;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public final class KteSyntheticKotlinModelService {
    private static final Key<CachedValue<KteSyntheticKotlinModel>> MODEL_KEY =
            Key.create("jte.kte.synthetic.kotlin.model");

    private final Project project;

    public KteSyntheticKotlinModelService(@NotNull Project project) {
        this.project = project;
    }

    @NotNull
    public static KteSyntheticKotlinModelService getInstance(@NotNull Project project) {
        return project.getService(KteSyntheticKotlinModelService.class);
    }

    @NotNull
    public KteSyntheticKotlinModel getModel(@NotNull PsiFile templateFile) {
        return CachedValuesManager.getManager(project).getCachedValue(
                templateFile,
                MODEL_KEY,
                () -> CachedValueProvider.Result.create(
                        buildModel(templateFile),
                        PsiModificationTracker.getInstance(project),
                        ProjectRootModificationTracker.getInstance(project)
                ),
                false
        );
    }

    @Nullable
    public PhysicalFile writePhysicalSyntheticFile(@NotNull PsiFile templateFile) throws IOException {
        VirtualFile templateVirtualFile = templateFile.getVirtualFile();
        if (templateVirtualFile == null || templateVirtualFile.getParent() == null) {
            return null;
        }

        KteSyntheticKotlinModel model = getModel(templateFile);
        KteSyntheticKotlinAnalysisContextService contextService =
                KteSyntheticKotlinAnalysisContextService.getInstance(project);
        VirtualFile fallbackParent = templateVirtualFile.getParent();
        WriteResult result = new WriteResult();
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                VirtualFile parent = model.getSourceRoot() == null
                        ? fallbackParent
                        : contextService.resolveSyntheticDirectory(model.getSourceRoot());
                VirtualFile syntheticVirtualFile = parent.findChild(model.getSyntheticFile().getFileName());
                if (syntheticVirtualFile == null) {
                    syntheticVirtualFile = parent.createChildData(this, model.getSyntheticFile().getFileName());
                }

                VfsUtil.saveText(syntheticVirtualFile, model.getSyntheticFile().getText());
                result.virtualFile = syntheticVirtualFile;
            } catch (IOException exception) {
                result.exception = exception;
            }
        });

        if (result.exception != null) {
            throw result.exception;
        }

        return result.virtualFile == null ? null : new PhysicalFile(model, result.virtualFile);
    }

    @NotNull
    private KteSyntheticKotlinModel buildModel(@NotNull PsiFile templateFile) {
        KteSyntheticKotlinAnalysisContextService contextService =
                KteSyntheticKotlinAnalysisContextService.getInstance(project);
        KteSyntheticKotlinFile syntheticFile = new KteSyntheticKotlinFileBuilder().build(templateFile);
        VirtualFile sourceRoot = contextService.findModuleSourceRoot(templateFile);
        return new KteSyntheticKotlinModel(
                syntheticFile,
                KteSyntheticKotlinPsiFactory.createKtFile(project, syntheticFile,
                        contextService.findAnalysisContext(templateFile, sourceRoot)),
                sourceRoot
        );
    }

    public record PhysicalFile(@NotNull KteSyntheticKotlinModel model, @NotNull VirtualFile virtualFile) {
    }

    private static final class WriteResult {
        private VirtualFile virtualFile;
        private IOException exception;
    }
}
