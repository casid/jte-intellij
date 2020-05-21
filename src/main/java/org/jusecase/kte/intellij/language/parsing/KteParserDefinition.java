package org.jusecase.kte.intellij.language.parsing;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jusecase.kte.intellij.language.psi.*;

public class KteParserDefinition implements ParserDefinition {

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new KteLexer();
    }

    @Override
    public PsiParser createParser(Project project) {
        return new KteParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return KteTokenTypes.FILE;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return KteTokenTypes.COMMENTS;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return KteTokenTypes.STRING_LITERALS;
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode node) {
        IElementType elementType = node.getElementType();

        if (elementType == KteTokenTypes.KOTLIN_CONTENT) {
            return new KtePsiKotlinContent(node);
        } else if (elementType == KteTokenTypes.KOTLIN_INJECTION) {
            return new KtePsiKotlinInjection(node);
        } else if (elementType == KteTokenTypes.PARAM) {
            return new KtePsiParam(node);
        } else if (elementType == KteTokenTypes.IMPORT) {
            return new KtePsiImport(node);
        } else if (elementType == KteTokenTypes.OUTPUT) {
            return new KtePsiOutput(node);
        } else if (elementType == KteTokenTypes.STATEMENT) {
            return new KtePsiStatement(node);
        } else if (elementType == KteTokenTypes.CONDITION_BEGIN) {
            return new KtePsiConditionBegin(node);
        } else if (elementType == KteTokenTypes.CONDITION_END) {
            return new KtePsiConditionEnd(node);
        } else if (elementType == KteTokenTypes.IF) {
            return new KtePsiIf(node);
        } else if (elementType == KteTokenTypes.ELSE) {
            return new KtePsiElse(node);
        } else if (elementType == KteTokenTypes.ELSEIF) {
            return new KtePsiElseIf(node);
        } else if (elementType == KteTokenTypes.ENDIF) {
            return new KtePsiEndIf(node);
        } else if (elementType == KteTokenTypes.FOR) {
            return new KtePsiFor(node);
        } else if (elementType == KteTokenTypes.ENDFOR) {
            return new KtePsiEndFor(node);
        } else if (elementType == KteTokenTypes.TAG) {
            return new KtePsiTag(node);
        } else if (elementType == KteTokenTypes.TAG_NAME) {
            return new KtePsiTagName(node);
        } else if (elementType == KteTokenTypes.PARAMS_BEGIN) {
            return new KtePsiParamsBegin(node);
        } else if (elementType == KteTokenTypes.PARAMS_END) {
            return new KtePsiParamsEnd(node);
        } else if (elementType == KteTokenTypes.LAYOUT) {
            return new KtePsiLayout(node);
        } else if (elementType == KteTokenTypes.LAYOUT_NAME) {
            return new KtePsiLayoutName(node);
        } else if (elementType == KteTokenTypes.ENDLAYOUT) {
            return new KtePsiEndLayout(node);
        }

        return new KtePsiElement(node);
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new KtePsiFile(viewProvider);
    }

    @Override
    public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }
}
