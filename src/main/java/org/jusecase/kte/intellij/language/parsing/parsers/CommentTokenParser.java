package org.jusecase.kte.intellij.language.parsing.parsers;

import org.jusecase.kte.intellij.language.parsing.KteTokenTypes;

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

        myTokenInfo.updateData(start, position, KteTokenTypes.COMMENT);
        return true;
    }
}
