package org.jusecase.jte.intellij.language.parsing;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jusecase.jte.intellij.language.JteLanguage;

public interface JteTokenTypes {
    IElementType HTML_CONTENT = new JteElementType("HTML_CONTENT");
    IElementType JAVA_CONTENT = new JteElementType("JAVA_CONTENT");

    IElementType JAVA_INJECTION = new JteElementType("JAVA_INJECTION");
    IElementType EXTRA_JAVA_INJECTION = new JteElementType("EXTRA_JAVA_INJECTION");

    IElementType OUTER_ELEMENT_TYPE = new JteElementType("OUTER_ELEMENT_TYPE");

    IElementType IMPORT = new JteElementType("IMPORT");
    IElementType PARAM = new JteElementType("PARAM");
    IElementType OUTPUT = new JteElementType("OUTPUT");
    IElementType OUTPUT_BEGIN = new JteElementType("OUTPUT_BEGIN");
    IElementType OUTPUT_END = new JteElementType("OUTPUT_END");
    IElementType STATEMENT = new JteElementType("STATEMENT");
    IElementType STATEMENT_BEGIN = new JteElementType("STATEMENT_BEGIN");
    IElementType STATEMENT_END = new JteElementType("STATEMENT_END");

    IElementType IF = new JteElementType("IF");
    IElementType CONDITION_BEGIN = new JteElementType("CONDITION_BEGIN");
    IElementType CONDITION_END = new JteElementType("CONDITION_END");
    IElementType ENDIF = new JteElementType("ENDIF");
    IElementType ELSE = new JteElementType("ELSE");
    IElementType ELSEIF = new JteElementType("ELSEIF");
    IElementType FOR = new JteElementType("FOR");
    IElementType ENDFOR = new JteElementType("ENDFOR");

    IElementType TAG = new JteElementType("TAG");
    IElementType TAG_NAME = new JteElementType("TAG_NAME");
    IElementType NAME_SEPARATOR = new JteElementType("NAME_SEPARATOR");
    IElementType PARAMS_BEGIN = new JteElementType("PARAMS_BEGIN");
    IElementType PARAM_NAME = new JteElementType("PARAM_NAME");
    IElementType PARAMS_END = new JteElementType("PARAMS_END");
    IElementType LAYOUT = new JteElementType("LAYOUT");
    IElementType LAYOUT_NAME = new JteElementType("LAYOUT_NAME");
    IElementType DEFINE = new JteElementType("DEFINE");
    IElementType DEFINE_NAME = new JteElementType("DEFINE_NAME");
    IElementType ENDDEFINE = new JteElementType("ENDDEFINE");
    IElementType RENDER = new JteElementType("RENDER");
    IElementType RENDER_NAME = new JteElementType("RENDER_NAME");
    IElementType ENDLAYOUT = new JteElementType("ENDLAYOUT");
    IElementType CONTENT_BEGIN = new JteElementType("CONTENT_BEGIN");
    IElementType CONTENT_END = new JteElementType("CONTENT_END");

    IElementType COMMENT = new JteElementType("COMMENT");
    IElementType COMMENT_CONTENT = new JteElementType("COMMENT_CONTENT");

    IElementType WHITESPACE = new JteElementType("WHITESPACE");
    IElementType EQUALS = new JteElementType("EQUALS");
    IElementType COMMA = new JteElementType("COMMA");

    IElementType STRING = new JteElementType("STRING");

    IFileElementType FILE = new IStubFileElementType<>("FILE", JteLanguage.INSTANCE);

    TokenSet COMMENTS = TokenSet.create(COMMENT, COMMENT_CONTENT);
    TokenSet STRING_LITERALS = TokenSet.create(STRING);
}
