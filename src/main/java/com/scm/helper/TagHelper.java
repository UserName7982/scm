package com.scm.helper;

import java.util.Set;
import java.util.TreeSet;

/** Cleans up user-typed tags: lower-case, no spaces, only letters/digits/_/-, max 30 chars, max 10 tags. */
public class TagHelper {

    public static final int MAX_TAGS = 10;
    public static final int MAX_LENGTH = 30;

    private TagHelper() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String t = raw.trim().toLowerCase().replaceAll("\\s+", "-").replaceAll("[^\\p{L}\\p{N}_-]", "");
        return t.length() > MAX_LENGTH ? t.substring(0, MAX_LENGTH) : t;
    }

    /** Parses "friend, work; vip" into a sorted set of clean tags. */
    public static Set<String> parse(String raw) {
        Set<String> tags = new TreeSet<>();
        if (raw == null || raw.isBlank()) {
            return tags;
        }
        for (String part : raw.split("[,;]")) {
            String t = normalize(part);
            if (!t.isEmpty() && tags.size() < MAX_TAGS) {
                tags.add(t);
            }
        }
        return tags;
    }

    public static String join(Set<String> tags, String separator) {
        return tags == null ? "" : String.join(separator, tags);
    }
}
