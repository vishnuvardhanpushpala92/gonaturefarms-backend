package com.gonaturefarms.util;

import java.security.SecureRandom;

/**
 * Generates order IDs in the same shape as the original Node helper:
 * <pre>
 *   'GNF' + Date.now().toString(36).toUpperCase() + Math.random().toString(36).substr(2,4).toUpperCase()
 * </pre>
 */
public final class OrderIdGenerator {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    private OrderIdGenerator() {
    }

    public static String generate() {
        String timestampBase36 = Long.toString(System.currentTimeMillis(), 36).toUpperCase();
        StringBuilder randomPart = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            randomPart.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return "GNF" + timestampBase36 + randomPart.toString().toUpperCase();
    }
}
