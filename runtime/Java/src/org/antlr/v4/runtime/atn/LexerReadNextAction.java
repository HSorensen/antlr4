/*
 * Copyright (c) 2012-2017 The ANTLR Project. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE.txt file in the project root.
 */

package org.antlr.v4.runtime.atn;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.misc.MurmurHash;

/**
 * Implements the {@code pushMode} lexer action by calling
 * {@link Lexer#pushMode} with the assigned mode.
 *
 * @author Sam Harwell
 * @since 4.2
 */
public final class LexerReadNextAction implements LexerAction {
	/**
	 * Provides a singleton instance of this parameterless lexer action.
	 */
	public static final LexerReadNextAction INSTANCE = new LexerReadNextAction();
	
	/**
	 * Constructs a new {@code readNext} action with the specified mode value.
	 * @param mode The mode value to pass to {@link Lexer#readNext}.
	 */
	public LexerReadNextAction() {
	}

	/**
	 * {@inheritDoc}
	 * @return This method returns {@link LexerActionType#READ_NEXT}.
	 */
	@Override
	public LexerActionType getActionType() {
		return LexerActionType.READ_NEXT;
	}

	/**
	 * {@inheritDoc}
	 * @return This method returns {@code false}.
	 */
	@Override
	public boolean isPositionDependent() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>This action is implemented by calling {@link Lexer#readNext} with the
	 * value provided by {@link #getMode}.</p>
	 */
	@Override
	public void execute(Lexer lexer) {
		lexer.readNext();
	}

	@Override
	public int hashCode() {
		int hash = MurmurHash.initialize();
		hash = MurmurHash.update(hash, getActionType().ordinal());
		return MurmurHash.finish(hash, 1);
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public String toString() {
		return "readNext";
	}
}
