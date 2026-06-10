package org.jusecase.jte.intellij.language.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JtePsiParam extends JtePsiElement implements PsiNamedElement {
    private static final Pattern KOTLIN_PARAM_NAME =
            Pattern.compile("^\\s*(?:vararg\\s+)?([A-Za-z_][A-Za-z0-9_]*)\\s*:");
    private static final Pattern JAVA_PARAM_NAME =
            Pattern.compile("^\\s*(?:@[A-Za-z_][A-Za-z0-9_.]*(?:\\([^\\n]*\\))?\\s+)*(?:final\\s+)?[A-Za-z_][A-Za-z0-9_.$<>?,\\s\\[\\]]+\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*(?:=.*)?$");

    public JtePsiParam(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    public String getName() {
        ParamNameRange nameRange = paramNameRange();
        return nameRange == null ? null : nameRange.name();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        ParamNameRange nameRange = paramNameRange();
        JtePsiJavaInjection injection = PsiTreeUtil.getChildOfType(this, JtePsiJavaInjection.class);
        if (nameRange == null || injection == null) {
            throw new IncorrectOperationException("Cannot rename malformed template parameter");
        }

        Document document = PsiDocumentManager.getInstance(getProject()).getDocument(getContainingFile());
        if (document == null) {
            throw new IncorrectOperationException("Cannot rename template parameter without a document");
        }

        TextRange elementRange = injection.getTextRange();
        int startOffset = elementRange.getStartOffset() + nameRange.range().getStartOffset();
        int endOffset = elementRange.getStartOffset() + nameRange.range().getEndOffset();
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(getProject());
        if (documentManager.isDocumentBlockedByPsi(document)) {
            documentManager.doPostponedOperationsAndUnblockDocument(document);
        }

        document.replaceString(startOffset, endOffset, name);
        documentManager.commitDocument(document);
        return this;
    }

    @NotNull
    @Override
    public SearchScope getUseScope() {
        return GlobalSearchScope.projectScope(getProject());
    }

    @Nullable
    private ParamNameRange paramNameRange() {
        JtePsiJavaInjection injection = PsiTreeUtil.getChildOfType(this, JtePsiJavaInjection.class);
        if (injection == null) {
            return null;
        }

        String text = injection.getText();
        ParamNameRange kotlinName = matchName(text, KOTLIN_PARAM_NAME);
        return kotlinName != null ? kotlinName : matchName(text, JAVA_PARAM_NAME);
    }

    @Nullable
    private ParamNameRange matchName(@NotNull String text, @NotNull Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return null;
        }

        TextRange range = TextRange.create(matcher.start(1), matcher.end(1));
        return new ParamNameRange(matcher.group(1), range);
    }

    private record ParamNameRange(@NotNull String name, @NotNull TextRange range) {
    }
}
