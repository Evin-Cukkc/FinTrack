package com.evinn.fintrack.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
  
public class DateUtils {

    private static final Locale ID = new Locale("id", "ID");

    // Contoh hasil: "11 Oktober 2026"
    public static String getToday() {
        return new SimpleDateFormat("d MMMM yyyy", ID).format(new Date());
    }

    // Contoh hasil: "Oktober 2026"
    public static String getCurrentMonthLabel() {
        return new SimpleDateFormat("MMMM yyyy", ID).format(new Date());
    }

    // True jika tanggal transaksi ("d MMMM yyyy") berada di bulan berjalan.
    public static boolean isInCurrentMonth(String date) {
        return date != null && date.endsWith(" " + getCurrentMonthLabel());
    }
}