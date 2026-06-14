package org.jusecase.jte.intellij.language.k2;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.psi.KtClass;
import org.jusecase.jte.intellij.language.KteLanguage;
import org.jusecase.jte.intellij.language.psi.JtePsiElseIf;
import org.jusecase.jte.intellij.language.psi.JtePsiFor;
import org.jusecase.jte.intellij.language.psi.JtePsiIf;
import org.jusecase.jte.intellij.language.psi.JtePsiImport;
import org.jusecase.jte.intellij.language.psi.JtePsiJavaInjection;
import org.jusecase.jte.intellij.language.psi.JtePsiOutput;
import org.jusecase.jte.intellij.language.psi.JtePsiParam;
import org.jusecase.jte.intellij.language.psi.JtePsiStatement;
import org.jusecase.jte.intellij.language.psi.JtePsiTemplate;
import org.jusecase.jte.intellij.language.psi.KtePsiFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class KteSyntheticKotlinReferenceContributor extends PsiReferenceContributor {
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final Set<String> KEYWORDS = Set.of(
            "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in", "interface",
            "is", "null", "object", "package", "return", "super", "this", "throw", "true", "try", "typealias",
            "typeof", "val", "var", "when", "while"
    );

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(psiElement(JtePsiJavaInjection.class), new SyntheticKotlinReferenceProvider());
    }

    private static final class SyntheticKotlinReferenceProvider extends PsiReferenceProvider {
        private static final List<Class<? extends PsiElement>> SUPPORTED_KOTLIN_FRAGMENT_PARENTS = List.of(
                JtePsiOutput.class,
                JtePsiIf.class,
                JtePsiElseIf.class,
                JtePsiFor.class,
                JtePsiStatement.class,
                JtePsiTemplate.class
        );

        @Override
        public @NotNull PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                         @NotNull ProcessingContext context) {
            if (!(element instanceof JtePsiJavaInjection injection) || !isSupportedKteInjection(injection)) {
                return PsiReference.EMPTY_ARRAY;
            }

            List<PsiReference> result = new ArrayList<>();
            Matcher matcher = IDENTIFIER.matcher(injection.getText());
            while (matcher.find()) {
                String identifier = matcher.group();
                if (!KEYWORDS.contains(identifier)) {
                    result.add(new SyntheticKotlinReference(injection, new TextRange(matcher.start(), matcher.end())));
                }
            }

            return result.toArray(PsiReference.EMPTY_ARRAY);
        }

        private boolean isSupportedKteInjection(@NotNull JtePsiJavaInjection injection) {
            PsiFile containingFile = injection.getContainingFile();
            if (!(containingFile instanceof KtePsiFile) &&
                    !(containingFile.getViewProvider().getPsi(KteLanguage.INSTANCE) instanceof KtePsiFile)) {
                return false;
            }

            return PsiTreeUtil.getParentOfType(injection, JtePsiImport.class, false) != null ||
                    PsiTreeUtil.getParentOfType(injection, JtePsiParam.class, false) != null ||
                    isSupportedKotlinFragment(injection);
        }

        private boolean isSupportedKotlinFragment(@NotNull JtePsiJavaInjection injection) {
            for (Class<? extends PsiElement> parentClass : SUPPORTED_KOTLIN_FRAGMENT_PARENTS) {
                if (PsiTreeUtil.getParentOfType(injection, parentClass, false) != null) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class SyntheticKotlinReference extends PsiReferenceBase<JtePsiJavaInjection> {
        private SyntheticKotlinReference(@NotNull JtePsiJavaInjection element, @NotNull TextRange rangeInElement) {
            super(element, rangeInElement, false);
        }

        @Override
        public @NotNull String getCanonicalText() {
            return identifierText();
        }

        @Override
        public @Nullable PsiElement resolve() {
            PsiFile templateFile = myElement.getContainingFile();
            return KteSyntheticKotlinSemanticService.getInstance(templateFile.getProject())
                    .resolveReferenceAtTemplateRange(myElement, getRangeInElement());
        }

        @NotNull
        private String identifierText() {
            return myElement.getText().substring(
                    getRangeInElement().getStartOffset(),
                    getRangeInElement().getEndOffset()
            );
        }

        @Override
        public boolean isReferenceTo(@NotNull PsiElement element) {
            try {
                PsiElement resolved = resolve();
                if (resolved == null) {
                    return false;
                }

                return resolved.getManager().areElementsEquivalent(resolved, element) ||
                        resolved.getManager().areElementsEquivalent(resolved.getNavigationElement(), element.getNavigationElement());
            } catch (ProcessCanceledException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                return false;
            }
        }

        @Override
        public @NotNull PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
            return replaceRangeText(getRangeInElement(), newElementName);
        }

        @Override
        public @NotNull PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
            if (PsiTreeUtil.getParentOfType(getElement(), JtePsiImport.class, false) != null &&
                    element instanceof PsiClass psiClass &&
                    psiClass.getQualifiedName() != null) {
                return replaceRangeText(TextRange.from(0, getElement().getTextLength()), psiClass.getQualifiedName());
            }

            if (PsiTreeUtil.getParentOfType(getElement(), JtePsiImport.class, false) != null &&
                    element instanceof KtClass ktClass &&
                    ktClass.getFqName() != null) {
                return replaceRangeText(TextRange.from(0, getElement().getTextLength()), ktClass.getFqName().asString());
            }

            if (element instanceof PsiNamedElement namedElement && namedElement.getName() != null) {
                return handleElementRename(namedElement.getName());
            }

            return getElement();
        }

        @NotNull
        private PsiElement replaceRangeText(@NotNull TextRange rangeInElement, @NotNull String newText) throws IncorrectOperationException {
            PsiFile containingFile = getElement().getContainingFile();
            Document document = PsiDocumentManager.getInstance(getElement().getProject()).getDocument(containingFile);
            if (document == null) {
                throw new IncorrectOperationException("Cannot update .kte reference without a document");
            }

            TextRange elementRange = getElement().getTextRange();
            int startOffset = elementRange.getStartOffset() + rangeInElement.getStartOffset();
            int endOffset = elementRange.getStartOffset() + rangeInElement.getEndOffset();
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(getElement().getProject());
            if (documentManager.isDocumentBlockedByPsi(document)) {
                documentManager.doPostponedOperationsAndUnblockDocument(document);
            }

            document.replaceString(startOffset, endOffset, newText);
            documentManager.commitDocument(document);
            return getElement();
        }
    }
}
