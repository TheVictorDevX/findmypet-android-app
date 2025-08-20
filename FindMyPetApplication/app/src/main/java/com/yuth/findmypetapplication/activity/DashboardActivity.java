package com.yuth.findmypetapplication.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yuth.findmypetapplication.R;
import com.yuth.findmypetapplication.fragment.HomeFragment;
import com.yuth.findmypetapplication.fragment.ProfileFragment;

public class DashboardActivity extends AppCompatActivity {
    // declare views
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // default stuffs
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cl_register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // set bottom nav bar
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Handle navigation item clicks
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment=null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_upload) {
                Intent intent=new Intent(this, UploadActivity.class);
                startActivity(intent);
                return false;
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }
            // You should probably load the fragment here
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView, selectedFragment) // replace 'fragment_container' with your container ID
                        .commit();
            }
            return true;
        });
    }
}