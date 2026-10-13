package com.evinn.fintrack.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {

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
