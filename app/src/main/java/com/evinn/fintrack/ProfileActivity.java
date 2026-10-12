package com.evinn.fintrack;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.evinn.fintrack.utils.NavigationHelper;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileSubtitle, tvProfileBio, tvAppVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileSubtitle = findViewById(R.id.tvProfileSubtitle);
        tvProfileBio = findViewById(R.id.tvProfileBio);
        tvAppVersion = findViewById(R.id.tvAppVersion);

        // Binding data profil
        tvProfileName.setText("Evin Ryan Rey");
        tvProfileSubtitle.setText("Pengembang FinTrack");
        tvProfileBio.setText("\"Alhamdulillah, ini juga lagi usaha biar dosanya bisa dihapus\"");

        tvAppVersion.setText(getAppVersion());

        NavigationHelper.setup(this, R.id.nav_profile);
    }

    private String getAppVersion() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return info.versionName != null ? info.versionName : "1.0";
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0";
        }
    }
}
