/*
 * Copyright (c) 2013 Tah Wei Hoon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License Version 2.0,
 * with full text available at http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * This software is provided "as is". Use at your own risk.
 */
package com.mrikso.codeeditor.util;

public final class Pair {
	public int first;
	public int second;

	public Pair(int x, int y){
		first = x;
		second = y;
	}

	public final void setFirst(int value){
		first = value;
	}

	public final void setSecond(int value){
		second = value;
	}

	public final int getFirst() {
		return first;
	}

	public final int getSecond() {
		return second;
	}

	@Override
	public String toString() {
		return "("+first+","+second+")";
	}

}

