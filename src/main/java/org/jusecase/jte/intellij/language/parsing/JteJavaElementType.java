package org.jusecase.jte.intellij.language.parsing;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.JteLanguage;

public class JteJavaElementType extends IElementType {
    public JteJavaElementType(@NotNull String debugName) {
        super(debugName, JteLanguage.INSTANCE);
    }

    // TODO see https://intellij-support.jetbrains.com/hc/en-us/community/posts/360004357879-Different-behavior-between-LanguageInjector-and-MultiHostInjector
    // TODO and https://github.com/JetBrains/intellij-plugins/blob/a2fd262a4f4efc47841612509038550dcc6afad1/AngularJS/src/org/angular2/lang/Angular2EmbeddedContentTokenType.java#L39
}
