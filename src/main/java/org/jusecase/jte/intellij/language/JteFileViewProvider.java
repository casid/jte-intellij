package org.jusecase.jte.intellij.language;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.LanguageSubstitutors;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.templateLanguages.ConfigurableTemplateLanguageFileViewProvider;
import com.intellij.psi.templateLanguages.TemplateDataElementType;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class JteFileViewProvider extends MultiplePsiFilesPerDocumentFileViewProvider implements ConfigurableTemplateLanguageFileViewProvider {

    private static final ConcurrentMap<String, TemplateDataElementType> TEMPLATE_DATA_TO_LANG = new ConcurrentHashMap<>();

    @NotNull
    private static Language getTemplateDataLanguage(PsiManager manager, VirtualFile file) {
        Language dataLang = TemplateDataLanguageMappings.getInstance(manager.getProject()).getMapping(file);
        if (dataLang == null) {
            dataLang = HtmlFileType.INSTANCE.getLanguage();
        }

        Language substituteLang = LanguageSubstitutors.getInstance().substituteLanguage(dataLang, file, manager.getProject());

        // only use a substituted language if it's templateable
        if (TemplateDataLanguageMappings.getTemplateableLanguages().contains(substituteLang)) {
            dataLang = substituteLang;
        }

        return dataLang;
    }

    private TemplateDataElementType getTemplateDataElementType(Language lang, Function<String, TemplateDataElementType> provider) {
        return TEMPLATE_DATA_TO_LANG.computeIfAbsent(lang.getID(), provider);
    }

    private final Language myBaseLanguage;
    private final Language myTemplateLanguage;

    public JteFileViewProvider(@NotNull PsiManager manager, @NotNull VirtualFile virtualFile, boolean eventSystemEnabled, Language language) {
        this(manager, virtualFile, eventSystemEnabled, language, getTemplateDataLanguage(manager, virtualFile));
    }

    private JteFileViewProvider(@NotNull PsiManager manager, @NotNull VirtualFile virtualFile, boolean eventSystemEnabled, Language myBaseLanguage, Language myTemplateLanguage) {
        super(manager, virtualFile, eventSystemEnabled);

        this.myBaseLanguage = myBaseLanguage;
        this.myTemplateLanguage = myTemplateLanguage;
    }

    @NotNull
    @Override
    public Language getBaseLanguage() {
        return myBaseLanguage;
    }

    @NotNull
    @Override
    public Language getTemplateDataLanguage() {
        return myTemplateLanguage;
    }

    @NotNull
    @Override
    public Set<Language> getLanguages() {
        return ContainerUtil.set(myBaseLanguage, getTemplateDataLanguage());
    }

    @NotNull
    @Override
    protected MultiplePsiFilesPerDocumentFileViewProvider cloneInner(@NotNull VirtualFile fileCopy) {
        return new JteFileViewProvider(getManager(), fileCopy, false, myBaseLanguage, myTemplateLanguage);
    }

    @Nullable
    @Override
    protected PsiFile createFile(@NotNull Language lang) {
        ParserDefinition parserDefinition = getDefinition(lang);
        if (parserDefinition == null) {
            return null;
        }

        if (lang.is(getTemplateDataLanguage())) {
            PsiFile file = parserDefinition.createFile(this);
            IElementType type = getContentElementType(lang);
            if (type != null) {
                ((PsiFileImpl) file).setContentElementType(type);
            }
            return file;
        } else if (lang.isKindOf(getBaseLanguage())) {
            return parserDefinition.createFile(this);
        }
        return null;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public IElementType getContentElementType(@NotNull Language language) {
        if (language.is(getTemplateDataLanguage())) {
            return getTemplateDataElementType(language, s -> new JteTemplateDataElementType(language));
        }
        return null;
    }

    private ParserDefinition getDefinition(Language lang) {
        if (lang.isKindOf(getBaseLanguage())) {
            return LanguageParserDefinitions.INSTANCE.forLanguage(lang.is(getBaseLanguage()) ? lang : getBaseLanguage());
        } else {
            return LanguageParserDefinitions.INSTANCE.forLanguage(lang);
        }
    }
}
