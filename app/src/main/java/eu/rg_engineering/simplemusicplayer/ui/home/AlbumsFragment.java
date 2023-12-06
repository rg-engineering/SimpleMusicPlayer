package eu.rg_engineering.simplemusicplayer.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import eu.rg_engineering.simplemusicplayer.R;

public class AlbumsFragment extends Fragment {

    private String TAG = "AlbumsFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_albums, container, false);

        try {


        } catch (Exception ex) {
            Log.e(TAG, "exception in onCreateView " + ex.toString());
        }
        return root;


    }

}
