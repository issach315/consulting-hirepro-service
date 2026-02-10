package com.hirepro.common.util;

import com.github.f4b6a3.ulid.UlidCreator;

/**
 * Utility class for generating ULID (Universally Unique Lexicographically Sortable Identifier)
 * Uses the ulid-creator library for ULID generation
 */
public class UlidGenerator {

    /**
     * Generates a new ULID string
     * @return ULID string (26 characters)
     */
    public static String generate() {
        return UlidCreator.getUlid().toString();
    }
}