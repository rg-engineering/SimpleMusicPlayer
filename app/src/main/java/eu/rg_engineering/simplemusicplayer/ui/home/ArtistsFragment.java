package eu.rg_engineering.simplemusicplayer.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import eu.rg_engineering.simplemusicplayer.ArtistItem;
import eu.rg_engineering.simplemusicplayer.ArtistItemsAdapter;
import eu.rg_engineering.simplemusicplayer.MainActivity;
import eu.rg_engineering.simplemusicplayer.MusicData.MusicData;
import eu.rg_engineering.simplemusicplayer.MusicItem;
import eu.rg_engineering.simplemusicplayer.MusicItemsAdapter;
import eu.rg_engineering.simplemusicplayer.R;
import eu.rg_engineering.simplemusicplayer.utils.MyItemTouchHelper;
import eu.rg_engineering.simplemusicplayer.utils.OnDeleteArtistitemListener;
import eu.rg_engineering.simplemusicplayer.utils.OnDeleteMusicitemListener;
import io.sentry.Sentry;


//todo Anzeige Anzahl Artists
//todo Info-Anzeige optimieren
public class ArtistsFragment extends Fragment implements
        OnDeleteArtistitemListener {

    private final String TAG = "ArtistsFragment";
    private RecyclerView rvArtistItems = null;
    private ArtistItemsAdapter ArtistItemsAdapter = null;
    ArrayList<ArtistItem> mArtists;


    @Override
    public void ItemDeleted() {
        //SaveData();
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_artists, container, false);

        try {
            rvArtistItems = (RecyclerView) root.findViewById(R.id.rvArtists);

            MainActivity activity = (MainActivity) getActivity();
            mArtists=activity.mMusicData.getArtistData();

            // Create adapter passing in the sample user data
            ArtistItemsAdapter = new ArtistItemsAdapter(mArtists,this);


            ItemTouchHelper.Callback callback = new MyItemTouchHelper(ArtistItemsAdapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            ArtistItemsAdapter.setTouchHelper(itemTouchHelper);
            itemTouchHelper.attachToRecyclerView(rvArtistItems);
            // Attach the adapter to the recyclerview to populate items
            rvArtistItems.setAdapter(ArtistItemsAdapter);

            // Set layout manager to position the items
            rvArtistItems.setLayoutManager(new LinearLayoutManager(getActivity()));
            rvArtistItems.setItemAnimator(null);
            // That's all!
            AutoCompleteTextView editFilterArtist = (AutoCompleteTextView ) root.findViewById(R.id.filter_artist);
            ArrayList <String> ArtistList = new ArrayList<>();

            for (int i = 0; i < mArtists.size(); i++) {

                String artist = mArtists.get(i).getName();
                if (!ArtistList.contains(artist)) {
                    ArtistList.add(artist);
                }
            }

            ArrayAdapter<String> ArtistListAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_dropdown_item_1line, ArtistList);

            editFilterArtist.setThreshold(1);
            editFilterArtist.setAdapter(ArtistListAdapter);

            editFilterArtist.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d(TAG, "artist filter text changed " + s);
                    ArtistItemsAdapter.setFilterIdx(1);
                    ArtistItemsAdapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } catch (Exception ex) {
            Log.e(TAG, "exception in onCreateView " + ex);
            Sentry.captureException(ex);
        }
        return root;
    }

    public void ReadPlexArtistData(){

        MainActivity activity = (MainActivity) getActivity();
        if (activity.mMusicData!=null) {
            activity.mMusicData.ReadPlexArtistData();
            Log.d(TAG, "plex data read ");

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mArtists != null) {
                        ArtistItemsAdapter.updateItems(mArtists);
                        ArtistItemsAdapter.notifyDatasetChanged();
                    }
                }
            });

            Log.d(TAG, "adapter notified ");
        }
    }
}
