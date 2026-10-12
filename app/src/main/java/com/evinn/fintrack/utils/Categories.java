package com.evinn.fintrack.utils;

import android.content.Context;

import com.evinn.fintrack.R;

import java.util.Locale;

/**
 * Daftar kategori standar dan normalisasi nama kategori.
 */
public final class Categories {

    private Categories() {
    }

    /** Daftar kategori untuk dropdown (lihat string-array "categories"). */
    public static String[] list(Context context) {
        return context.getResources().getStringArray(R.array.categories);
    }

    /**
     * Menyamakan penulisan kategori (Indonesia/Inggris, huruf besar/kecil)
     * supaya anggaran tetap cocok dengan transaksi lama yang memakai nama Inggris.
     */
    public static String canonical(String category) {
        if (category == null) {
            return "";
        }

        String key = category.trim().toLowerCase(Locale.ROOT);

        switch (key) {
            case "food":
            case "restaurant":
            case "restoran":
                return "makanan";
            case "coffee":
                return "kopi";
            case "shopping":
                return "belanja";
            case "transport":
            case "transportation":
                return "transportasi";
            case "entertainment":
                return "hiburan";
            case "subscription":
                return "langganan";
            case "electricity":
                return "listrik";
            case "book":
            case "books":
                return "buku";
            case "health":
            case "medical":
                return "kesehatan";
            case "education":
                return "pendidikan";
            case "salary":
                return "gaji";
            case "other":
                return "lainnya";
            default:
                return key;
        }
    }
}
