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

public class JteParserDefinition implements ParserDefinition {

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new JteLexer();
    }

    @Override
    public PsiParser createParser(Project project) {
        return new JteParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return JteTokenTypes.FILE;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return JteTokenTypes.COMMENTS;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return JteTokenTypes.STRING_LITERALS;
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return JteTokenTypes.WHITESPACES;
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode node) {
        IElementType elementType = node.getElementType();

        if (elementType == JteTokenTypes.JAVA_CONTENT) {
            return new JtePsiJavaContent(node);
        } else if (elementType == JteTokenTypes.JAVA_INJECTION) {
            return new JtePsiJavaInjection(node);
        } else if (elementType == JteTokenTypes.PARAM) {
            return new JtePsiParam(node);
        } else if (elementType == JteTokenTypes.IMPORT) {
            return new JtePsiImport(node);
        } else if (elementType == JteTokenTypes.OUTPUT) {
            return new JtePsiOutput(node);
        } else if (elementType == JteTokenTypes.STATEMENT) {
            return new JtePsiStatement(node);
        } else if (elementType == JteTokenTypes.CONDITION_BEGIN) {
            return new JtePsiConditionBegin(node);
        } else if (elementType == JteTokenTypes.CONDITION_END) {
            return new JtePsiConditionEnd(node);
        } else if (elementType == JteTokenTypes.IF) {
            return new JtePsiIf(node);
        } else if (elementType == JteTokenTypes.ELSE) {
            return new JtePsiElse(node);
        } else if (elementType == JteTokenTypes.ELSEIF) {
            return new JtePsiElseIf(node);
        } else if (elementType == JteTokenTypes.ENDIF) {
            return new JtePsiEndIf(node);
        } else if (elementType == JteTokenTypes.FOR) {
            return new JtePsiFor(node);
        } else if (elementType == JteTokenTypes.ENDFOR) {
            return new JtePsiEndFor(node);
        } else if (elementType == JteTokenTypes.TEMPLATE) {
            return new JtePsiTemplate(node);
        } else if (elementType == JteTokenTypes.TEMPLATE_NAME) {
            return new JtePsiTemplateName(node, ".jte");
        } else if (elementType == JteTokenTypes.PARAMS_BEGIN) {
            return new JtePsiParamsBegin(node);
        } else if (elementType == JteTokenTypes.PARAMS_END) {
            return new JtePsiParamsEnd(node);
        } else if (elementType == JteTokenTypes.NAME_SEPARATOR) {
            return new JtePsiNameSeparator(node);
        } else if (elementType == JteTokenTypes.OUTPUT_BEGIN) {
            return new JtePsiOutputBegin(node);
        } else if (elementType == JteTokenTypes.OUTPUT_END) {
            return new JtePsiOutputEnd(node);
        } else if (elementType == JteTokenTypes.STATEMENT_BEGIN) {
            return new JtePsiStatementBegin(node);
        } else if (elementType == JteTokenTypes.STATEMENT_END) {
            return new JtePsiStatementEnd(node);
        } else if (elementType == JteTokenTypes.COMMENT) {
            return new JtePsiComment(node);
        } else if (elementType == JteTokenTypes.EQUALS) {
            return new JtePsiEquals(node);
        } else if (elementType == JteTokenTypes.EXTRA_JAVA_INJECTION) {
            return new JtePsiExtraJavaInjection(node);
        } else if (elementType == JteTokenTypes.COMMA) {
            return new JtePsiComma(node);
        } else if (elementType == JteTokenTypes.PARAM_NAME) {
            return new JtePsiParamName(node);
        } else if (elementType == JteTokenTypes.CONTENT_BEGIN) {
            return new JtePsiContent(node);
        } else if (elementType == JteTokenTypes.CONTENT_END) {
            return new JtePsiEndContent(node);
        } else if (elementType == JteTokenTypes.BLOCK) {
            return new JtePsiBlock(node);
        }

        return new JtePsiElement(node);
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new JtePsiFile(viewProvider);
    }

    @Override
    public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }
}
