package org.jusecase.jte.intellij.language.parsing;

import com.intellij.psi.tree.*;
import org.jusecase.jte.intellij.language.JteLanguage;

public class JteTokenTypes implements TokenTypes {
    public static final JteTokenTypes INSTANCE = new JteTokenTypes();

    public static final IElementType HTML_CONTENT = new JteElementType("HTML_CONTENT");
    public static final IElementType JAVA_CONTENT = new JteElementType("JAVA_CONTENT");
    public static final IElementType BLOCK = new JteElementType("BLOCK");

    public static final IElementType JAVA_INJECTION = new JteElementType("JAVA_INJECTION");
    public static final IElementType EXTRA_JAVA_INJECTION = new JteElementType("EXTRA_JAVA_INJECTION");

    public static final IElementType OUTER_ELEMENT_TYPE = new OuterLanguageElementType("OUTER_ELEMENT_TYPE", JteLanguage.INSTANCE);

    public static final IElementType IMPORT = new JteElementType("IMPORT");
    public static final IElementType PARAM = new JteElementType("PARAM");
    public static final IElementType OUTPUT = new JteElementType("OUTPUT");
    public static final IElementType OUTPUT_BEGIN = new JteElementType("OUTPUT_BEGIN");
    public static final IElementType OUTPUT_END = new JteElementType("OUTPUT_END");
    public static final IElementType STATEMENT = new JteElementType("STATEMENT");
    public static final IElementType STATEMENT_BEGIN = new JteElementType("STATEMENT_BEGIN");
    public static final IElementType STATEMENT_END = new JteElementType("STATEMENT_END");

    public static final IElementType IF = new JteElementType("IF");
    public static final IElementType CONDITION_BEGIN = new JteElementType("CONDITION_BEGIN");
    public static final IElementType CONDITION_END = new JteElementType("CONDITION_END");
    public static final IElementType ENDIF = new JteElementType("ENDIF");
    public static final IElementType ELSE = new JteElementType("ELSE");
    public static final IElementType ELSEIF = new JteElementType("ELSEIF");
    public static final IElementType FOR = new JteElementType("FOR");
    public static final IElementType ENDFOR = new JteElementType("ENDFOR");

    public static final IElementType TEMPLATE = new JteElementType("TEMPLATE");
    public static final IElementType TEMPLATE_NAME = new JteElementType("TEMPLATE_NAME");
    public static final IElementType NAME_SEPARATOR = new JteElementType("NAME_SEPARATOR");
    public static final IElementType PARAMS_BEGIN = new JteElementType("PARAMS_BEGIN");
    public static final IElementType PARAM_NAME = new JteElementType("PARAM_NAME");
    public static final IElementType PARAMS_END = new JteElementType("PARAMS_END");
    public static final IElementType CONTENT_BEGIN = new JteElementType("CONTENT_BEGIN");
    public static final IElementType CONTENT_END = new JteElementType("CONTENT_END");

    public static final IElementType COMMENT = new JteElementType("COMMENT");
    public static final IElementType COMMENT_CONTENT = new JteElementType("COMMENT_CONTENT");

    public static final IElementType WHITESPACE = new JteElementType("WHITESPACE");
    public static final IElementType EQUALS = new JteElementType("EQUALS");
    public static final IElementType COMMA = new JteElementType("COMMA");

    public static final IElementType STRING = new JteElementType("STRING");

    public static final IFileElementType FILE = new IStubFileElementType<>("FILE", JteLanguage.INSTANCE);

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
    public IElementType TEMPLATE() {
        return TEMPLATE;
    }

    @Override
    public IElementType TEMPLATE_NAME() {
        return TEMPLATE_NAME;
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
