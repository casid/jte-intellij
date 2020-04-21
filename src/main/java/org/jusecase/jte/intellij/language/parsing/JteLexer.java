package org.jusecase.jte.intellij.language.parsing;

import com.intellij.ide.highlighter.custom.tokens.TokenInfo;
import com.intellij.ide.highlighter.custom.tokens.TokenParser;
import com.intellij.ide.highlighter.custom.tokens.WhitespaceParser;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.CustomHighlighterTokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jusecase.jte.intellij.language.parsing.parsers.*;

public class JteLexer extends LexerBase {

    public static final int CONTENT_STATE_HTML = 0;
    public static final int CONTENT_STATE_JAVA_IMPORT_BEGIN = 1;
    public static final int CONTENT_STATE_JAVA_IMPORT_END = 2;
    public static final int CONTENT_STATE_JAVA_PARAM_BEGIN = 3;
    public static final int CONTENT_STATE_JAVA_PARAM_END = 4;
    public static final int CONTENT_STATE_JAVA_OUTPUT_BEGIN = 5;
    public static final int CONTENT_STATE_JAVA_OUTPUT_END = 6;

    private CharSequence myBuffer = ArrayUtil.EMPTY_CHAR_SEQUENCE;
    private int myEndOffset = 0;
    private final TokenParser[] myTokenParsers;
    private TokenInfo myCurrentToken;
    private int myPosition;
    private int myState = CONTENT_STATE_HTML;

    public JteLexer() {
        myTokenParsers = new TokenParser[]{
                new ContentTokenParser(this),
                new ImportTokenParser(this),
                new ParamTokenParser(this),
                new OutputTokenParser(this),
                new IfTokenParser(),
                new EndIfTokenParser(),
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

    public void setState(int state) {
        myState = state;
    }

    public boolean isInJavaEndState() {
        switch (myState) {
            case CONTENT_STATE_JAVA_IMPORT_END:
            case CONTENT_STATE_JAVA_PARAM_END:
            case CONTENT_STATE_JAVA_OUTPUT_END:
                return true;
        }
        return false;
    }
}
