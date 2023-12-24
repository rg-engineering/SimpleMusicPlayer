package eu.rg_engineering.simplemusicplayer.ui.home;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import eu.rg_engineering.simplemusicplayer.Constants;
import eu.rg_engineering.simplemusicplayer.MusicItem;
import eu.rg_engineering.simplemusicplayer.MusicItemsAdapter;
import eu.rg_engineering.simplemusicplayer.R;
import eu.rg_engineering.simplemusicplayer.utils.MyItemTouchHelper;
import eu.rg_engineering.simplemusicplayer.utils.OnDeleteMusicitemListener;
import io.sentry.Sentry;


public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private final String TAG = "HomeFragment";
    HomeFragmentListener mCommunication;
    Context mContext;
    public interface HomeFragmentListener {
        void messageFromHomeFragment(String msg, String params);
    }
    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        mCommunication = (HomeFragmentListener) context;
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCommunication = null;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        try {

            Button btnPauseMusic = (Button) root.findViewById(R.id.PauseMusic);
            btnPauseMusic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "PauseMusic pressed");
                    mCommunication.messageFromHomeFragment("PauseMusic", "");
                }
            });

            Button btnPlayMusic = (Button) root.findViewById(R.id.PlayMusic);
            btnPlayMusic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "PlayMusic pressed");
                    mCommunication.messageFromHomeFragment("PlayMusic", "");
                }
            });

            Button btnStopMusic = (Button) root.findViewById(R.id.StopMusic);
            btnStopMusic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "StopMusic pressed");
                    mCommunication.messageFromHomeFragment("StopMusic", "");
                }
            });

        } catch (Exception ex) {
            Log.e(TAG, "exception in onCreateView " + ex);
            Sentry.captureException(ex);
        }
        return root;
    }

    //todo wma kann nicht interpretiert werden
}