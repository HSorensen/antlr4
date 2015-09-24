package org.antlr.v4.runtime;

import org.antlr.v4.runtime.misc.Pair;


/**
 * LexerScannerIncludeStateStackItem
 * Item needed for maintain scanner state when restoring scanning 
 * from previous file
 */
public class LexerScannerIncludeStateStackItem {
	public final static int SIZE=4;
	private CharStream input;
    private Pair<TokenSource, CharStream> tokenFactorySourcePair;
    private int line;
    private int charPosInLine;
    
	
	public LexerScannerIncludeStateStackItem(CharStream input,
			Pair<TokenSource, CharStream> tokenFactorySourcePair, int line, int charPosInLine) {
		this.input = input;
		this.tokenFactorySourcePair = tokenFactorySourcePair;
		this.line=line;
		this.charPosInLine=charPosInLine;
	}

	public CharStream getInput() {return input;}
	public Pair<TokenSource, CharStream> getTokenFactorySourcePair() {return tokenFactorySourcePair;}
	public int getLine() {return line;}
	public int getCharPosInLine() {return charPosInLine;}
	
}
