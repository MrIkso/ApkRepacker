package com.mrikso.codeeditor.util;

import com.mrikso.codeeditor.lang.LanguageNonProg;

import java.util.List;

public class NonProgLexTask extends LexTask {

	@Override
	protected void tokenize(List<Pair> _tokens, String text) {
		_tokens.add(new Pair(0, Lexer.NAME));
	}

	@Override
	public String getLanguageType() {
		return "None";
	}

	public static NonProgLexTask instance = new NonProgLexTask();
	private NonProgLexTask() {
		super(LanguageNonProg.getInstance());
	}

}
