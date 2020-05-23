package org.jusecase.kte.intellij.language.parsing;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jusecase.kte.intellij.language.KteLanguage;

public interface KteTokenTypes {
    IElementType HTML_CONTENT = new KteElementType("HTML_CONTENT");
    IElementType KOTLIN_CONTENT = new KteElementType("KOTLIN_CONTENT");

    IElementType KOTLIN_INJECTION = new KteElementType("KOTLIN_INJECTION");

    IElementType OUTER_ELEMENT_TYPE = new KteElementType("OUTER_ELEMENT_TYPE");

    IElementType IMPORT = new KteElementType("IMPORT");
    IElementType PARAM = new KteElementType("PARAM");
    IElementType OUTPUT = new KteElementType("OUTPUT");
    IElementType OUTPUT_BEGIN = new KteElementType("OUTPUT_BEGIN");
    IElementType OUTPUT_END = new KteElementType("OUTPUT_END");
    IElementType STATEMENT = new KteElementType("STATEMENT");
    IElementType STATEMENT_BEGIN = new KteElementType("STATEMENT_BEGIN");
    IElementType STATEMENT_END = new KteElementType("STATEMENT_END");

    IElementType IF = new KteElementType("IF");
    IElementType CONDITION_BEGIN = new KteElementType("CONDITION_BEGIN");
    IElementType CONDITION_END = new KteElementType("CONDITION_END");
    IElementType ENDIF = new KteElementType("ENDIF");
    IElementType ELSE = new KteElementType("ELSE");
    IElementType ELSEIF = new KteElementType("ELSEIF");
    IElementType FOR = new KteElementType("FOR");
    IElementType ENDFOR = new KteElementType("ENDFOR");

    IElementType TAG = new KteElementType("TAG");
    IElementType TAG_NAME = new KteElementType("TAG_NAME");
    IElementType NAME_SEPARATOR = new KteElementType("NAME_SEPARATOR");
    IElementType PARAMS_BEGIN = new KteElementType("PARAMS_BEGIN");
    IElementType PARAMS_END = new KteElementType("PARAMS_END");
    IElementType LAYOUT = new KteElementType("LAYOUT");
    IElementType LAYOUT_NAME = new KteElementType("LAYOUT_NAME");
    IElementType DEFINE = new KteElementType("DEFINE");
    IElementType DEFINE_NAME = new KteElementType("DEFINE_NAME");
    IElementType ENDDEFINE = new KteElementType("ENDDEFINE");
    IElementType RENDER = new KteElementType("RENDER");
    IElementType RENDER_NAME = new KteElementType("RENDER_NAME");
    IElementType ENDLAYOUT = new KteElementType("ENDLAYOUT");

    IElementType COMMENT = new KteElementType("COMMENT");
    IElementType COMMENT_CONTENT = new KteElementType("COMMENT_CONTENT");

    IElementType WHITESPACE = new KteElementType("WHITESPACE");

    IElementType STRING = new KteElementType("STRING");

    IFileElementType FILE = new IStubFileElementType<>("FILE", KteLanguage.INSTANCE);

    TokenSet COMMENTS = TokenSet.create(COMMENT, COMMENT_CONTENT);
    TokenSet STRING_LITERALS = TokenSet.create(STRING);
}
