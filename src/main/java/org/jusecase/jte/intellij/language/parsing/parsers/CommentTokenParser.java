package org.jusecase.jte.intellij.language.parsing.parsers;

import org.jusecase.jte.intellij.language.parsing.JteTokenTypes;

public class CommentTokenParser extends AbstractTokenParser {
    public CommentTokenParser() {
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

        myTokenInfo.updateData(start, position, JteTokenTypes.COMMENT);
        return true;
    }
}
