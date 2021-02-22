package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.Lexer;

public class CommentTokenParser extends AbstractTokenParser {
    public CommentTokenParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public boolean hasToken(int position) {
        if (!isBeginOf(position, "<%--")) {
            return false;
        }

        int start = position;
        position += 4;

        while (position < myEndOffset && !isEndOf(position, "--%>")) {
            position++;
        }

        myTokenInfo.updateData(start, position, lexer.tokens.COMMENT());
        return true;
    }
}
