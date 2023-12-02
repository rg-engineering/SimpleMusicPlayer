package eu.rg_engineering.simplemusicplayer.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import eu.rg_engineering.simplemusicplayer.R;

public class SearchArtistFragment extends Fragment {

    private String TAG = "SearchArtistFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_artists, container, false);

        try {


        } catch (Exception ex) {
            Log.e(TAG, "exception in onCreateView " + ex.toString());
        }
        return root;


    }

}
