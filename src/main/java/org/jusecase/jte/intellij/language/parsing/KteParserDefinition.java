package org.jusecase.jte.intellij.language.parsing;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.psi.*;

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

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return KteTokenTypes.WHITESPACES;
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode node) {
        IElementType elementType = node.getElementType();

        if (elementType == KteTokenTypes.JAVA_CONTENT) {
            return new KtePsiJavaContent(node);
        } else if (elementType == KteTokenTypes.JAVA_INJECTION) {
            return new JtePsiJavaInjection(node);
        } else if (elementType == KteTokenTypes.PARAM) {
            return new JtePsiParam(node);
        } else if (elementType == KteTokenTypes.IMPORT) {
            return new JtePsiImport(node);
        } else if (elementType == KteTokenTypes.OUTPUT) {
            return new JtePsiOutput(node);
        } else if (elementType == KteTokenTypes.STATEMENT) {
            return new JtePsiStatement(node);
        } else if (elementType == KteTokenTypes.CONDITION_BEGIN) {
            return new JtePsiConditionBegin(node);
        } else if (elementType == KteTokenTypes.CONDITION_END) {
            return new JtePsiConditionEnd(node);
        } else if (elementType == KteTokenTypes.IF) {
            return new JtePsiIf(node);
        } else if (elementType == KteTokenTypes.ELSE) {
            return new JtePsiElse(node);
        } else if (elementType == KteTokenTypes.ELSEIF) {
            return new JtePsiElseIf(node);
        } else if (elementType == KteTokenTypes.ENDIF) {
            return new JtePsiEndIf(node);
        } else if (elementType == KteTokenTypes.FOR) {
            return new JtePsiFor(node);
        } else if (elementType == KteTokenTypes.ENDFOR) {
            return new JtePsiEndFor(node);
        } else if (elementType == KteTokenTypes.TEMPLATE) {
            return new JtePsiTemplate(node);
        } else if (elementType == KteTokenTypes.TEMPLATE_NAME) {
            return new JtePsiTemplateName(node, ".kte");
        } else if (elementType == KteTokenTypes.PARAMS_BEGIN) {
            return new JtePsiParamsBegin(node);
        } else if (elementType == KteTokenTypes.PARAMS_END) {
            return new JtePsiParamsEnd(node);
        } else if (elementType == KteTokenTypes.NAME_SEPARATOR) {
            return new JtePsiNameSeparator(node);
        } else if (elementType == KteTokenTypes.OUTPUT_BEGIN) {
            return new JtePsiOutputBegin(node);
        } else if (elementType == KteTokenTypes.OUTPUT_END) {
            return new JtePsiOutputEnd(node);
        } else if (elementType == KteTokenTypes.STATEMENT_BEGIN) {
            return new JtePsiStatementBegin(node);
        } else if (elementType == KteTokenTypes.STATEMENT_END) {
            return new JtePsiStatementEnd(node);
        } else if (elementType == KteTokenTypes.COMMENT) {
            return new JtePsiComment(node);
        } else if (elementType == KteTokenTypes.EQUALS) {
            return new JtePsiEquals(node);
        } else if (elementType == KteTokenTypes.EXTRA_JAVA_INJECTION) {
            return new JtePsiExtraJavaInjection(node);
        } else if (elementType == KteTokenTypes.COMMA) {
            return new JtePsiComma(node);
        } else if (elementType == KteTokenTypes.PARAM_NAME) {
            return new KtePsiParamName(node);
        } else if (elementType == KteTokenTypes.CONTENT_BEGIN) {
            return new JtePsiContent(node);
        } else if (elementType == KteTokenTypes.CONTENT_END) {
            return new JtePsiEndContent(node);
        } else if (elementType == KteTokenTypes.BLOCK) {
            return new JtePsiBlock(node);
        }

        return new JtePsiElement(node);
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
