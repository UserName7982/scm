package com.scm.helper;

import java.util.ArrayList;
import java.util.List;

/** Tiny CSV reader/writer helper (handles quotes, commas and line breaks inside cells). */
public class CsvHelper {

    private CsvHelper() {
    }

    /** Escapes a cell for CSV output. Also neutralises spreadsheet formula injection (=, +, -, @). */
    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        String v = value;
        if (!v.isEmpty() && "=+-@\t\r".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    /** Parses CSV text into rows of cells. */
    public static List<List<String>> parse(String text) {
        List<List<String>> rows = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return rows;
        }
        if (text.charAt(0) == '\uFEFF') {
            text = text.substring(1); // strip BOM
        }
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        cell.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cell.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                row.add(clean(cell.toString()));
                cell.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                row.add(clean(cell.toString()));
                cell.setLength(0);
                if (!(row.size() == 1 && row.get(0).isEmpty())) {
                    rows.add(row);
                }
                row = new ArrayList<>();
            } else {
                cell.append(c);
            }
        }
        if (cell.length() > 0 || !row.isEmpty()) {
            row.add(clean(cell.toString()));
            rows.add(row);
        }
        return rows;
    }

    // undo the formula-injection prefix added by escape()
    private static String clean(String v) {
        String t = v.trim();
        if (t.length() > 1 && t.charAt(0) == '\'' && "=+-@".indexOf(t.charAt(1)) >= 0) {
            return t.substring(1);
        }
        return t;
    }
}
