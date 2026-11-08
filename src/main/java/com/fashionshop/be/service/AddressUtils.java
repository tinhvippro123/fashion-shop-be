package com.fashionshop.be.service;

import lombok.Builder;
import lombok.Getter;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

public class AddressUtils {

    @Getter
    @Builder
    public static class ParsedAddress {
        private String province;
        private String district;
        private String ward;
        private String streetAddress;
        private String fullAddress;
    }

    public static ParsedAddress parseAddress(String raw) {
        if (raw == null || raw.isBlank()) {
            return ParsedAddress.builder()
                    .province("")
                    .district("")
                    .ward("")
                    .streetAddress("")
                    .fullAddress("")
                    .build();
        }
        String full = raw.trim();
        String[] parts = Arrays.stream(full.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        String province = "";
        String district = "";
        String ward = "";
        String streetAddress = "";

        if (parts.length >= 4) {
            province = parts[parts.length - 1];
            district = parts[parts.length - 2];
            ward = parts[parts.length - 3];
            streetAddress = String.join(", ", Arrays.copyOfRange(parts, 0, parts.length - 3));
        } else if (parts.length == 3) {
            province = parts[2];
            district = parts[1];
            ward = "";
            streetAddress = parts[0];
        } else if (parts.length == 2) {
            province = parts[1];
            district = "";
            ward = "";
            streetAddress = parts[0];
        } else {
            province = parts[0];
            district = "";
            ward = "";
            streetAddress = parts[0];
        }

        return ParsedAddress.builder()
                .province(province)
                .district(district)
                .ward(ward)
                .streetAddress(streetAddress)
                .fullAddress(full)
                .build();
    }

    public static String normalize(String str) {
        if (str == null) return "";
        String n = Normalizer.normalize(str, Normalizer.Form.NFD);
        n = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(n).replaceAll("");
        n = n.replace('đ', 'd').replace('Đ', 'd');
        return n.replaceAll("[^a-zA-Z0-9]+", " ").trim().toLowerCase(Locale.ROOT);
    }
}
