package org.jusecase.jte.intellij.language.psi;

import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PsiFileUtil {
    public static PsiDirectory resolve(@NotNull PsiDirectory rootDirectory, @NotNull String relativePathString) {
        Path relativePath = Paths.get(relativePathString);
        return resolve(rootDirectory, relativePath);
    }

    @Nullable
    public static PsiDirectory resolve(@NotNull PsiDirectory rootDirectory, @NotNull Path relativePath) {
        PsiDirectory result = rootDirectory;

        for (Path element : relativePath) {
            if ("..".equals(element.toString())) {
                result = result.getParentDirectory();
            } else {
                result = result.findSubdirectory(element.toString());
            }

            if (result == null) {
                break;
            }
        }

        return result;
    }
}
