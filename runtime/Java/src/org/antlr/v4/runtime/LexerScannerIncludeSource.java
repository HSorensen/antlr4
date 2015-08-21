package org.antlr.v4.runtime;


/**
 * Interface that allow the lexer to include another file
 * into the scanning stream.
 */
public interface LexerScannerIncludeSource {
	
	/**
	 * The embedSource method reads the fileName and optionally substitutes text
	 * before returning the CharStream for the file.
	 */
	public CharStream embedSource(String fileName, String substituteFrom, String substituteTo);
	

}
