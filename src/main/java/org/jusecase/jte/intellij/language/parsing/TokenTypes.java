package org.jusecase.jte.intellij.language.parsing;

import com.intellij.psi.tree.*;

public interface TokenTypes {
    IElementType HTML_CONTENT();
    IElementType JAVA_CONTENT();
    IElementType BLOCK();

    IElementType JAVA_INJECTION();
    IElementType EXTRA_JAVA_INJECTION();

    IElementType OUTER_ELEMENT_TYPE();

    IElementType IMPORT();
    IElementType PARAM();
    IElementType OUTPUT();
    IElementType OUTPUT_BEGIN();
    IElementType OUTPUT_END();
    IElementType STATEMENT();
    IElementType STATEMENT_BEGIN();
    IElementType STATEMENT_END();

    IElementType IF();
    IElementType CONDITION_BEGIN();
    IElementType CONDITION_END();
    IElementType ENDIF();
    IElementType ELSE();
    IElementType ELSEIF();
    IElementType FOR();
    IElementType ENDFOR();

    IElementType TEMPLATE();
    IElementType TEMPLATE_NAME();
    IElementType NAME_SEPARATOR();
    IElementType PARAMS_BEGIN();
    IElementType PARAM_NAME();
    IElementType PARAMS_END();
    IElementType CONTENT_BEGIN();
    IElementType CONTENT_END();

    IElementType COMMENT();
    IElementType COMMENT_CONTENT();

    IElementType WHITESPACE();
    IElementType EQUALS();
    IElementType COMMA();

    IElementType STRING();

    IFileElementType FILE();

    TokenSet COMMENTS();
    TokenSet STRING_LITERALS();
    TokenSet WHITESPACES();
}
