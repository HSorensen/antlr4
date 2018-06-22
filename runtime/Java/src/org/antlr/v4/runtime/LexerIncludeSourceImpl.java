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

import java.io.IOException;
import java.io.Serializable;
import java.util.Stack;
import org.antlr.v4.runtime.misc.Pair;

/**
 * Implementation of interface that allow the lexer to include another file
 * into the scanning stream.
 * The lexer expects a CharStream object to be returned.
 */
public class LexerIncludeSourceImpl implements LexerIncludeSource, Serializable {
	
	
	/** 
	 * Keep track of lexerScanner states.
	 * Needed to handling grammars that allow to include 
	 * new content into the current scanning stream. 
	 */
	public final Stack<LexerIncludeStateStackItem> _lexerIncludeStateStack = new Stack<LexerIncludeStateStackItem>(); 

	
	public LexerIncludeSourceImpl() { }

	/**
	 * Override this method if exact tracking of included source files are needed.
	 */
	public CharStream embedSource(String currentName, int currentLine, int currentLinePos, String newFileName) { 
		return embedSource(newFileName); 
		}
	
	/**
	 * The embedSource method return a CharStream for the fileName.
	 * Returns null on error.
	 */
	public CharStream embedSource(String lexerText) {
		String fileName=lexerText;
		ANTLRInputStream istrm = null;
		try {
			istrm = new ANTLRFileStream(fileName);		
		} 
		catch (IOException e) {
			// TODO: Add error handling
			e.printStackTrace();
			istrm=null;
		} 
		
		return istrm;
	}

	
	/**
	 * return
	 * 	false iff scanner state stack is empty. Means nothing to restore.
	 *  true iff input has been restored
	 */
	public boolean restorePrevious(Lexer lexer) {
		if (lexer._hitEOF == false) {
			throw new IllegalStateException("restorePrevious requires an EOF to be met.");

		if (_lexerIncludeStateStack.isEmpty() == true) {
			return false;
		}
		
		if ( LexerATNSimulator.debug )
		   System.err.println(">> restorePrevious >"+lexer.getText()+"<"
			                + " stack size before >"+_lexerIncludeStateStack.size()+"<");
		
		LexerIncludeStateStackItem stackItem=_lexerIncludeStateStack.pop();
		// restore _input, _tokenFactorySourcePair, line and charPosInLine
		int checkSize=0;
		lexer._input=stackItem.getInput(); checkSize++;
		lexer._tokenFactorySourcePair=stackItem.getTokenFactorySourcePair(); checkSize++;
		lexer.getInterpreter().setLine(stackItem.getLine()); checkSize++;
		lexer.getInterpreter().setCharPositionInLine(stackItem.getCharPosInLine()); checkSize++;
		
		if (LexerIncludeStateStackItem.SIZE != checkSize) {
			throw new IllegalStateException("restorePrevious lexer state not fully restored.");
		}
		
		return true;
	}
	
	/**
	 * store and switch
	 * use lexer.getText() to get the matched text
	 */
	public void storeAndSwitch(Lexer lexer) {
		if ( LexerATNSimulator.debug )
			System.err.println(">> storeAndSwitch >"+lexer.getText()+"<"
			                   " stack size before >"+_lexerIncludeStateStack.size()+"<");
		
		if (lexer._hitIncludeSource == false) {
			throw new IllegalStateException("storeAndSwitch requires includeSource action.");
		}

		// store current lexer scanner state
		_lexerIncludeStateStack.push(new LexerIncludeStateStackItem(lexer._input
				                                                  , lexer._tokenFactorySourcePair
				                                                  , lexer.getInterpreter().getLine()
				                                                  , lexer.getInterpreter().getCharPositionInLine()));
		
			// open _includeFileName ...
		lexer._input = embedSource(lexer._input.getSourceName()    // currentName
					              ,lexer._tokenStartLine           // currentLine
                                  ,lexer._tokenStartCharIndex      // currentLinePos
					              ,lexer.getText());               // newFileName (lexer text matched)
			
	    if (lexer._input==null) {
				// An error happened so restore previous input
				lexer._input=_lexerIncludeStateStack.peek().getInput();
			   _lexerIncludeStateStack.pop();
	    }
		else {
			lexer._tokenFactorySourcePair = new Pair<TokenSource, CharStream>(lexer, lexer._input); 
			lexer._input.seek(0); // ensure position is set
			lexer.getInterpreter().reset();
		}		
	}
}
