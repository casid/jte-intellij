package org.jusecase.kte.intellij.language.parsing;

import com.intellij.ide.highlighter.custom.tokens.TokenInfo;
import com.intellij.ide.highlighter.custom.tokens.TokenParser;
import com.intellij.ide.highlighter.custom.tokens.WhitespaceParser;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.CustomHighlighterTokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jusecase.kte.intellij.language.parsing.parsers.*;

public class KteLexer extends LexerBase {

    public static final int CONTENT_STATE_HTML = 0;
    public static final int CONTENT_STATE_IMPORT_BEGIN = 1;
    public static final int CONTENT_STATE_IMPORT_END = 2;
    public static final int CONTENT_STATE_PARAM_BEGIN = 3;
    public static final int CONTENT_STATE_PARAM_END = 4;
    public static final int CONTENT_STATE_OUTPUT_BEGIN = 5;
    public static final int CONTENT_STATE_OUTPUT_END = 6;
    public static final int CONTENT_STATE_IF_BEGIN = 7;
    public static final int CONTENT_STATE_IF_CONDITION = 8;
    public static final int CONTENT_STATE_IF_END = 9;
    public static final int CONTENT_STATE_FOR_BEGIN = 10;
    public static final int CONTENT_STATE_FOR_CONDITION = 11;
    public static final int CONTENT_STATE_FOR_END = 12;
    public static final int CONTENT_STATE_ELSEIF_BEGIN = 13;
    public static final int CONTENT_STATE_ELSEIF_CONDITION = 14;
    public static final int CONTENT_STATE_ELSEIF_END = 15;
    public static final int CONTENT_STATE_TAG_BEGIN = 16;
    public static final int CONTENT_STATE_TAG_NAME_BEGIN = 17;
    public static final int CONTENT_STATE_TAG_PARAMS = 18;
    public static final int CONTENT_STATE_TAG_END = 19;
    public static final int CONTENT_STATE_STATEMENT_BEGIN = 20;
    public static final int CONTENT_STATE_STATEMENT_END = 21;

    private CharSequence myBuffer = ArrayUtil.EMPTY_CHAR_SEQUENCE;
    private int myEndOffset = 0;
    private final TokenParser[] myTokenParsers;
    private TokenInfo myCurrentToken;
    private int myPosition;
    private int myState;

    public KteLexer() {
        myTokenParsers = new TokenParser[]{
                new ContentTokenParser(this),
                new CommentTokenParser(),
                new ImportTokenParser(this),
                new ParamTokenParser(this),
                new OutputTokenParser(this),
                new StatementTokenParser(this),
                new IfTokenParser(this),
                new IfConditionTokenParser(this),
                new ElseIfTokenParser(this),
                new ElseIfConditionTokenParser(this),
                new ElseTokenParser(this),
                new EndIfTokenParser(),
                new ForTokenParser(this),
                new ForConditionTokenParser(this),
                new EndForTokenParser(),
                new TagTokenParser(this),
                new TagNameTokenParser(this),
                new TagParamsTokenParser(this),
                new WhitespaceParser(),
        };
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        myBuffer = buffer;
        myEndOffset = endOffset;
        myPosition = startOffset;
        myCurrentToken = new TokenInfo();
        myState = initialState;
        for (TokenParser tokenParser : myTokenParsers) {
            tokenParser.setBuffer(myBuffer, startOffset, myEndOffset);
        }
        advance();
    }

    // Method used by jetbrains, do not use for state checks
    @Override
    public int getState() {
        return myState;
    }

    @Override
    public IElementType getTokenType() {
        return myCurrentToken.getType();
    }

    @Override
    public int getTokenStart() {
        return myCurrentToken.getStart();
    }

    @Override
    public int getTokenEnd() {
        return myCurrentToken.getEnd();
    }

    @Override
    public void advance() {
        if (myPosition >= myEndOffset) {
            myCurrentToken.updateData(myPosition, myPosition, null);
            return;
        }
        boolean tokenFound = false;
        for (TokenParser tokenParser : myTokenParsers) {
            if (tokenParser.hasToken(myPosition)) {
                tokenParser.getTokenInfo(myCurrentToken);
                if (myCurrentToken.getEnd() <= myCurrentToken.getStart()) {
                    throw new AssertionError(tokenParser);
                }
                tokenFound = true;
                break;
            }
        }

        if (!tokenFound) {
            handleTokenNotFound();
        }
        myPosition = myCurrentToken.getEnd();
    }

    protected void handleTokenNotFound() {
        myCurrentToken.updateData(myPosition, myPosition + 1, CustomHighlighterTokenType.CHARACTER);
    }

    @Override
    @NotNull
    public CharSequence getBufferSequence() {
        return myBuffer;
    }

    @Override
    public int getBufferEnd() {
        return myEndOffset;
    }

    public int getCurrentState() {
        return myState & 0xFFFF;
    }

    public int getCurrentCount() {
        return myState >> 16;
    }

    public void setCurrentState(int state) {
        setStateInternal(state, getCurrentCount());
    }

    public void incrementCurrentCount() {
        setCurrentCount(getCurrentCount() + 1);
    }

    public void decrementCurrentCount() {
        setCurrentCount(getCurrentCount() - 1);
    }

    public void setCurrentCount(int count) {
        setStateInternal(getCurrentState(), count);
    }

    private void setStateInternal(int state, int count) {
        myState = (count << 16) | (state & 0xFFFF);
    }

    public boolean isInKotlinEndState() {
        switch (getCurrentState()) {
            case CONTENT_STATE_IMPORT_END:
            case CONTENT_STATE_PARAM_END:
            case CONTENT_STATE_STATEMENT_END:
            case CONTENT_STATE_OUTPUT_END:
            case CONTENT_STATE_IF_END:
            case CONTENT_STATE_FOR_END:
            case CONTENT_STATE_ELSEIF_END:
            case CONTENT_STATE_TAG_END:
                return true;
        }
        return false;
    }
}
