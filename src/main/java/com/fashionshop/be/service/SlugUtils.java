package com.fashionshop.be.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class SlugUtils {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private SlugUtils() {
    }

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "item-" + System.currentTimeMillis();
        }
        String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'd')
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "item-" + System.currentTimeMillis() : normalized;
    }

    public static String normalizeSearchText(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        return Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'd')
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static String formatVnd(BigDecimal amount) {
        if (amount == null) {
            return "0 ₫";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("vi-VN"));
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,###", symbols);
        return df.format(amount) + " ₫";
    }

    public static String formatDate(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATE_FMT);
    }

    public static String formatDateTime(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DATETIME_FMT);
    }
}

