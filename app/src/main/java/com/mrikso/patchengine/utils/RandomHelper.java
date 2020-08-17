package com.mrikso.patchengine.utils;

import java.util.Random;

public final class RandomHelper {

    private static final char[] LETTERS = {'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x',
            'y', 'z'};
    private static Random mRandom;

    public static String getRandomString(int values) {
        if (mRandom == null) {
            mRandom = new Random(System.currentTimeMillis());
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values; values++) {
            sb.append(LETTERS[mRandom.nextInt(26)]);
        }
        return sb.toString();
    }
}