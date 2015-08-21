package org.antlr.v4.runtime;

import java.io.IOException;

/**
 * Implementation of interface that allow the lexer to include another file
 * into the scanning stream.
 * The lexer expects a CharStream object to be returned.
 */
public class LexerScannerIncludeSourceImpl implements LexerScannerIncludeSource {
	
	/**
	 * The embedSource method reads the fileName and optionally substitutes text
	 * before returning the CharStream for the file.
	 */
	public CharStream embedSource(String fileName, String substituteFrom, String substituteTo) {
		ANTLRInputStream istrm = null;
		try {
			istrm = new ANTLRFileStream(fileName);
			if (substituteFrom != null) {
				String beforeStream = String.copyValueOf(istrm.data, 0,
						istrm.size());
				String replacedStream = beforeStream.replaceAll(substituteFrom,
						substituteTo);
				istrm = new ANTLRInputStream(replacedStream);
			}
		} catch (IOException e) {
			// TODO: Add error handling
			e.printStackTrace();
		}

		return istrm;
	}
}
