package eu.rg_engineering.simplemusicplayer;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

import eu.rg_engineering.simplemusicplayer.R;
import eu.rg_engineering.simplemusicplayer.ui.home.HomeFragment;




public class MainActivity extends AppCompatActivity
        implements MusicItemsAdapter.MusicItemsAdapterListener
         {

    private AppBarConfiguration mAppBarConfiguration;

    private String TAG = "Main";
    MediaPlayer music;
    @Override
    public void messageFromMusicItemsAdapter (String msg, String params) {

        Log.d(TAG, "got message from MusicItemsAdapter " + msg + " " + params);

        switch (msg) {

            case "PlayMusic":
                musicplay(params);
                break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        //fragments wieder rein: activity_main_drawer.xml und mobile_navigation.xml
        mAppBarConfiguration = new AppBarConfiguration.Builder(

        R.id.nav_home, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //show version info
        PackageInfo packageinfo = null;
        try {
            packageinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "exception in onCreate " + e.getMessage());
        }
        String Version = packageinfo.versionName.toString();

        View header = navigationView.getHeaderView(0);
        TextView text = (TextView) header.findViewById(R.id.VersionView);
        text.setText(Version);


        FindHomeFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //SaveData();
    }

    private HomeFragment FindHomeFragment() {

        HomeFragment homeFragment = null;
        FragmentManager manager = getSupportFragmentManager();
        if (manager != null) {
            Fragment mainFragment = (Fragment) manager.findFragmentById(R.id.nav_host_fragment);
            if (mainFragment != null) {
                FragmentManager subManager = mainFragment.getChildFragmentManager();
                if (subManager != null) {

                    List<Fragment> allFrag = subManager.getFragments();
                     homeFragment = (HomeFragment) subManager.findFragmentById(R.id.nav_home);
                }
            }
        }
        return homeFragment;
    }

    // Playing the music
    private void musicplay(String filename) {
        if (music!=null) {
            music.stop();
        }
        Uri uri = Uri.parse(filename);
        music = MediaPlayer.create(this, uri);
        music.start();
    }

    private void musicplay() {
        music.start();
    }

    // Pausing the music
    private void musicpause() {
        music.pause();
    }

    // Stopping the music
    private void musicstop() {
        music.stop();
        //music = MediaPlayer.create(this, R.raw.antihero);
    }



}
