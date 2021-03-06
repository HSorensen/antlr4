/*
 * [The "BSD license"]
 *  Copyright (c) 2015 Henrik Sorensen
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
