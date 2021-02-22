package org.jusecase.jte.intellij.language.parsing;

import com.intellij.psi.tree.*;
import org.jusecase.jte.intellij.language.KteLanguage;

public class KteTokenTypes implements TokenTypes {
    public static final KteTokenTypes INSTANCE = new KteTokenTypes();

    public static final IElementType HTML_CONTENT = new KteElementType("HTML_CONTENT");
    public static final IElementType JAVA_CONTENT = new KteElementType("JAVA_CONTENT");
    public static final IElementType BLOCK = new KteElementType("BLOCK");

    public static final IElementType JAVA_INJECTION = new KteElementType("JAVA_INJECTION");
    public static final IElementType EXTRA_JAVA_INJECTION = new KteElementType("EXTRA_JAVA_INJECTION");

    public static final IElementType OUTER_ELEMENT_TYPE = new OuterLanguageElementType("OUTER_ELEMENT_TYPE", KteLanguage.INSTANCE);

    public static final IElementType IMPORT = new KteElementType("IMPORT");
    public static final IElementType PARAM = new KteElementType("PARAM");
    public static final IElementType OUTPUT = new KteElementType("OUTPUT");
    public static final IElementType OUTPUT_BEGIN = new KteElementType("OUTPUT_BEGIN");
    public static final IElementType OUTPUT_END = new KteElementType("OUTPUT_END");
    public static final IElementType STATEMENT = new KteElementType("STATEMENT");
    public static final IElementType STATEMENT_BEGIN = new KteElementType("STATEMENT_BEGIN");
    public static final IElementType STATEMENT_END = new KteElementType("STATEMENT_END");

    public static final IElementType IF = new KteElementType("IF");
    public static final IElementType CONDITION_BEGIN = new KteElementType("CONDITION_BEGIN");
    public static final IElementType CONDITION_END = new KteElementType("CONDITION_END");
    public static final IElementType ENDIF = new KteElementType("ENDIF");
    public static final IElementType ELSE = new KteElementType("ELSE");
    public static final IElementType ELSEIF = new KteElementType("ELSEIF");
    public static final IElementType FOR = new KteElementType("FOR");
    public static final IElementType ENDFOR = new KteElementType("ENDFOR");

    public static final IElementType TAG = new KteElementType("TAG");
    public static final IElementType TAG_NAME = new KteElementType("TAG_NAME");
    public static final IElementType NAME_SEPARATOR = new KteElementType("NAME_SEPARATOR");
    public static final IElementType PARAMS_BEGIN = new KteElementType("PARAMS_BEGIN");
    public static final IElementType PARAM_NAME = new KteElementType("PARAM_NAME");
    public static final IElementType PARAMS_END = new KteElementType("PARAMS_END");
    public static final IElementType LAYOUT = new KteElementType("LAYOUT");
    public static final IElementType CONTENT_BEGIN = new KteElementType("CONTENT_BEGIN");
    public static final IElementType CONTENT_END = new KteElementType("CONTENT_END");

    public static final IElementType COMMENT = new KteElementType("COMMENT");
    public static final IElementType COMMENT_CONTENT = new KteElementType("COMMENT_CONTENT");

    public static final IElementType WHITESPACE = new KteElementType("WHITESPACE");
    public static final IElementType EQUALS = new KteElementType("EQUALS");
    public static final IElementType COMMA = new KteElementType("COMMA");

    public static final IElementType STRING = new KteElementType("STRING");

    public static final IFileElementType FILE = new IStubFileElementType<>("FILE", KteLanguage.INSTANCE);

    public static final TokenSet COMMENTS = TokenSet.create(COMMENT, COMMENT_CONTENT);
    public static final TokenSet STRING_LITERALS = TokenSet.create(STRING);
    public static final TokenSet WHITESPACES = TokenSet.create(WHITESPACE);


    @Override
    public IElementType HTML_CONTENT() {
        return HTML_CONTENT;
    }

    @Override
    public IElementType JAVA_CONTENT() {
        return JAVA_CONTENT;
    }

    @Override
    public IElementType BLOCK() {
        return BLOCK;
    }

    @Override
    public IElementType JAVA_INJECTION() {
        return JAVA_INJECTION;
    }

    @Override
    public IElementType EXTRA_JAVA_INJECTION() {
        return EXTRA_JAVA_INJECTION;
    }

    @Override
    public IElementType OUTER_ELEMENT_TYPE() {
        return OUTER_ELEMENT_TYPE;
    }

    @Override
    public IElementType IMPORT() {
        return IMPORT;
    }

    @Override
    public IElementType PARAM() {
        return PARAM;
    }

    @Override
    public IElementType OUTPUT() {
        return OUTPUT;
    }

    @Override
    public IElementType OUTPUT_BEGIN() {
        return OUTPUT_BEGIN;
    }

    @Override
    public IElementType OUTPUT_END() {
        return OUTPUT_END;
    }

    @Override
    public IElementType STATEMENT() {
        return STATEMENT;
    }

    @Override
    public IElementType STATEMENT_BEGIN() {
        return STATEMENT_BEGIN;
    }

    @Override
    public IElementType STATEMENT_END() {
        return STATEMENT_END;
    }

    @Override
    public IElementType IF() {
        return IF;
    }

    @Override
    public IElementType CONDITION_BEGIN() {
        return CONDITION_BEGIN;
    }

    @Override
    public IElementType CONDITION_END() {
        return CONDITION_END;
    }

    @Override
    public IElementType ENDIF() {
        return ENDIF;
    }

    @Override
    public IElementType ELSE() {
        return ELSE;
    }

    @Override
    public IElementType ELSEIF() {
        return ELSEIF;
    }

    @Override
    public IElementType FOR() {
        return FOR;
    }

    @Override
    public IElementType ENDFOR() {
        return ENDFOR;
    }

    @Override
    public IElementType TAG() {
        return TAG;
    }

    @Override
    public IElementType TAG_NAME() {
        return TAG_NAME;
    }

    @Override
    public IElementType NAME_SEPARATOR() {
        return NAME_SEPARATOR;
    }

    @Override
    public IElementType PARAMS_BEGIN() {
        return PARAMS_BEGIN;
    }

    @Override
    public IElementType PARAM_NAME() {
        return PARAM_NAME;
    }

    @Override
    public IElementType PARAMS_END() {
        return PARAMS_END;
    }

    @Override
    public IElementType LAYOUT() {
        return LAYOUT;
    }

    @Override
    public IElementType CONTENT_BEGIN() {
        return CONTENT_BEGIN;
    }

    @Override
    public IElementType CONTENT_END() {
        return CONTENT_END;
    }

    @Override
    public IElementType COMMENT() {
        return COMMENT;
    }

    @Override
    public IElementType COMMENT_CONTENT() {
        return COMMENT_CONTENT;
    }

    @Override
    public IElementType WHITESPACE() {
        return WHITESPACE;
    }

    @Override
    public IElementType EQUALS() {
        return EQUALS;
    }

    @Override
    public IElementType COMMA() {
        return COMMA;
    }

    @Override
    public IElementType STRING() {
        return STRING;
    }

    @Override
    public IFileElementType FILE() {
        return FILE;
    }

    @Override
    public TokenSet COMMENTS() {
        return COMMENTS;
    }

    @Override
    public TokenSet STRING_LITERALS() {
        return STRING_LITERALS;
    }

    @Override
    public TokenSet WHITESPACES() {
        return WHITESPACES;
    }
}
