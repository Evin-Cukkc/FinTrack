package com.evinn.fintrack.utils;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public class CurrencyUtils {


    /**
     * Parses user-entered Rupiah amounts using common Indonesian separators.
     * Examples: "1.000.000", "1,000,000", "1.250,50", and "1250.50".
     */
    public static double parseAmount(String input) throws NumberFormatException {
        if (input == null) throw new NumberFormatException("Nominal kosong");

        String value = input.trim()
                .replaceAll("(?i)rp", "")
                .replaceAll("\\s+", "")
                .replace("\u00A0", "");
        if (value.isEmpty()) throw new NumberFormatException("Nominal kosong");

        // Keep only characters that can participate in a numeric value.
        value = value.replaceAll("[^0-9,.\\-]", "");
        if (value.isEmpty() || !value.matches("-?[0-9][0-9.,]*")) {
            throw new NumberFormatException("Format nominal tidak valid");
        }

        boolean hasDot = value.contains(".");
        boolean hasComma = value.contains(",");
        if (hasDot && hasComma) {
            int dot = value.lastIndexOf('.');
            int comma = value.lastIndexOf(',');
            char decimalSeparator = dot > comma ? '.' : ',';
            int decimalIndex = Math.max(dot, comma);
            int decimalDigits = value.length() - decimalIndex - 1;
            if (decimalDigits >= 1 && decimalDigits <= 2) {
                char groupingSeparator = decimalSeparator == '.' ? ',' : '.';
                value = value.replace(String.valueOf(groupingSeparator), "");
                if (decimalSeparator == ',') value = value.replace(',', '.');
            } else {
                value = value.replace(".", "").replace(",", "");
            }
        } else if (hasDot || hasComma) {
            char separator = hasDot ? '.' : ',';
            String separatorText = Pattern.quote(String.valueOf(separator));
            if (value.matches("-?\\d{1,3}(?:" + separatorText + "\\d{3})+")) {
                value = value.replace(String.valueOf(separator), "");
            } else {
                value = value.replace(separator, '.');
            }
        }

        double parsed = Double.parseDouble(value);
        if (Double.isNaN(parsed) || Double.isInfinite(parsed)) {
            throw new NumberFormatException("Nominal di luar batas");
        }
        return parsed;
    }

    private static NumberFormat createFormatter() {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
        formatter.setMaximumFractionDigits(0);
        return formatter;
    }

    // Tanpa tanda "+", tapi nilai negatif tetap ditampilkan "-Rp ..."
    // (dipakai untuk saldo, anggaran, dan sisa anggaran).
    public static String formatRupiahWithoutSign(double amount) {
        String formatted = createFormatter().format(Math.abs(amount));
        return (amount < 0 ? "-Rp " : "Rp ") + formatted;
    }

    // Dengan tanda "+" / "-" (dipakai di daftar transaksi).
    public static String formatRupiah(double amount) {
        String formatted = createFormatter().format(Math.abs(amount));
        return (amount < 0 ? "-Rp " : "+Rp ") + formatted;
    }

    // Format ringkas untuk label grafik: "Rp 450 rb", "Rp 2,5 jt", "Rp 1,2 M".
    public static String formatCompact(double amount) {
        double abs = Math.abs(amount);
        String sign = amount < 0 ? "-" : "";
        String value;

        if (abs >= 1_000_000_000d) {
            value = compactNumber(abs / 1_000_000_000d) + " M";
        } else if (abs >= 1_000_000d) {
            value = compactNumber(abs / 1_000_000d) + " jt";
        } else if (abs >= 1_000d) {
            value = compactNumber(abs / 1_000d) + " rb";
        } else {
            value = String.valueOf((long) abs);
        }

        return sign + "Rp " + value;
    }

    private static String compactNumber(double value) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(1);
        return formatter.format(value);
    }
}
