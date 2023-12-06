package eu.rg_engineering.simplemusicplayer.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import eu.rg_engineering.simplemusicplayer.ArtistItem;
import eu.rg_engineering.simplemusicplayer.ArtistItemsAdapter;
import eu.rg_engineering.simplemusicplayer.MusicItem;
import eu.rg_engineering.simplemusicplayer.MusicItemsAdapter;
import eu.rg_engineering.simplemusicplayer.R;
import eu.rg_engineering.simplemusicplayer.utils.MyItemTouchHelper;
import eu.rg_engineering.simplemusicplayer.utils.OnDeleteArtistitemListener;
import eu.rg_engineering.simplemusicplayer.utils.OnDeleteMusicitemListener;

public class ArtistsFragment extends Fragment implements
        OnDeleteArtistitemListener {

    private String TAG = "ArtistsFragment";
    private RecyclerView rvArtistItems = null;
    private ArtistItemsAdapter ArtistItemsAdapter = null;
    ArrayList<ArtistItem> artists;

    @Override
    public void ItemDeleted() {
        //SaveData();
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_artists, container, false);

        try {
            rvArtistItems = (RecyclerView) root.findViewById(R.id.rvArtists);

            artists = ArtistItem.createItemsList(5);


            // Create adapter passing in the sample user data
            ArtistItemsAdapter = new ArtistItemsAdapter(artists,this);


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


        } catch (Exception ex) {
            Log.e(TAG, "exception in onCreateView " + ex.toString());
        }
        return root;


    }

}
