package org.antlr.v4.runtime;

import org.antlr.v4.runtime.misc.Pair;


/**
 * LexerScannerIncludeStateStackItem
 * Item needed for maintain scanner state when restoring scanning 
 * from previous file
 */
public class LexerScannerIncludeStateStackItem {
	private CharStream input;
    private Pair<TokenSource, CharStream> tokenFactorySourcePair;
    
	public LexerScannerIncludeStateStackItem(CharStream input,
			Pair<TokenSource, CharStream> tokenFactorySourcePair) {
		this.input = input;
		this.tokenFactorySourcePair = tokenFactorySourcePair;
	}

	public CharStream getInput() {return input;}
	public Pair<TokenSource, CharStream> getTokenFactorySourcePair() {return tokenFactorySourcePair;}
}
