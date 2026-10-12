package com.evinn.fintrack.utils;

import android.app.Activity;
import android.content.Intent;

import com.evinn.fintrack.AnalyticsActivity;
import com.evinn.fintrack.BudgetActivity;
import com.evinn.fintrack.MainActivity;
import com.evinn.fintrack.ProfileActivity;
import com.evinn.fintrack.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Menghubungkan bottom navigation yang sama di semua halaman utama.
 * Beranda menjadi halaman akar: tombol kembali dari tab lain selalu menuju Beranda.
 */
public final class NavigationHelper {

    private NavigationHelper() {
    }

    @SuppressWarnings("deprecation")
    public static void setup(final Activity activity, final int selectedItemId) {
        BottomNavigationView nav = activity.findViewById(R.id.bottomNav);
        if (nav == null) {
            return;
        }

        // Atur item aktif lebih dulu, baru pasang listener.
        nav.setSelectedItemId(selectedItemId);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == selectedItemId) {
                return true;
            }

            Intent intent;
            if (id == R.id.nav_home) {
                intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else if (id == R.id.nav_analytics) {
                intent = new Intent(activity, AnalyticsActivity.class);
            } else if (id == R.id.nav_budget) {
                intent = new Intent(activity, BudgetActivity.class);
            } else if (id == R.id.nav_profile) {
                intent = new Intent(activity, ProfileActivity.class);
            } else {
                return false;
            }

            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);

            // Beranda tetap di belakang; tab lain diganti agar tumpukan halaman tidak menumpuk.
            if (selectedItemId != R.id.nav_home) {
                activity.finish();
                activity.overridePendingTransition(0, 0);
            }

            // false = jangan ubah item aktif di halaman ini (halaman baru yang akan tampil).
            return false;
        });
    }
}
