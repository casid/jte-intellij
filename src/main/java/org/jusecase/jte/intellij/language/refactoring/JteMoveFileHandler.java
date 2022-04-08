package org.jusecase.jte.intellij.language.refactoring;

import com.intellij.lang.*;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.DummyHolder;
import com.intellij.psi.impl.source.DummyHolderFactory;
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFileHandler;
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFilesOrDirectoriesUtil;
import com.intellij.refactoring.util.MoveRenameUsageInfo;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;
import org.jusecase.jte.intellij.language.psi.*;

import java.util.*;

public class JteMoveFileHandler extends MoveFileHandler {

    @Override
    public boolean canProcessElement(PsiFile element) {
        return element instanceof JtePsiFile || element instanceof KtePsiFile;
    }

    @Override
    public void prepareMovedFile(PsiFile file, PsiDirectory moveDestination, Map<PsiElement, PsiElement> oldToNewMap) {
        VirtualFile dstDir = moveDestination.getVirtualFile();

        final PsiDirectory containingDirectory = file.getContainingDirectory();
        if (!Comparing.equal(dstDir, containingDirectory != null ? containingDirectory.getVirtualFile() : null)) {
            MoveFilesOrDirectoriesUtil.doMoveFile(file, moveDestination);
        }
    }

    @Override
    public @Nullable List<UsageInfo> findUsages(PsiFile psiFile, PsiDirectory newParent, boolean searchInComments, boolean searchInNonJavaFiles) {
        List<UsageInfo> result = new ArrayList<>();

        Set<PsiReference> foundReferences = new HashSet<>();

        for (PsiReference reference : ReferencesSearch.search(psiFile, GlobalSearchScope.projectScope(psiFile.getProject()), false)) {
            if (foundReferences.contains(reference)) {
                continue;
            }
            TextRange range = reference.getRangeInElement();
            result.add(new MoveRenameUsageInfo(reference.getElement(), reference, range.getStartOffset(), range.getEndOffset(), psiFile, false));
            foundReferences.add(reference);
        }

        return result;
    }

    @Override
    public void retargetUsages(List<UsageInfo> usageInfos, Map<PsiElement, PsiElement> oldToNewMap) {
        for (UsageInfo usage : usageInfos) {
            if (usage instanceof MoveRenameUsageInfo) {
                retargetUsage((MoveRenameUsageInfo) usage);
            }
        }
    }

    private void retargetUsage(MoveRenameUsageInfo usage) {
        PsiReference reference = usage.getReference();
        if (reference == null) {
            return;
        }

        if (!(usage.getReferencedElement() instanceof PsiFile)) {
            return;
        }
        PsiFile newFile = (PsiFile) usage.getReferencedElement();

        if (!(reference.getElement() instanceof JtePsiTemplateName)) {
            return;
        }
        JtePsiTemplateName templateName = (JtePsiTemplateName) reference.getElement();

        PsiDirectory rootDirectory = templateName.findRootDirectory();
        if (rootDirectory == null) {
            return;
        }

        PsiElement template = reference.getElement().getParent();
        if (!(template instanceof JtePsiTemplate)) {
            return;
        }

        JtePsiTemplateName firstTemplateName = PsiTreeUtil.getChildOfType(template, JtePsiTemplateName.class);
        JtePsiTemplateName lastTemplateName = JtePsiUtil.getLastChildOfType(template, JtePsiTemplateName.class);
        if (firstTemplateName == null || lastTemplateName == null) {
            return;
        }

        String newTemplateLocation = getNewTemplateLocation(newFile, rootDirectory);

        JtePsiTemplate tempTemplate = createNewDummyTemplateNode(template, newTemplateLocation);
        JtePsiTemplateName firstTempTemplateName = PsiTreeUtil.getChildOfType(tempTemplate, JtePsiTemplateName.class);
        JtePsiTemplateName lastTempTemplateName = JtePsiUtil.getLastChildOfType(tempTemplate, JtePsiTemplateName.class);

        if (firstTempTemplateName != null && lastTempTemplateName != null) {
            template.addRangeBefore(firstTempTemplateName, lastTempTemplateName, firstTemplateName);
            template.deleteChildRange(firstTemplateName, lastTemplateName);
        }
    }

    @Nullable
    private JtePsiTemplate createNewDummyTemplateNode(PsiElement template, String newTemplateLocation) {
        String text = "@template." + newTemplateLocation + "()";
        Project project = template.getProject();
        PsiManager psiManager = PsiManager.getInstance(project);
        DummyHolder dummyHolder = DummyHolderFactory.createHolder(psiManager, null);
        ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(template.getLanguage());
        Lexer lexer = parserDefinition.createLexer(project);
        PsiBuilder psiBuilder = PsiBuilderFactory.getInstance().createBuilder(project, dummyHolder.getTreeElement(), lexer, template.getLanguage(), text);
        ASTNode node = parserDefinition.createParser(project).parse(JteTokenTypes.HTML_CONTENT, psiBuilder);

        dummyHolder.getTreeElement().rawAddChildren((TreeElement) node);

        CodeEditUtil.setNodeGeneratedRecursively(node, true);

        return PsiTreeUtil.findChildOfType(dummyHolder, JtePsiTemplate.class);
    }

    @NotNull
    private String getNewTemplateLocation(PsiFile newFile, PsiDirectory rootDirectory) {
        List<String> names = new ArrayList<>();

        names.add(newFile.getVirtualFile().getNameWithoutExtension());

        PsiDirectory parent = newFile.getParent();
        while (parent != null && !parent.equals(rootDirectory)) {
            names.add(parent.getName());
            parent = parent.getParent();
        }

        Collections.reverse(names);
        return String.join(".", names);
    }

    @Override
    public void updateMovedFile(PsiFile file) throws IncorrectOperationException {
        // Nothing to do
    }
}
