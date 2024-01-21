package eu.rg_engineering.simplemusicplayer.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import eu.rg_engineering.simplemusicplayer.MainActivity;
import eu.rg_engineering.simplemusicplayer.R;
import eu.rg_engineering.simplemusicplayer.TrackItem;
import eu.rg_engineering.simplemusicplayer.PlaylistItemsAdapter;
import eu.rg_engineering.simplemusicplayer.utils.MyItemTouchHelper;
import eu.rg_engineering.simplemusicplayer.utils.OnDeletePlaylistitemListener;
import io.sentry.Sentry;

//todo Anzeige Anzahl Tracks
// Bild von Artist und Album einfügen

public class PlaylistFragment extends Fragment implements
        OnDeletePlaylistitemListener {

    private final String TAG = "PlaylistFragment";
    PlaylistFragmentListener mCommunication;
    Context mContext;
    private RecyclerView rvPlaylistItems = null;
    private PlaylistItemsAdapter PlaylistItemsAdapter = null;
    ArrayList<TrackItem> mPlaylistTracks;

    @Override
    public void ItemDeleted() {
        //SaveData();
    }
    public interface PlaylistFragmentListener {
        void messageFromPlaylistFragment(String msg, String params);
    }
    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        mCommunication = (PlaylistFragmentListener) context;
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCommunication = null;
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_playlist, container, false);

        try {
            rvPlaylistItems = (RecyclerView) root.findViewById(R.id.rvPlaylistTracks);

            MainActivity activity = (MainActivity) getActivity();

            //todo load playlist from file
            //mPlaylistTracks=  from file...

            // Create adapter passing in the sample user data
            PlaylistItemsAdapter = new PlaylistItemsAdapter(mPlaylistTracks,this);

            ItemTouchHelper.Callback callback = new MyItemTouchHelper(PlaylistItemsAdapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            PlaylistItemsAdapter.setTouchHelper(itemTouchHelper);
            itemTouchHelper.attachToRecyclerView(rvPlaylistItems);
            // Attach the adapter to the recyclerview to populate items
            rvPlaylistItems.setAdapter(PlaylistItemsAdapter);

            // Set layout manager to position the items
            rvPlaylistItems.setLayoutManager(new LinearLayoutManager(getActivity()));
            rvPlaylistItems.setItemAnimator(null);
            // That's all!
            AutoCompleteTextView editFilterTracks = (AutoCompleteTextView ) root.findViewById(R.id.filter_playlisttracks);
            ArrayList <String> TrackList = new ArrayList<>();

            for (int i = 0; i < mPlaylistTracks.size(); i++) {

                String track = mPlaylistTracks.get(i).getName();
                if (!TrackList.contains(track)) {
                    TrackList.add(track);
                }
            }

            ArrayAdapter<String> TrackListAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_dropdown_item_1line, TrackList);

            editFilterTracks.setThreshold(1);
            editFilterTracks.setAdapter(TrackListAdapter);

            editFilterTracks.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d(TAG, "track filter text changed " + s);
                    PlaylistItemsAdapter.setFilterIdx(1);
                    PlaylistItemsAdapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            Button btnBack = (Button) root.findViewById(R.id.btnBackTracks);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "back button pressed");
                    mCommunication.messageFromPlaylistFragment("btnBack", "");
                }
            });

        } catch (Exception ex) {
            Log.e(TAG, "exception in onCreateView " + ex);
            Sentry.captureException(ex);
        }
        return root;
    }

    public void SetCurrentplaytime(int index, long duration){
        PlaylistItemsAdapter.SetCurrentPlaytime(index, duration);
    }

    public void GetSongs(){
        PlaylistItemsAdapter.GetSongs();
    }
}
