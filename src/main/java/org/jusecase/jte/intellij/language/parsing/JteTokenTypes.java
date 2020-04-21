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

    IElementType LINE_BREAK = new JteElementType("LINE_BREAK");
    IElementType OUTER_ELEMENT_TYPE = new JteElementType("OUTER_ELEMENT_TYPE");
    IElementType OUTER_JAVA_ELEMENT_TYPE = new JteElementType("OUTER_JAVA_ELEMENT_TYPE");

    IElementType IMPORT = new JteElementType("IMPORT");
    IElementType PARAM = new JteElementType("PARAM");
    IElementType OUTPUT = new JteElementType("OUTPUT");
    IElementType OUTPUT_BEGIN = new JteElementType("OUTPUT_BEGIN");
    IElementType OUTPUT_END = new JteElementType("OUTPUT_END");

    IElementType IF = new JteElementType("IF");
    IElementType CONDITION_BEGIN = new JteElementType("CONDITION_BEGIN");
    IElementType CONDITION_END = new JteElementType("CONDITION_END");
    IElementType ENDIF = new JteElementType("ENDIF");

    IElementType COMMENT = new JteElementType("COMMENT");
    IElementType COMMENT_CONTENT = new JteElementType("COMMENT_CONTENT");

    IElementType WHITESPACE = new JteElementType("WHITESPACE");

    IElementType STRING = new JteElementType("STRING");

    IFileElementType FILE = new IStubFileElementType<>("FILE", JteLanguage.INSTANCE);

    TokenSet COMMENTS = TokenSet.create(COMMENT, COMMENT_CONTENT);
    TokenSet STRING_LITERALS = TokenSet.create(STRING);
}
