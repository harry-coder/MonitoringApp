package firebasecloud.com.firebasecloud;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import firebasecloud.com.firebasecloud.BottomNavigationFragements.Active;
import firebasecloud.com.firebasecloud.BottomNavigationFragements.Profile;
import firebasecloud.com.firebasecloud.BottomNavigationFragements.Tasks;
import firebasecloud.com.firebasecloud.CustomElements.InstantLocation;
import firebasecloud.com.firebasecloud.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    String[] locationPermissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
    int PERMISSION_ALL = 1;

    ActivityMainBinding binding;
    boolean doubleBackToExitPressedOnce = false;

    InstantLocation getUserInstantLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        Toolbar tabs_scrolling = findViewById(R.id.toolbar);
        setSupportActionBar(tabs_scrolling);


        getUserInstantLocation=new InstantLocation(this,this);
        binding.bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                switch (item.getItemId()) {
                    case R.id.bn_task: {
                        selectedFragment = Tasks.newInstance("", "");
                        break;
                    }
                    case R.id.bn_active: {
                        selectedFragment = Active.newInstance("", "");
                        break;
                    }
                    case R.id.bn_profile: {
                        selectedFragment = Profile.newInstance("", "");
                        break;
                    }

                }
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame, selectedFragment);
                transaction.commit();
                return true;


            }
        });
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, Tasks.newInstance("", ""));
        transaction.commit();



    }


    @Override
    public void onBackPressed() {

        //  int count = getFragmentManager().getBackStackEntryCount();

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);

    }

    @Override
    protected void onResume() {
        super.onResume();
      getUserInstantLocation.  getLocation();
      getUserInstantLocation.  changeLocationSetting(PERMISSION_ALL,locationPermissions);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 300 && resultCode == RESULT_OK) {


            Toast.makeText(this, "Location service activated", Toast.LENGTH_SHORT).show();


        }

    }
}
