package com.test;

/** Retired: hardcoded IDs and seed writes cannot repair corrupted text safely. */
public final class FixDatabaseFont {
    public static void main(String[] args) {
        System.err.println("SKIPPED: legacy font/seed writer retired. Use database/06_Demo_Data.sql for explicit demo data; repair real text only from a reviewed backup/migration.");
        System.exit(2);
    }
}
